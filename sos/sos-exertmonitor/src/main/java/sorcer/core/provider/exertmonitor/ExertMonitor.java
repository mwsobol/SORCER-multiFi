/*
 * Copyright 2012 the original author or authors.
 * Copyright 2012 SorcerSoft.org.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sorcer.core.provider.exertmonitor;

import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.DatabaseException;
import com.sun.jini.landlord.LeasedResource;
import com.sun.jini.start.LifeCycle;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.id.Uuid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.UEID;
import sorcer.core.context.RoutineStrategy;
import sorcer.core.monitor.MonitorEvent;
import sorcer.core.monitor.MonitoringManagement;
import sorcer.core.provider.MonitorManagementSession;
import sorcer.core.provider.ServiceExerter;
import sorcer.core.provider.exertmonitor.db.SessionDatabase;
import sorcer.core.provider.exertmonitor.db.SessionDatabaseViews;
import sorcer.core.provider.exertmonitor.lease.MonitorLandlord;
import sorcer.security.util.SorcerPrincipal;
import sorcer.service.*;
import sorcer.util.bdb.objects.UuidKey;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ExertMonitor extends ServiceExerter implements MonitoringManagement {
	static transient final Logger logger = LoggerFactory.getLogger(ExertMonitor.class.getName());
	private MonitorLandlord landlord;
	private SessionDatabase db;
	private StoredMap<UuidKey, MonitorManagementSession> resources;
    private Map<Uuid, UuidKey> cacheSessionKeyMap = new HashMap<>();
    private final Object resourcesWriteLock = new Object();
    private ExertMonitorEventHandler eventHandler;

	public ExertMonitor(String[] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle);
		initMonitor();
	}

	private void initMonitor() throws Exception {
		landlord = new MonitorLandlord();
		String dbHome = getProperty("monitor.database.home");
		File dbHomeFile = null;
		if (dbHome == null || dbHome.length() == 0) {
			logger.error("Session database home missing: " + dbHome);
			destroy();
		} else {
			dbHomeFile = new File(dbHome);
			if (!dbHomeFile.isDirectory() && !dbHomeFile.exists()) {			
				boolean done = dbHomeFile.mkdirs();
				if (!done) {
					logger.error("Not able to create session database home: "
                            + dbHomeFile.getAbsolutePath());
					destroy();
				}
			}
		}
        logger.debug("Opening BDBJE environment in: " + dbHomeFile);
		db = new SessionDatabase(dbHome);
		SessionDatabaseViews views = new SessionDatabaseViews(db);
		resources = views.getSessionMap();

		// statically initialize
		MonitorSession.mLandlord = landlord;
		MonitorSession.sessionManager = (MonitoringManagement) getServiceProxy();

        eventHandler = new ExertMonitorEventHandler(getProviderConfiguration());
	}

	public Routine register(RemoteEventListener lstnr, Routine ex, long duration) throws MonitorException {
		MonitorSession resource = new MonitorSession(ex, lstnr, duration);
		synchronized (resourcesWriteLock) {
			try {
				persist(resource);
			} catch (IOException e) {
				logger.warn("Problem persisting Routine", e);
			}
		}
		return resource.getRuntimeExertion();
	}

	/**
	 * Makes this an active session. The jobber decides the lease duration and
	 * the timeout after which the monitor will prc on monitorables that the
	 * job is failed and report back to the Listener that the exertion of this
	 * session has failed.
	 * 
	 * @param mntrbl
	 *            The monitorable to which this task is dispatched.
	 * @param duration
	 *            Requested lease duration for the session.
	 * @param timeout
	 *            Timeout for execution of this task.
	 * 
	 * @throws MonitorException
	 *             1) If there is no such session 2) If this session is already
	 *             active
	 */
	public Lease init(Uuid cookie, Monitorable mntrbl, long duration, long timeout) throws MonitorException {
		// Get the SessionResource corresponding to this cookie
		MonitorSession resource = findSessionResource(cookie);
		if (resource == null)
			throw new MonitorException("There exists no such session");
        Lease lease = resource.init(mntrbl, duration, timeout);
        Routine exertion = resource.getRuntimeExertion();
        eventHandler.fire(new MonitorEvent(getProxy(), exertion, ((ServiceMogram)exertion).getStatus()));
		return lease;
	}
	
	private MonitorSession findSessionResource(Uuid cookie) throws MonitorException {
		MonitorSession resource;

		// Check if landlord is keeping it in memory
		Map<Uuid, LeasedResource> lresources = landlord.getResources();
		if (lresources.get(cookie) != null)
			return (MonitorSession) lresources.get(cookie);

		for(Map.Entry<Uuid, LeasedResource> entry : lresources.entrySet()) {
			resource = ((MonitorSession) entry.getValue()).getSessionResource(cookie);
			if (resource != null)
				return resource;
		}

		// if (landlord.getResource(cookie)!=null) return
		// (SessionResource)landlord.getResource(cookie);

		// Ok it's not with landlord. So we retrieve it from the database
		synchronized (resourcesWriteLock) {
            Iterator<Map.Entry<UuidKey, MonitorManagementSession>> si = resources.entrySet().iterator();
            Map.Entry<UuidKey, MonitorManagementSession> next;
            while (si.hasNext()) {
                next = si.next();
                try {
                    resource = getSession(next.getKey()).getSessionResource(cookie);
                } catch (Exception e) {
                    throw new MonitorException(e);
                }
                if (resource != null)
                    return resource;
            }
        }
        return null;
    }

    /**
     *
	 * If the Broker wants to drop the exertion to space, then the Broker has no
	 * idea who will pick up this exertion. In that case, it doesn't make sense
	 * for the broker to force leasing. However, it may activate the the session
	 * with the timeout marked and the lease duration specified so that if no
	 * provider picks out and the task gets timed out, then we can clean up the
	 * entry from space and notify the broker.
	 * 
	 * If the provider picks up before it timesout, then the provider must
	 * initialize this session by calling init(Monitorable) so that the monitor
	 * will now make sure that the leases are renewed properly for this session.
	 * 
	 * @param duration
	 *            Requested lease duration for the session.
	 * @param timeout
	 *            Timeout for execution of this task wich includes idle time in
	 *            space.
	 * 
	 * @throws MonitorException
	 *             1) If this session is already active 2) If there is no such
	 *             session
	 */
	public void init(Uuid cookie, long duration, long timeout) throws MonitorException {
		// Get the SessionResource corresponding to this cookie
		MonitorSession resource = findSessionResource(cookie);
		if (resource == null)
			throw new MonitorException("There exists no such session");
		resource.init(duration, timeout);
        Routine exertion = resource.getRuntimeExertion();
        eventHandler.fire(new MonitorEvent(getProxy(), exertion, ((ServiceMogram)exertion).getStatus()));
	}

	/**
	 * 
	 * If the Broker wants to drop the exertion to space, then the Broker has no
	 * idea who will pick up this exertion. In that case, the broker would have
	 * already setValue the lease duration and timeout.
	 * 
	 * The provider who picks up the entry must initialize this session by
	 * calling init(Monitorable) so that the we will now know that the task with
	 * the monitorable and also will make sure that the leases are renewed
	 * properly for this session.
	 * 
	 * @param mntrbl
	 *            The monitorable who picked this up.
	 * 
	 * @throws MonitorException
	 *             1) If there is no such session 2) The execution has been
	 *             inited by some one else.
	 */
	public Lease init(Uuid cookie, Monitorable mntrbl) throws MonitorException {
		// Get the SessionResource corresponding to this cookie
		MonitorSession resource = findSessionResource(cookie);
		if (resource == null)
			throw new MonitorException("There exists no such session");
        Lease lease = resource.init(mntrbl);
        Routine exertion = resource.getRuntimeExertion();
        eventHandler.fire(new MonitorEvent(getProxy(), exertion, ((ServiceMogram)exertion).getStatus()));
		return lease;
	}

	/**
	 * Providers use this method to update their current status of the executed
	 * tasks
	 *
     * @param cookie A Uuid
	 * @param ctx The current state of data of this task.
	 * 
	 * @throws MonitorException
	 *             1) If there is no such session 2) The session is not valid
	 */
    private void update(int aspect, Uuid cookie, Context ctx, RoutineStrategy controlContext) throws MonitorException {
		// Get the SessionResource corresponding to this cookie
		MonitorSession resource = findSessionResource(cookie);
		if (resource == null)
			throw new MonitorException("There exists no such session for: "+ cookie);
		resource.update(ctx, controlContext, aspect);
        Routine exertion = resource.getRuntimeExertion();
        eventHandler.fire(new MonitorEvent(getProxy(), exertion, ((ServiceMogram)exertion).getStatus()));
	}

	/**
	 * Providers use this method to notify that the exertion has been executed.
	 * 
	 * @param ctx
	 *            The monitorable who picked this up.
	 * 
	 * @throws MonitorException
	 *             1) If there is no such session 2) The exertion does not
	 *             belong to this session
     */
    private void done(Uuid cookie, Context ctx, RoutineStrategy controlContext) throws MonitorException {
		// Get the SessionResource corresponding to this cookie
		MonitorSession resource = findSessionResource(cookie);
		if (resource == null)
			throw new MonitorException("There exists no such session");
		resource.done(ctx, controlContext);
        Routine exertion = resource.getRuntimeExertion();
        eventHandler.fire(new MonitorEvent(getProxy(), exertion, ((ServiceMogram)exertion).getStatus()));
	}

	/**
	 * Providers use this method to notify that the exertion was failed
	 * 
	 * @param ctx
	 *            The monitorable who picked this up.
	 * 
	 * @throws MonitorException
	 *             1) If there is no such session 2) The exertion does not
	 *             belong to this session
	 */
	private void failed(Uuid cookie, Context ctx, RoutineStrategy controlContext) throws MonitorException {
		MonitorSession resource = findSessionResource(cookie);
		if (resource == null)
			throw new MonitorException("There exists no such session");
		resource.failed(ctx, controlContext);
        Routine exertion = resource.getRuntimeExertion();
        eventHandler.fire(new MonitorEvent(getProxy(), exertion, ((ServiceMogram)exertion).getStatus()));
	}

	public int getState(Uuid cookie) throws MonitorException {
		MonitorSession resource = findSessionResource(cookie);
		if (resource == null)
			throw new MonitorException("There exists no such session");
		return resource.getState();
	}

	/**
	 * The spec requires that this method gets all the monitorable exertion
	 * infos from all the monitor managers and return a Hashtable where
	 * 
	 * key -> ExertionReferenceID eval -> Some info regarding this exertion
	 *
	 * @throws MonitorException
	 */
	public Map<Uuid, ExertionInfo>  getMonitorableExertionInfo(Exec.State state,
															  Principal principal) throws MonitorException {
        logger.debug("Trying to getValue exertionInfos for: {} for: {}", (state==null?"null":state.toString()), principal);
		Map<Uuid, ExertionInfo> table = new HashMap<>();
		try {
			if (resources==null) return table;
			Iterator<UuidKey> ki = resources.keySet().iterator();
			UuidKey key;
			while (ki.hasNext()) {
				key = ki.next();
                MonitorSession monSession = getSession(key);
                table.putAll(getMonitorableExertionInfo(monSession, key, state, principal));
			}
		} catch (Exception e) {
			logger.error("Failed getting ExertionInfo for principal: {}, State: {}",
                         principal.getName(), (state==null?"null":state.toString()), e);
			throw new MonitorException(e);
		}
		return table;
	}

    private Map<Uuid, ExertionInfo> getMonitorableExertionInfo(MonitorSession monitorSession, UuidKey key, Exec.State state, Principal principal) throws RemoteException,MonitorException {
        Map<Uuid, ExertionInfo> table = new HashMap<>();
        logger.debug("Trying to getValue exertionInfos for: {} state: {} for: {}", monitorSession, (state==null?"null":state.toString()), principal);
        Subroutine xrt = (Subroutine) (monitorSession).getRuntimeExertion();
        if (xrt.getPrincipal().getId()
                .equals(((SorcerPrincipal) principal).getId())) {
            if (state == null || state.equals(Exec.State.NULL)
                    || xrt.getStatus() == state.ordinal()) {
                table.put(xrt.getId(), new ExertionInfo(xrt, xrt.getId(), xrt.getStatus(), key.getId()));
            }
        }
        for (MonitorSession internalSession : monitorSession) {
            table.putAll(getMonitorableExertionInfo(internalSession, key, state, principal));
        }
        return table;
    }

    public Routine getMonitorableExertion(Uuid id, Principal principal) throws MonitorException {
        Routine xrt = getSession(id).getRuntimeExertion();
        if (((Subroutine) xrt).getPrincipal().getId().equals(((SorcerPrincipal) principal).getId()))
            return xrt;
        else
            return null;
    }

    /**
     * For this reference ID, which references a exertion in a monitor, getValue the
	 * exertion if the client has enough credentials.
	 */
	public Routine getMonitorableExertion(UEID cookie, Principal principal) throws MonitorException {
        UuidKey lkey = cacheSessionKeyMap.get(cookie.exertionID);
        Routine ex;
        if (lkey!=null) {
            ex = (getSession(lkey)).getRuntimeExertion();
            if (ex!=null && ((Subroutine) ex).getPrincipal().getId().equals(((SorcerPrincipal) principal).getId()))
                return ex;
            else
                return null;
        }
		Iterator<UuidKey> ki = resources.keySet().iterator();
		while (ki.hasNext()) {
			lkey = ki.next();
			ex = (getSession(lkey)).getRuntimeExertion();
            if (ex!=null) cacheSessionKeyMap.put(((ServiceMogram)ex).getId(), lkey);
            if (cookie.exertionID.equals(ex.getId().toString())
					&& ((Subroutine) ex).getPrincipal().getId().equals(((SorcerPrincipal) principal).getId()))
				return ex;
		}
		return null;
	}


    @Override
    public EventRegistration register(Principal principal, RemoteEventListener listener, long duration)
        throws LeaseDeniedException, RemoteException {
        if(principal!=null && !(principal instanceof SorcerPrincipal)) {
            throw new LeaseDeniedException("supplied principal is not the correct fiType, must be "+
                                           SorcerPrincipal.class.getName());
        }
        return eventHandler.register(this.getProxy(), listener, (SorcerPrincipal)principal, duration);
    }

    @Override
	public void update(Uuid cookie, Context ctx, RoutineStrategy controlContext, int aspect) throws MonitorException {
		if (aspect==Exec.UPDATED || aspect==Exec.PROVISION) {
			update(aspect, cookie, ctx, controlContext);
		} else if (aspect==Exec.DONE) {
			done(cookie, ctx, controlContext);
		} else if (aspect== Exec.FAILED) {
			failed(cookie, ctx, controlContext);
		} else
            logger.warn("Got wrong aspect to update: " + aspect);

	}

	public void destroy() {
		try {
			db.close();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		landlord.terminate();
        eventHandler.terminate();
		super.destroy();
	}

	/* (non-Javadoc)
	 * @see sorcer.core.monitor.MonitorManagement#persist(sorcer.core.provider.exertmonitor.MonitorSession)
	 */
	@Override
	public boolean persist(MonitorManagementSession session) throws IOException {
        logger.warn("Persist {}", session);
		resources.put(new UuidKey(((MonitorSession)session).getCookie()), session);
		return true;
	}
	
	public MonitorSession getSession(UuidKey key) throws MonitorException {
		try {
			return (MonitorSession) resources.get(key);
		} catch (Exception e) {
			throw new MonitorException(e);
		}
	}

	public MonitorSession getSession(Uuid key) throws MonitorException {
		try {
			return (MonitorSession) resources.get(new UuidKey(key));
		} catch (Exception e) {
			throw new MonitorException(e);
		}
	}
}
