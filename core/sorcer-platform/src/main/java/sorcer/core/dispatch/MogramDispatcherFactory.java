/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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

package sorcer.core.dispatch;

import net.jini.core.lease.Lease;
import net.jini.lease.LeaseRenewalManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.Dispatcher;
import sorcer.core.deploy.ServiceDeployment;
import sorcer.core.exertion.Mograms;
import sorcer.core.loki.member.LokiMemberUtil;
import sorcer.core.monitor.MonitorUtil;
import sorcer.core.monitor.MonitoringSession;
import sorcer.core.provider.Cataloger;
import sorcer.service.Exerter;
import sorcer.core.signature.ServiceSignature;
import sorcer.service.*;
import sorcer.service.modeling.Model;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This class creates instances of appropriate subclasses of Dispatch. The
 * appropriate subclass is determined by calling the provided Mogram insatnce.
 */
public class MogramDispatcherFactory implements DispatcherFactory {
    public static Cataloger catalog; // The service catalog object
    private final static Logger logger = LoggerFactory.getLogger(MogramDispatcherFactory.class.getName());

    private LokiMemberUtil loki;

    public static final long DEFAULT_LEASE_PERIOD = TimeUnit.MINUTES.toMillis(5);
    public static final long LEASE_RENEWAL_PERIOD = TimeUnit.SECONDS.toMillis(60);
    public static final long DEFAULT_TIMEOUT_PERIOD = TimeUnit.SECONDS.toMillis(90);

    protected MogramDispatcherFactory(LokiMemberUtil loki){
        this.loki = loki;
    }

    public static DispatcherFactory getFactory() {
        return new MogramDispatcherFactory(null);
    }

    public static DispatcherFactory getFactory(LokiMemberUtil loki) {
        return new MogramDispatcherFactory(loki);
    }

    public Dispatcher createDispatcher(Mogram mogram,
                                       Set<Context> sharedContexts,
                                       boolean isSpawned,
                                       Exerter provider) throws DispatchException {
        Dispatcher dispatcher = null;
        ProvisionManager provisionManager = null;
        try {
            if (mogram instanceof Routine) {
                List<ServiceDeployment> deployments = ((Subroutine) mogram).getDeployments();
                if (deployments.size() > 0 && (((ServiceSignature) ((ServiceMogram)mogram).getProcessSignature()).isProvisionable()
                    || ((Routine)mogram).isProvisionable()))
                    provisionManager = new ProvisionManager((Routine)mogram);
            }
            if(mogram instanceof Job)
                mogram = new ExertionSorter((Job)mogram).getSortedJob();

            if ( mogram instanceof Block && Mograms.isCatalogBlock((Routine)mogram)) {
                logger.info("Running Catalog Block Dispatch...");
                dispatcher = new CatalogBlockDispatcher((Block)mogram,
                    sharedContexts,
                    isSpawned,
                    provider,
                    provisionManager);
            } else if (isSpaceSequential(mogram)) {
                logger.info("Running Space Sequential Dispatch...");
                dispatcher = new SpaceSequentialDispatcher((Routine)mogram,
                    sharedContexts,
                    isSpawned,
                    loki,
                    provider,
                    provisionManager);
            }
            if (dispatcher==null && mogram instanceof Job) {
                Job job = (Job) mogram;
                if (Mograms.isSpaceParallel(job)) {
                    logger.info("Running Space Parallel Dispatch...");
                    dispatcher = new SpaceParallelDispatcher(job,
                        sharedContexts,
                        isSpawned,
                        loki,
                        provider,
                        provisionManager);
                } else if (Mograms.isCatalogParallel(job)) {
                    logger.info("Running Catalog Parallel Dispatch...");
                    dispatcher = new CatalogParallelDispatcher(job,
                        sharedContexts,
                        isSpawned,
                        provider,
                        provisionManager);
                } else if (Mograms.isCatalogSequential(job)) {
                    logger.info("Running Catalog Sequential Dispatch...");
                    dispatcher = new CatalogSequentialDispatcher(job,
                        sharedContexts,
                        isSpawned,
                        provider,
                        provisionManager);
                }
            }
            assert dispatcher != null;
            MonitoringSession monSession = MonitorUtil.getMonitoringSession(mogram);
            if (mogram.isMonitorable() && monSession!=null) {
                logger.debug("Initializing monitor session for : " + mogram.getName());
                if (!(monSession.getState()==Exec.INSPACE)) {
                    monSession.init((Monitorable) provider.getProxy(),
                        DEFAULT_LEASE_PERIOD,
                        DEFAULT_TIMEOUT_PERIOD);
                } else {
                    monSession.init((Monitorable)provider.getProxy());
                }
                LeaseRenewalManager lrm = new LeaseRenewalManager();
                lrm.renewUntil(monSession.getLease(), Lease.FOREVER, LEASE_RENEWAL_PERIOD, null);
                dispatcher.setLrm(lrm);

                logger.debug("Routine state: " + Exec.State.name(((ServiceMogram)mogram).getStatus()));
                logger.debug("Session for the mogram = " + monSession);
                logger.debug("Lease to be renewed for duration = " +
                    (monSession.getLease().getExpiration() - System
                        .currentTimeMillis()));
            }

            logger.info("*** tally of used dispatchers: " + ExertDispatcher.getDispatchers().size());
        } catch (Exception e) {
            throw new DispatchException("Failed to create the mogram governor for job: "+ mogram.getName(), e);
        }
        return dispatcher;
    }

    protected boolean isSpaceSequential(Mogram mogram) throws RemoteException {
        if(mogram instanceof Job) {
            Job job = (Job) mogram;
            return Mograms.isSpaceSingleton(job) || Mograms.isSpaceSequential(job);
        } else if(mogram instanceof Block)
            return Mograms.isSpaceBlock((Block)mogram);
        else  if(mogram instanceof Model) {
            ServiceStrategy ms = mogram.getDomainStrategy();
            return ms.getAccessType() == Strategy.Access.PULL &&  ms.getFlowType() == Strategy.Flow.SEQ;
        }
        return false;
    }

    /**
     * Returns an instance of the appropriate subclass of Dispatch as
     * determined from information provided by the given Mogram instance.
     *
     * @param mogram
     *            The SORCER job that will be used to perform a collection of
     *            components domains
     */
    @Override
    public Dispatcher createDispatcher(Mogram mogram, Exerter provider, String... config) throws DispatchException {
        return createDispatcher(mogram, Collections.synchronizedSet(new HashSet<Context>()), false, provider);
    }

    @Override
    public SpaceTaskDispatcher createDispatcher(Task task, Exerter provider, String... config) throws DispatchException {
        ProvisionManager provisionManager = null;
        try {
            List<ServiceDeployment> deployments = task.getDeployments();
            if (deployments.size() > 0) {
                provisionManager = new ProvisionManager(task);
            }
            logger.info("Running Space Task Dispatch...");
            return new SpaceTaskDispatcher(task,
                Collections.synchronizedSet(new HashSet<Context>()),
                false,
                loki,
                provisionManager);
        } catch (ContextException | RoutineException | RemoteException e) {
            throw new DispatchException(e);
        }
    }
}
