/*
 * Copyright 2009 the original author or authors.
 * Copyright 2009 SorcerSoft.org.
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

import net.jini.core.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetTask;
import sorcer.core.exertion.ObjectTask;
import sorcer.core.provider.ControlFlowManager;
import sorcer.core.signature.LocalSignature;
import sorcer.core.signature.RemoteSignature;
import sorcer.core.signature.ServiceSignature;
import sorcer.service.modeling.Conditional;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A <code>Task</code> is an elementary service-oriented message
 * {@link Routine} (with its own service {@link Context} and a collection of
 * service {@link sorcer.service.Signature}s. Signatures of four
 * {@link Signature.Type}s can be associated with each task:
 * <code>SERVICE</code>, <code>PREPROCESS</code>, <code>POSTROCESS</code>, and
 * <code>APPEND</code>. However, only a single <code>PROCESS</code> signature
 * can be associated with a task but multiple preprocessing, postprocessing, and
 * context appending methods can be added.
 * 
 * @see Routine
 * @see sorcer.service.Job
 * 
 * @author Mike Sobolewski
 */
@SuppressWarnings("rawtypes")
public class Task extends Subroutine implements ElementaryRequest {

	private static final long serialVersionUID = 5179772214884L;

	/** our logger */
	protected final static Logger logger = LoggerFactory.getLogger(Task.class);

	public final static String argsPath = "method/args";

	// used for tasks with multiple signatures by CatalogSequentialDispatcher
	private boolean isContinous = false;

	protected Task delegate;
	
	public Task() {
		super("task-" + count++);
	}

	public Task(String name) {
		super(name);
	}

	public Task(Signature signature) {
		addSignature(signature);
	}
	
	public Task(String name, Signature signature) {
		this(name);
		addSignature(signature);
	}
	
	public Task(Signature signature, Context context) {
		addSignature(signature);
		if (context != null)
			dataContext = (ServiceContext)context;
	}
	
	public Task(String name, Signature signature, Context context) {
		this(name);
		addSignature(signature);
		if (context != null)
			dataContext = (ServiceContext)context;
	}
	
	public Task(String name, String description) {
		this(name);
		this.description = description;
	}

	public Task(String name, List<Signature> signatures) {
		this(name, "", signatures);
	}

	public Task(String name, String description, List<Signature> signatures) {
		this(name, description);
		ServiceFidelity sFi = new ServiceFidelity(name);
		sFi.fiType = ServiceFidelity.Type.SIG;
		sFi.selects.addAll(signatures);
		sFi.select = signatures.get(0);
		multiFi.setSelect(sFi);
	}

	public Task doTask(Arg... args) throws ServiceException {
		return doTask(null, args);
	}

	public Task doTask(Transaction txn, Arg... args) throws MogramException, ServiceException {
		initDelegate();
		Task done = delegate.doTask(txn, args);
		setContext(done.getDataContext());
		setControlContext(done.getControlContext());
		return this;
	}

	public void initDelegate() throws MogramException {
		if (delegate != null && multiFi.getSelect() != delegate.getMultiFi().getSelect()) {
			delegate = null;
			dataContext.clearReturnPath();
		}

		try {
			if (delegate == null) {
				ServiceSignature ts = (ServiceSignature) ((ServiceFidelity)multiFi.getSelect()).getSelect();
				if (ts.getClass() == ServiceSignature.class) {
					((ServiceFidelity)multiFi.getSelect()).getSelects().remove(ts);
					ts = createSignature(ts);
				}
				if (ts instanceof RemoteSignature) {
					delegate = new NetTask(key, ts);
				} else {
					delegate = new ObjectTask(key, ts);
				}
				((ServiceFidelity)multiFi.getSelect()).addSelect(ts);
				((ServiceFidelity)multiFi.getSelect()).setSelect(ts);
				delegate.setFidelityManager(getFidelityManager());
				delegate.setServiceMorphFidelity(getServiceMorphFidelity());
				delegate.setMultiMetaFi(getMultiMetaFi());
				delegate.setContext(dataContext);
				delegate.setControlContext(controlContext);
			}
		} catch (SignatureException e) {
			throw new RoutineException(e);
		}
	}

	private ServiceSignature createSignature(ServiceSignature signature) {
		ServiceSignature sig;
		if (signature.getServiceType().isInterface()) {
			sig = new RemoteSignature(signature);
		} else {
			sig = new LocalSignature(signature);
		}
		return sig;
	}

	public Task doTask(Routine xrt, Transaction txn) throws EvaluationException {
		// implemented for example by VarTask
		return null;
	}
	
	public void updateConditionalContext(Conditional condition)
			throws EvaluationException, ContextException {
		// implement is subclasses
	}

	public void undoTask() throws RoutineException, SignatureException {
		throw new RoutineException("Not implemented by this Task: " + this);
	}

	@Override
	public boolean isTask()  {
		return true;
	}
	
	@Override
	public boolean isCmd()  {
		return ((ServiceFidelity)multiFi.getSelect()).getSelects().size() == 1;
	}
	
	public boolean hasChild(String childName) {
		return false;
	}

	/** {@inheritDoc} */
	public boolean isJob() {
		return false;
	}

	public void setOwnerId(String oid) {
		// Util.debug("Owner ID: " +oid);
		this.ownerId = oid;
		List<Service> ls = ((ServiceFidelity)multiFi.getSelect()).getSelects();
		if (ls != null)
			for (Service l : ls)
				((RemoteSignature) l).setOwnerId(oid);
		// Util.debug("Context : "+ context);
		if (dataContext != null)
			dataContext.setOwnerId(oid);
	}

	public ServiceContext doIt() throws RoutineException {
		throw new RoutineException("Not supported method in this class");
	}

	public boolean isNetTask() throws SignatureException {
		return getProcessSignature().getServiceType().isInterface();
	}

	// Just to remove if at all the places.
	public boolean equals(Task task) {
		return key.equals(task.key);
	}

	public String toString() {
		if (delegate != null) {
			return delegate.toString();
		}
		StringBuilder sb = new StringBuilder(
				"\n=== START PRINTING TASK ===\nRoutine Description: "
						+ getClass().getName() + ":" + key);
		sb.append("\n\tstatus: ").append(getStatus());
		sb.append(", task ID=");
		sb.append(getId());
		sb.append(", description: ");
		sb.append(description);
		sb.append(", priority: ");
		sb.append(priority);
		// .append( ", Index=" + getIndex())
		// sb.append(", AccessClass=");
		// sb.append(getAccessClass());
		sb.append(
				// ", isExportControlled=" + isExportControlled()).append(
				", providerName: ");
		if (getProcessSignature() != null)
			sb.append(getProcessSignature().getProviderName());
		sb.append(", principal: ").append(getPrincipal());
		try {
			sb.append(", serviceInfo: ").append(getServiceType());
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		sb.append(", selector: ").append(getSelector());
		sb.append(", parent ID: ").append(parentId);

		if (multiFi.getSelect() != null) {
			List<Service> ls = ((ServiceFidelity) multiFi.getSelect()).getSelects();
			if (ls.size() == 1) {
				sb.append(getProcessSignature().getProviderName());
			} else {
				for (Object s : ls) {
					sb.append("\n  ").append(s);
				}
			}
		}
		String time = getControlContext().getExecTime();
		if (time != null && time.length() > 0)
			sb.append("\n\texecEnt time=").append(time);
		sb.append("\n");
		sb.append(controlContext).append("\n");
//		sb.append(dataContext);
		sb.append(dataContext.getName()).append(": ");
		sb.append(dataContext.getSubjectPath()).append(" = ");
		sb.append(dataContext.getSubjectValue());
		sb.append(dataContext.toString());
		sb.append("\n=== DONE PRINTING TASK ===\n");

		return sb.toString();
	}

	public String describe() {
		StringBuilder sb = new StringBuilder(this.getClass().getName() + ": "
				+ key);
		sb.append(" task ID: ").append(getId()).append("\n  compute sig: ")
				.append(getProcessSignature());
		sb.append("\n  status: ").append(getStatus());
		String time = getControlContext().getExecTime();
		if (time != null && time.length() > 0)
			sb.append("\n  execEnt time: ").append(time);
		return sb.toString();
	}

	/**
	 * Returns true; elementary domains are always "trees."
	 * 
	 * @param visited
	 *            ignored
	 * @return true; elementary domains are always "trees"
	 * @see Routine#isTree()
	 */
	public boolean isTree(Set visited) {
		visited.add(this);
		return true;
	}

	/**
	 * Returns a service task in the specified format. Some tasks can be defined
	 * for thin clients that do not use RMI or Jini.
	 * 
	 * @param type
	 *            the fiType of needed task format
	 * @return
	 */
	public Routine getUpdatedExertion(int type) {
		// the previous implementation of ServiceTask (thin) and
		// RemoteServiceTask (thick) abandoned for a while.
		return this;
	}

	@Override
	public Context linkContext(Context context, String path) {
		try {
			((ServiceContext) context).putLink(path, getDataContext());
		} catch (ContextException e) {
			e.printStackTrace();
		}
		return context;
	}

	@Override
	public Context linkControlContext(Context context, String path) {
		try {
			((ServiceContext) context).putLink(path, getControlContext());
		} catch (ContextException e) {
			e.printStackTrace();
		}
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.service.Routine#addMogram(sorcer.service.Routine)
	 */
	@Override
	public Contextion addMogram(Contextion component) {
		throw new RuntimeException("Tasks do not contain component domains!");
	}

	public Task getDelegate() {
		return delegate;
	}

	public void setDelegate(Task delegate) {
		this.delegate = delegate;
	}
	
	/**
	 * <p>
	 * Returns <code>true</code> if this task takes its service context from the
	 * previously executed task in sequence, otherwise <code>false</code>.
	 * </p>
	 * 
	 * @return the isContinous
	 */
	public boolean isContinous() {
		return isContinous;
	}

	/**
	 * <p>
	 * Assigns <code>isContinous</code> <code>true</code> to if this task takes
	 * its service context from the previously executed task in sequence.
	 * </p>
	 * 
	 * @param isContinous
	 *            the isContinous to set
	 */
	public void setContinous(boolean isContinous) {
		this.isContinous = isContinous;
	}

	protected Task doBatchTask(Transaction txn) throws ServiceException {
		ControlFlowManager ep = new ControlFlowManager();
		return ep.doFidelityTask(this);
	}

	/* (non-Javadoc)
             * @see sorcer.service.Contextion#execute(java.lang.String, sorcer.service.Arg[])
             */
	@Override
	public Object getValue(String path, Arg... args) throws ContextException {
		Object val = dataContext.getValue(path, args);
		if (val == Context.none) {
			if (scope != null){
				try {
					val = scope.getValue(path, args);
				} catch (RemoteException e) {
					throw new ContextException(e);
				}
			}
		}
		return val;
	}

	public List<Contextion> getMograms(List<Contextion> exs) {
		exs.add(this);
		return exs;
	}

	public List<Contextion> getContextions(List<Contextion> exs) {
		exs.add(this);
		return exs;
	}

	@Override
	public List<Contextion> getMograms() {
		List<Contextion> ml = new ArrayList<>();
		ml.add(this);
		return ml;
	}

	@Override
	public List<Contextion> getContextions() {
		List<Contextion> ml = new ArrayList<>();
		ml.add(this);
		return ml;
	}

	public Mogram clearScope() throws MogramException {
		if (!isContinous()) getDataContext().clearScope();
		return this;
	}

	public void correctBatchSignatures() {
		// if all signatures are of service compute SRV fiType make all
		// except the last one of preprocess PRE fiType
		List<Service> alls = ((ServiceFidelity)multiFi.getSelect()).getSelects();
		if (alls.size() > 1) {
			Signature lastSig = (Signature) alls.get(alls.size() - 1);
			if (this.isBatch() && !(lastSig instanceof RemoteSignature)) {
				boolean allSrvType = true;
				for (Service sig : alls) {
					if (!((Signature)sig).getExecType().equals(Signature.SRV)) {
						allSrvType = false;
						break;
					}
				}
				if (allSrvType) {
					for (int i = 0; i < alls.size() - 1; i++) {
						((Signature)alls.get(i)).setType(Signature.PRE);
					}
				}
			}
		}
	}

	@Override
	public Object get(String component) {
		return ((ServiceFidelity)multiFi).getSelect(component);
	}
}
