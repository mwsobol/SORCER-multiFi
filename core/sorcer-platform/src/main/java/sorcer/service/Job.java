/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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

package sorcer.service;

import net.jini.core.lookup.ServiceID;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.context.ControlContext;
import sorcer.core.context.FidelityContext;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.ThrowableTrace;
import sorcer.core.context.model.ent.Entry;
import sorcer.core.exertion.NetJob;
import sorcer.core.exertion.ObjectJob;
import sorcer.core.provider.Jobber;
import sorcer.core.provider.Spacer;
import sorcer.core.provider.rendezvous.ServiceJobber;
import sorcer.core.signature.LocalSignature;
import sorcer.core.signature.RemoteSignature;
import sorcer.security.util.Auth;
import sorcer.security.util.SorcerPrincipal;
import sorcer.service.Strategy.Access;
import sorcer.service.modeling.ExploreException;
import sorcer.util.SorcerUtil;

import javax.security.auth.Subject;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.*;

/**
 * A job is a composite service-oriented message comprised of {@link Routine}
 * instances with its own service {@link sorcer.service.Context} and a collection of service
 * {@link sorcer.service.Signature}s. The job's signature is usually referring to a
 * {@link Jobber} and the job's context describes the composition
 * of component domains as defined by the Interpreter programming pattern.
 * 
 * @see Routine
 * @see Task
 * 
 * @author Mike Sobolewski
 */
public class Job extends Transroutine {

	private static final long serialVersionUID = -6161435179772214884L;

	/* our logger */
	protected final static Logger logger = LoggerFactory.getLogger(Job.class.getName());

	protected Job delegate;

	public Integer state = new Integer(INITIAL);

	/**
	 * Constructs a job and sets all default values to it.
	 */
	public Job() {
		this("job-" + count++);
		// domains = Collections.synchronizedList(new ArrayList<Routine>());
	}

	/**
	 * Constructs a job and sets all default values to it.
	 * 
	 * @param name
	 *            The key of the job.
	 */
	public Job(String name) {
		super(name);
	}

	/**
	 * Constructs a job and sets all default values to it.
	 * 
	 * @param mogram
	 *            The first Routine of the job.
	 * @throws RoutineException
	 */
	public Job(Mogram mogram) throws RoutineException {
		this("job-" + count++);
		addMogram(mogram);
	}

	public Job(String name, String description) {
		this(name);
		this.description = description;
	}

	public Job(String name, String description, ServiceFidelity fidelity) {
		this(name, description);
		multiFi.setSelect(fidelity);
	}

	/**
	 * Initialize it with assigning it a new ControlContext and a defaultMethod
	 * with serviceInfo as "sorcer.core.provider.jobber.ServiceJobber" key as
	 * "service" and providerName "*"
	 * @throws sorcer.service.SignatureException
	 */
	protected void init() {
		super.init();
		Signature sig;
		if (this instanceof ObjectJob) {
			sig = new LocalSignature("exert", ServiceJobber.class);
		} else {
			sig = new RemoteSignature("exert", Jobber.class);
		}
		sig.getProviderName().setName(null);
		sig.setType(Signature.Type.PRO);
		ServiceFidelity sFi = new ServiceFidelity(sig);
		sFi.setSelect(sig);
		((ServiceFidelity)multiFi).getSelects().add(sFi);// Add the signature
		multiFi.setSelect(sFi);
	}

	@Override
	public boolean isJob() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Routine#isCompound()
	 */
	@Override
	public boolean isCompound() {
		return true;
	}

	public long getLsbID() {
		return (lsbId == null) ? -1 : lsbId.longValue();
	}

	/**
	 * Returns the number of domains in this Job.
	 * 
	 * @return the number of domains in this Job.
	 */
	public int size() {
		return mograms.size();
	}

	public int indexOf(Routine ex) {
		return mograms.indexOf(ex);
	}

	public void setRendezvousName(String jobberName) {
		controlContext.setRendezvousName(jobberName);
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Routine#addMogram(sorcer.service.Routine)
	 */
	@Override
	public Contextion addMogram(Contextion ex) throws RoutineException {
		mograms.add(ex);
		((ServiceMogram)ex).setIndex(mograms.indexOf(ex));
		try {
			controlContext.registerExertion((Mogram)ex);
			((ServiceMogram)ex).getDataContext().setScope(dataContext);
		} catch (ContextException | RemoteException e) {
			throw new RoutineException(e);
		}
		((ServiceMogram)ex).setParentId(getId());
//		((ServiceMogram)ex).setParent(this);
		return this;
	}

    public Job doJob(Transaction txn) throws MogramException,
            SignatureException, RemoteException, TransactionException, ServiceException {
        if (delegate == null) {
            if (delegate == null) {
                Signature ps = (Signature) ((ServiceFidelity)multiFi.getSelect()).getSelect();
                if (ps instanceof RemoteSignature) {
                    delegate = new NetJob(key);
                } else {
                    delegate = new ObjectJob(key);
                }

                delegate.setFidelityManager(getFidelityManager());
                delegate.setMultiFi((ServiceFidelity) getMultiFi());
                delegate.setSelectedFidelity((ServiceFidelity) multiFi.getSelect());
                delegate.setServiceMorphFidelity(getServiceMorphFidelity());
                delegate.setMultiMetaFi(getMultiMetaFi());
                delegate.setContext(dataContext);
                delegate.setControlContext(controlContext);
            }

            if (delegate instanceof NetJob) {
                delegate.setControlContext(controlContext);
                if (controlContext.getAccessType().equals(Access.PULL)) {
                    Signature procSig = delegate.getProcessSignature();
                    procSig.setServiceType(Spacer.class);
					((ServiceFidelity)delegate.multiFi.getSelect()).selects.clear();
                    delegate.addSignature(procSig);
                }
            }
            if (mograms.size() > 0) {
                for (Contextion ex : mograms) {
                    delegate.addMogram(ex);
                }
            }
        }

        return delegate.doJob(txn);
    }

	public void undoJob() throws RoutineException, SignatureException,
			RemoteException {
		throw new RoutineException("Not implemneted by this Job: " + this);
	}
	
	public void setState(int state) {
		this.state = new Integer(state);
	}

	public int getState() {
		return state.intValue();
	}

	public String getPrincipalID() {
		Set principals = subject.getPrincipals();
		Iterator iterator = principals.iterator();
		while (iterator.hasNext()) {
			Principal p = (Principal) iterator.next();
			if (p instanceof SorcerPrincipal)
				return ((SorcerPrincipal) p).getId();
		}
		return null;
	}

	public void setPrincipalID(String id) {
		Set principals = subject.getPrincipals();
		Iterator iterator = principals.iterator();
		while (iterator.hasNext()) {
			Principal p = (Principal) iterator.next();
			if (p instanceof SorcerPrincipal)
				((SorcerPrincipal) p).setId(id);
		}
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
		for (int i = 0; i < size(); i++) {
			((Subroutine) get(i)).setSubject(subject);
		}
	}

	public void setPrincipal(SorcerPrincipal principal) {
		setSubject(Auth.createSubject(principal));
		this.principal = principal;
	}

	public Subject getSubject() {
		return subject;
	}
	
	public ServiceID getServiceID() {
		if (lsbId == null || msbId == null)
			return null;
		else
			return new ServiceID(msbId.longValue(), lsbId.longValue());
	}
	
	/**
	 * Returns a string representation of Contexts of this Job, containing the
	 * String representation of each context in it's exertion.
	 * @throws sorcer.service.RoutineException
	 */
	public String jobContextToString() throws RoutineException {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mograms.size(); i++) {
			if (((Subroutine) get(i)).isJob())
				sb.append(((Job) get(i)).jobContextToString());
			else
				sb.append(((Subroutine) get(i)).contextToString());
		}
		return sb.toString();
	}

	public void setMasterExertion(Routine exertion) {
		controlContext.setMasterExertion(exertion);
	}

	public void setOwnerId(String id) {
		ownerId = id;
		if (controlContext != null)
			controlContext.setOwnerId(id);
		for (int i = 0; i < mograms.size(); i++)
			(((Subroutine) get(i))).setOwnerId(id);
	}

	public String getContextName() {
		return Context.JOB_ + key + "[" + index + "]" + Context.ID;
	}

	public String toString() {
		StringBuffer desc = new StringBuffer(super.toString());
		desc.append("\n=== START PRINTING JOB ===\n");	
		desc.append("\n=============================\nListing Component Exertions\n=============================\n");
		for (int i = 0; i < size(); i++) {
			desc.append("\n===========\n Routine ").append(i).append("\n===========\n").append((get(i)));
		}
		desc.append("\n=== DONE PRINTING JOB ===\n");
		return desc.toString();
	}

	@Override
	public List<ThrowableTrace> getExceptions() throws RemoteException {
		List<ThrowableTrace> exceptions = new ArrayList<>();
		for (Contextion ext : mograms) {
			exceptions.addAll(((Mogram)ext).getExceptions());
		}
		return exceptions;
	}
	
	/**
	 * Return true if this composite <code>Job</code> is a tree.
	 * 
	 * @param visited
	 *            a set of visited domains
	 * @return true if this <code>Job</code> composite is a tree
	 * @see Routine#isTree()
	 */
	public boolean isTree(Set visited) {
		visited.add(this);
		Iterator i = mograms.iterator();
		while (i.hasNext()) {
			Subroutine e = (Subroutine) i.next();
			if (visited.contains(e) || !e.isTree(visited)) {
				return false;
			}
		}
		return true;
	}

	public Contextion getExertion(int index) {
		return mograms.get(index);
	}

	public Context finalizeOutDataContext() throws ContextException {
		if (dataContext.getDomainStrategy().getOutConnector() != null) {
			updateContextWith(dataContext.getDomainStrategy().getOutConnector());
		}
		return dataContext;
	}

	// TODO in/out/inout marking as defined in the connector
	public Context updateContextWith(Context connector) throws ContextException {
		if (connector != null) {
			Context jobContext =  getJobContext();
			Iterator it = ((ServiceContext)connector).entryIterator();
			while (it.hasNext()) {
				Map.Entry e = (Map.Entry) it.next();
				try {
					dataContext.putInValue((String) e.getKey(), jobContext.getValue((String) e.getValue()));
					dataContext.removePath((String) e.getValue());
				} catch (RemoteException ex) {
					throw new ContextException(ex);
				}
			}
		}
		return dataContext;
	}

	public Context getContext() throws ContextException {
		 return getJobContext();
	}
	
	public Context getJobContext() throws ContextException {
		ServiceContext cxt = new ServiceContext(key);
		cxt.setSubject("job/data/context", key);
		cxt.append(dataContext);
		return linkContext(cxt, getName());
	}

	public Context getControlInfo() {
		ServiceContext cxt = new ServiceContext(key);
		cxt.setSubject("job/control/context", key);
		
		return linkControlContext(cxt,  getName());
	}

	@Override
	public Context linkContext(Context context, String path) {
		Mogram ext;
		for (int i = 0; i < size(); i++) {
			ext = (Mogram) mograms.get(i);
			try {
				((Subroutine) ext).linkContext(context, path + CPS + ext.getName());
			} catch (ContextException e) {
				e.printStackTrace();
			}
		}
		return context;
	}
	
	@Override
	public Context linkControlContext(Context context, String path) {
		Mogram ext;
		for (int i = 0; i < size(); i++) {
			ext = (Mogram) mograms.get(i);
			try {
				((Subroutine) ext).linkControlContext(context, path + CPS
						+ ext.getName());
			} catch (ContextException e) {
				e.printStackTrace();
			}
		}
		return context;
	}

	public Object getJobValue(String path) throws ContextException {
		String[] attributes = SorcerUtil.pathToArray(path);
		// remove the leading attribute of the current exertion
		if (attributes[0].equals(getName())) {
			String[] attributes1 = new String[attributes.length - 1];
			System.arraycopy(attributes, 1, attributes1, 0,
					attributes.length - 1);
			attributes = attributes1;
		}
		String last = attributes[0];
		Mogram exti = this;
		for (String attribute : attributes) {
			if (((Subroutine) exti).hasChild(attribute)) {
				exti = (Mogram) ((Job) exti).getChild(attribute);
				if (exti instanceof Task) {
					last = attribute;
					break;
				}
			} else {
				break;
			}
		}
		int index = path.indexOf(last);
		String contextPath = path.substring(index + last.length() + 1);

		try {
			return exti.getContext().getValue(contextPath);
		} catch (RemoteException ex) {
			throw new ContextException(ex);
		}
	}

	/* (non-Javadoc)
         * @see sorcer.service.Contextion#execute(java.lang.String, sorcer.service.Arg[])
         */
	@Override
	public Object getValue(String path, Arg... args) throws ContextException {
		if (path.indexOf(key) >= 0) {
			return getJobValue(path);
		}
		Object val = dataContext.getValue(path, args);
		if (val == Context.none) {
			try {
				val = scope.getValue(path, args);
			} catch (RemoteException ex) {
				throw new ContextException(ex);
			}
		}
		return val;
	}

	public Object putValue(String path, Object value) throws ContextException {
		if (path.indexOf(key) >= 0)
			putJobValue(path, value);
		else
			super.putValue(path, value);
		return value;
	}
	
	public Object putJobValue(String path, Object value) throws ContextException {
		String[] attributes = SorcerUtil.pathToArray(path);
		// remove the leading attribute of the current exertion
		if (attributes[0].equals(getName())) {
			String[] attributes1 = new String[attributes.length - 1];
			System.arraycopy(attributes, 1, attributes1, 0,
					attributes.length - 1);
			attributes = attributes1;
		}
		String last = attributes[0];
		Routine exti = this;
		for (String attribute : attributes) {
			if (((Subroutine) exti).hasChild(attribute)) {
				exti = (Routine)((Job) exti).getChild(attribute);
				if (exti instanceof Task) {
					last = attribute;
					break;
				}
			} else {
				break;
			}
		}
		int index = path.indexOf(last);
		String contextPath = path.substring(index + last.length() + 1);
		exti.getContext().putValue(contextPath, value);
		return value;
	}
	
	public Context.Return getReturnPath() {
		return dataContext.getContextReturn();
	}
	
	@Override
	public Object getReturnValue(Arg... entries) throws ContextException {
		//TODO for getJobContextReturn
		//Return rp = ((ServiceContext) dataContext).getJobContextReturn();
		Context.Return rp = dataContext.getContextReturn();
		Object obj;
		if (rp != null) {
			try {
				if (rp.returnPath == null || rp.returnPath.equals(Signature.SELF)) {
					return this;
				} else if (rp.type != null) {
					obj = rp.type.cast(getContext().getValue(rp.returnPath));
				} else {
					obj = getContext().getValue(rp.returnPath);
				}
			} catch (RemoteException e) {
				throw new ContextException(e);
			}
		} else {
			obj = getJobContext();
		}
		return obj;
	}
	
	public Context getComponentContext(String path) throws ContextException {
		Routine xrt = (Routine)getComponentMogram(path);
		return xrt.getContext();
	}
	
	public Context getComponentControlContext(String path) {
		Routine xrt = (Routine)getComponentMogram(path);
		return xrt.getControlContext();
	}
	
	public Mogram getComponentMogram(String path) {
		String[] attributes = SorcerUtil.pathToArray(path);
		// remove the leading attribute of the current exertion
		if (attributes[0].equals(getName())) {
			String[] attributes1 = new String[attributes.length - 1];
			System.arraycopy(attributes, 1, attributes1, 0,
					attributes.length - 1);
			attributes = attributes1;
		}
		Routine exti = this;
		for (String attribute : attributes) {
			if (((Subroutine) exti).hasChild(attribute)) {
				exti = (Routine)((Transroutine) exti).getChild(attribute);
				if (exti instanceof Task) {
					break;
				}
			} else {
				break;
			}
		}
		return exti;
	}

	public void applyFidelityContext(FidelityContext fiContext) throws RoutineException {
		Collection<ServiceFidelity> fidelities = fiContext.values();
		Subroutine se = null;
		for (ServiceFidelity fi : fidelities) {
			if (fi instanceof ServiceFidelity) {
				se = (Subroutine) getComponentMogram(fi.getPath());
				try {
					se.selectFidelity(fi.getName());
				} catch (ConfigurationException e) {
					throw new RoutineException(e);
				}
			}
		}
	}

	@Override
	public void substitute(Arg... entries)
			throws SetterException {
		try {
			if (entries != null) {
				for (Arg e : entries) {
					if (e instanceof Entry)
						if (e.getName().indexOf(key) >= 0)
							putJobValue(e.getName(), this.getValue());

						else
							super.putValue(e.getName(), ((Entry) e).getValue());

					// check for control strategy
					else if (e instanceof ControlContext) {
						updateControlContect((ControlContext)e);
					}
				}
			}
		} catch (ContextException ex) {
			ex.printStackTrace();
			throw new SetterException(ex);
		}
	}

	@Override
	public Context analyze(Context context, Arg... args) throws EvaluationException {
		try {
			return exert(context);
		} catch (ServiceException e) {
			throw new EvaluationException(e);
		}
	}

	@Override
	public Context explore(Context context, Arg... args) throws ContextException {
		try {
			return exert(context);
		} catch (ServiceException e) {
			throw new ContextException(e);
		}
	}
}
