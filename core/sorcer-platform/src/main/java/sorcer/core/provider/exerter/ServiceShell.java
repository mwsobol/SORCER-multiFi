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

package sorcer.core.provider.exerter;

import net.jini.core.lookup.ServiceID;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.server.TransactionManager;
import org.dancres.blitz.jini.lockmgr.LockResult;
import org.dancres.blitz.jini.lockmgr.MutualExclusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerConstants;
import sorcer.core.context.ControlContext;
import sorcer.core.context.ModelTask;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.ThrowableTrace;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.core.context.model.ent.*;
import sorcer.core.deploy.ServiceDeployment;
import sorcer.core.dispatch.ExertionSorter;
import sorcer.core.dispatch.ProvisionManager;
import sorcer.core.dispatch.SortingException;
import sorcer.core.exertion.ObjectTask;
import sorcer.core.plexus.MorphFidelity;
import sorcer.core.plexus.MorphMogram;
import sorcer.core.provider.*;
import sorcer.core.signature.*;
import sorcer.jini.lookup.ProviderID;
import sorcer.netlet.ServiceScripter;
import sorcer.service.*;
import sorcer.service.Exec.State;
import sorcer.service.Strategy.Access;
import sorcer.service.modeling.Data;
import sorcer.service.modeling.Model;
import sorcer.service.txmgr.TransactionManagerAccessor;
import sorcer.util.Sorcer;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static sorcer.eo.operator.*;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ServiceShell implements Service, Activity, Exertion, Client, Callable<Object>, RemoteServiceShell {
	protected final static Logger logger = LoggerFactory.getLogger(ServiceShell.class);
	private Service service;
	private Contextion mogram;
	private File mogramSource;
	private Transaction transaction;
	private static MutualExclusion locker;
	// a reference to a provider running this mogram
	private Exertion provider;
	private static final ProviderCache providerCache = ProviderCache.get();

	public ServiceShell() {
	}

	public ServiceShell(Mogram mogram) {
		this();
		this.mogram = mogram;
	}

	public ServiceShell(Mogram mogram, Transaction txn) {
		this();
		this.mogram = mogram;
		transaction = txn;
	}

	public void init(Exerter provider) {
		this.provider = provider;
	}

	public Mogram exert(Contextion xrt, Arg... entries)
		throws TransactionException, ServiceException, RemoteException {
		try {
			((ServiceContext)xrt).substitute(entries);
		} catch (Exception e) {
			throw new RoutineException(e);
		}
		return exert(xrt, entries);
	}


	@Override
	public <T extends Contextion> T exert(T mogram, Transaction transaction, Arg... entries) throws MogramException {
		Contextion result = null;
		try {
			if (mogram instanceof Routine) {
				Routine exertion = (Routine) mogram;
				if ((exertion.getProcessSignature() != null
					&& ((ServiceSignature) exertion.getProcessSignature()).isShellRemote())
					|| (exertion.getControlContext() != null
					&& ((ControlContext) exertion.getControlContext()).isShellRemote())) {
					Exertion prv = (Exertion) Accessor.get().getService(sig(RemoteServiceShell.class));
					result = prv.exert(exertion, transaction, entries);
				} else {
					((ServiceMogram)exertion).substitute(entries);
					this.mogram = exertion;
					result = exert(transaction, null, entries);
				}
			}
		} catch (RemoteException | SignatureException | ServiceException e) {
			if (result != null) {
				((ServiceMogram) result).reportException(e);
			} else {
				result = mogram;
			}
		}
		return (T) result;
	}

	public <T extends Mogram> T exert(String providerName) throws ServiceException {
		return exert(null, providerName);
	}

	public <T extends Exertion> T exert(T mogram, Transaction txn, String providerName) throws ServiceException {
		this.mogram = (Mogram) mogram;
		transaction = txn;
		return exert(txn, providerName);
	}


	public <T extends Contextion> T exert(Transaction txn, String providerName, Arg... entries) throws ServiceException {
		try {
			if (mogram instanceof Routine) {
				Subroutine exertion = (Subroutine) mogram;
				((ServiceMogram)mogram).setValid(false);
				exertion.selectFidelity(entries);
				Mogram out = exerting(txn, providerName, entries);

				ServiceContext cxt = (ServiceContext) mogram.getContext();
				if (out instanceof Routine) {
					if (((ServiceMogram)out).getStatus() == Exec.ERROR || ((ServiceMogram)out).getStatus() == Exec.FAILED) {
						return (T) out;
					}
					postProcessExertion(out);
				}
				((ServiceMogram)mogram).setValid(true);
				if (cxt.getMorpher() != null) {
					((ServiceMogram) mogram).getContextFidelityManager().morph();
				}
				if (exertion.getInPathProjection() != null) {
					((ServiceContext) exertion.getContext()).remap(exertion.getInPathProjection());
				}
				if (exertion.getOutPathProjection() != null) {
					((ServiceContext) out.getContext()).setMultiFiPaths(((ServiceContext) mogram.getContext()).getMultiFiPaths());
					((ServiceContext) out.getContext()).remap(exertion.getOutPathProjection());
				}

				if (exertion.isProxy()) {
					Routine xrt = (Routine) out;
					exertion.setContext(xrt.getDataContext());
					exertion.setControlContext((ControlContext) xrt.getControlContext());
					if (exertion.isCompound()) {
						((Transroutine) exertion).setMograms(xrt.getMograms());
					}
					return (T) xrt;
				} else {
					return (T) out;
				}
			} else {
				((Context) mogram).getResponse();
				return (T) mogram;
			}
		} catch (ConfigurationException | RemoteException e) {
			throw new ContextException(e);
		}
	}

	private void initExecState(Arg... entries) throws MogramException, RemoteException {
		Context argCxt = null;
		if (entries != null) {
			for (Arg arg : entries) {
				if (arg instanceof Context && ((Context) arg).size() > 0) {
					argCxt = (Context) arg;
				}
			}
		}
		Exec.State state = ((Subroutine) mogram).getControlContext().getExecState();
		if (state == State.INITIAL) {
			if (mogram instanceof Routine) {
				if (((ServiceMogram) mogram).getExceptions() != null){
					((ServiceMogram) mogram).getExceptions().clear();
				}
				if (((ServiceMogram) mogram).getTrace() != null){
					((ServiceMogram) mogram).getTrace().clear();
				}
			}
			for (Contextion e : ((ServiceMogram)mogram).getAllMograms()) {
				if (e instanceof Routine) {
					if (((ControlContext) ((Routine) e).getControlContext()).getExecState() == State.INITIAL) {
						((ServiceMogram) e).setStatus(Exec.INITIAL);
						if (((ServiceMogram) mogram).getExceptions() != null){
							((ServiceMogram) mogram).getExceptions().clear();
						}
						if (((ServiceMogram) mogram).getTrace() != null) {
							((Mogram) e).getTrace().clear();
						}
					}
				}
				if (e instanceof Block) {
					resetScope((Routine) e, argCxt, entries);
				} else if (e instanceof Mogram) {
					((ServiceMogram) e).clearScope();
				}
			}
		}
	}

	private void resetScope(Routine exertion, Context context, Arg... entries) throws MogramException {
		((ServiceContext) exertion.getDataContext()).clearScope();
		exertion.getDataContext().append(((ServiceContext) exertion.getDataContext()).getInitContext());
		if (entries != null) {
			for (Arg a : entries) {
				if (a instanceof Entry) {
					exertion.getContext().putValue(
						a.getName(), ((Entry) a).getImpl());
				}
			}
		}
		if (context != null) {
			exertion.getDataContext().append(context);
		}
		for (Contextion mogram : exertion.getMograms()) {
			((ServiceMogram) mogram).clearScope();
		}
	}

	private void realizeDependencies(Arg... entries) throws RoutineException {
		List<Evaluation> dependers = ((Subroutine) mogram).getDependers();
		if (dependers != null && dependers.size() > 0) {
			for (Evaluation<Object> depender : dependers) {
				try {
					((Invocation) depender).invoke(mogram.getScope(), entries);
				} catch (Exception e) {
					throw new RoutineException(e);
				}
			}
		}
	}

	private Routine initExertion(Subroutine exertion, Transaction txn, Arg... entries) throws MogramException {
		try {
			// check if the exertion has to be initialized (to original state)
			// or used as is after resuming from suspension or failure
			if (exertion.isInitializable()) {
				initExecState(entries);
			}
			if (entries != null && entries.length > 0) {
				exertion.substitute(entries);
			}
			realizeDependencies(entries);
			if (exertion.getProcessSignature() != null) {
				if (exertion.isTask() && (exertion.isProvisionable()
					|| ((ServiceSignature) exertion.getProcessSignature()).isProvisionable())) {
					List<ServiceDeployment> deploymnets = exertion.getDeployments();
					if (deploymnets.size() > 0) {
						ProvisionManager provisionManager = new ProvisionManager(exertion);
						provisionManager.deployServices();
					} else {
						// no deployment
						((ServiceSignature) exertion.getProcessSignature()).setProvisionable(false);
						logger.warn("no deployment for: {}", exertion.getProcessSignature());
					}
				}
			}
//			//TODO disabled due to problem with monitoring. Needs to be fixed to run with monitoring
//			if (exertion instanceof Job && ((Job) exertion).size() == 1) {
//				return processAsTask();
//			}
			transaction = txn;
			Context<?> cxt = exertion.getDataContext();
			if (cxt != null)
				cxt.setRoutine(exertion);
		} catch (RemoteException | DispatchException ex) {
			throw new ContextException(ex);
		}
		return exertion;
	}

	private Routine processAsTask() throws ServiceException {
		Routine exertion = (Routine) mogram;
		Task task = (Task) exertion.getMograms().get(0);
		task = task.doTask();
		exertion.getMograms().set(0, task);
		((ServiceMogram)exertion).setStatus(task.getStatus());
		return exertion;
	}

	public Mogram exerting(Transaction txn, String providerName, Arg... entries) throws ServiceException {
		Subroutine exertion = (Subroutine) mogram;
		initExertion(exertion, txn, entries);
		Routine xrt = dispatchExertion(exertion, providerName, entries);
		if (xrt != null)
			return xrt;
		else {
			return callProvider(exertion, exertion.getProcessSignature(), entries);
		}
	}

	private Routine dispatchExertion(Subroutine exertion, String providerName, Arg... args)
		throws ServiceException {
		Signature signature = exertion.getProcessSignature();
		Object provider;
		//try {
		// If the exertion is a job rearrange the inner domains to make sure the
		// dependencies are not broken
		if (exertion.isJob()) {
			ExertionSorter es;
			try {
				es = new ExertionSorter(exertion);
			} catch (SortingException e) {
				throw new RoutineException(e.getMessage());
			}
			exertion = (Subroutine) es.getSortedJob();
		}
//			 exert modeling local tasks
		if (exertion instanceof ModelTask && exertion.getSelectedFidelity().getSelects().size() == 1) {
			return ((Task) exertion).doTask(transaction, args);
		}

		// handle delegated tasks with fidelities
		if (exertion.getClass() == Task.class) {
			if (exertion.getSelectedFidelity().getSelects().size() == 1) {
				return ((Task) exertion).doTask(transaction);
			} else {
				try {
					return new ControlFlowManager().doTask((Task) exertion);
				} catch (SignatureException | TransactionException | RemoteException | ServiceException e) {
					throw new MogramException(e);
				}
			}
		}

		// exert object tasks and jobs
		if (!(signature instanceof RemoteSignature)) {
			try {
				if (exertion instanceof Task) {
					if (exertion.getSelectedFidelity() == null
						|| exertion.getSelectedFidelity().getSelects().size() == 1) {
						return ((Task) exertion).doTask(transaction, args);
					} else {
						return new ControlFlowManager().doTask((Task) exertion);
					}
				} else if (exertion instanceof Job) {
					return ((Job) exertion).doJob(transaction);
				} else if (exertion instanceof Block) {
					return ((Block) exertion).doBlock(transaction, args);
				}
			} catch (SignatureException | TransactionException | RemoteException | ServiceException e) {
				throw new MogramException(e);
			}
		}
		// check for missing signature of inconsistent PULL/PUSH cases
		logger.info("signature (before) = {}", signature);

		signature = correctProcessSignature();

		logger.info("signature (after)  = {}", signature);

		if (!((ServiceSignature) signature).isSelectable()) {
			throw new RoutineException("No such operation in the requested signature: " + signature);
		}

		if (providerName != null && providerName.length() > 0) {
			signature.getProviderName().setName(providerName);
		}
		if (logger.isDebugEnabled())
			logger.debug("ServiceShell's service accessor: {}", Accessor.get().getClass().getName());

		// if space exertion should go to Spacer
		if (!exertion.isJob()
			&& exertion.getControlContext().getAccessType() == Access.PULL) {
			String srvName = Sorcer.getActualSpacerName();
			if (signature.getProviderName() instanceof ServiceName) {
				srvName = signature.getProviderName().getName();
			}
			signature = new RemoteSignature("exert", Spacer.class, srvName);
		}
		provider = ((RemoteSignature) signature).getProvider();
		if (provider == null) {
			// check proxy cache and ping with a provider key
			provider = providerCache.getProvider(signature);
		}
		if (provider == null) {
			String message =
				String.format("Provider key: [%s], fiType: %s not found, make sure it is running and there is " +
						"an available lookup service with correct discovery settings",
					signature.getProviderName(), signature.getServiceType().getName());
			logger.error(message);
			throw new RoutineException(message);
		}

		if (provider instanceof Exerter) {
			// cache the provider for the signature
			((RemoteSignature) signature).setProvider((Exerter) provider);
		} else if (exertion instanceof Task) {
			// exert smart proxy as an object task delegate
			LocalSignature sig = new LocalSignature();
			sig.setSelector(signature.getSelector());
			sig.setTarget(provider);
			Context cxt = exertion.getContext();
			((Task) exertion).setDelegate(new ObjectTask(sig, cxt));
			return ((Task) exertion).doTask(transaction);
		}

		this.provider = (Exerter) provider;
		// continue exerting
		return null;
	}

	private Routine callProvider(Subroutine exertion, Signature signature, Arg... entries) throws MogramException {
		if (provider == null) {
			logger.warn("* Provider not available for: {}", signature);
			exertion.setStatus(Exec.FAILED);
			exertion.reportException(new RuntimeException("Cannot find provider for: " + signature));
			return exertion;
		}
		try {
			exertion.trimNotSerializableSignatures();
		} catch (SignatureException e) {
			throw new ContextException(e);
		}
		exertion.getControlContext().appendTrace(String.format("service shell for signature: %s", signature));
		logger.info("Provider found for: {}", signature);
//		if (((Provider) provider).mutualExclusion()) {
//			try {
//				return serviceMutualExclusion((Provider) provider, exertion, transaction);
//			} catch (SignatureException e) {
//				throw new MogramException(e);
//			}
//		} else {
		// test exertion for serialization
//			try{
//				ObjectLogger.persist("exertionfiles.srl", exertion);
//				ObjectLogger.restore("exertionfiles.srl");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}

		Routine result;
		try {
			result = provider.exert(exertion, transaction, entries);
			if (result != null && result.getExceptions().size() > 0) {
				for (ThrowableTrace et : result.getExceptions()) {
					Throwable t = et.getThrowable();
					logger.error("Got exception running: {}", exertion.getName(), t);
					if (t instanceof Error)
						((ServiceMogram)result).setStatus(Exec.ERROR);
				}
				((ServiceMogram)result).setStatus(Exec.FAILED);
			} else if (result == null) {
				exertion.reportException(new RoutineException("ExertionDispatcher failed calling: "
					+ exertion.getProcessSignature()));
				exertion.setStatus(Exec.FAILED);
				result = exertion;
			}
		} catch (RemoteException | ServiceException e) {
			throw new MogramException(e);
		}
		return result;
//		}
	}

	private Routine serviceMutualExclusion(Exerter provider,
										   Routine exertion, Transaction transaction)
		throws RemoteException, TransactionException, ServiceException {
		ServiceID mutexId = provider.getProviderID();
		if (locker == null) {
			locker = Accessor.get().getService(null, MutualExclusion.class);
		}
		TransactionManager transactionManager = TransactionManagerAccessor.getTransactionManager();
		Transaction txn = null;

		LockResult lr = locker.getLock("" + exertion.getProcessSignature().getServiceType(),
			new ProviderID(mutexId),
			txn,
			((ServiceMogram)exertion).getId());
		if (lr.didSucceed()) {
			((ControlContext) exertion.getControlContext()).setMutexId(provider.getProviderID());
			Routine xrt = provider.exert(exertion, transaction);
			txn.commit();
			return xrt;
		} else {
			// try continue to getValue lock, if failed abort the transaction txn
			txn.abort();
		}
		exertion.getControlContext().addException(
			new RoutineException("no lock available for: "
				+ provider.getProviderName() + ":"
				+ provider.getProviderID()));
		return exertion;
	}

	/**
	 * Depending on provider access fiType correct inconsistent signatures for
	 * composite domains only. Tasks go either to its provider directly or
	 * Spacer depending on their provider access fiType (PUSH or PULL).
	 *
	 * @return the corrected signature
	 */
	public Signature correctProcessSignature() {
		Subroutine exertion = (Subroutine) mogram;
		if (!exertion.isJob()) {
			ServiceSignature sig = (ServiceSignature) exertion.getProcessSignature();
			if (sig.getOperation().accessType == Strategy.Access.PUSH) {
				return sig;
			} else if (sig.getOperation().accessType == Strategy.Access.PULL) {
				exertion.setAccess(Strategy.Access.PULL);
				return sig;
			}
		}
		Signature sig = exertion.getProcessSignature();
		if (sig != null) {
			Access access = exertion.getControlContext().getAccessType();
			if (Access.PULL == access
				&& !((ServiceMogram)mogram).getProcessSignature().getServiceType().isAssignableFrom(Spacer.class)) {
				sig.setServiceType(Spacer.class);
				((RemoteSignature) sig).setSelector("exert");
				sig.getProviderName().setName(SorcerConstants.ANY);
				sig.setType(Signature.Type.PRO);
				exertion.getControlContext().setAccessType(access);
			} else if (Access.PUSH == access
				&& !sig.getServiceType()
				.isAssignableFrom(Jobber.class)) {
				if (sig.getServiceType().isAssignableFrom(Spacer.class)) {
					sig.setServiceType(Jobber.class);
					((RemoteSignature) sig).setSelector("exert");
					sig.getProviderName().setName(SorcerConstants.ANY);
					sig.setType(Signature.Type.PRO);
					exertion.getControlContext().setAccessType(access);
				}
			}
		} else {
			sig = new RemoteSignature("exert", Jobber.class);
		}
		return sig;
	}

	public static Mogram postProcessExertion(Mogram mog) throws ServiceException {
		if (mog instanceof Routine) {
			List<Contextion> mograms = null;
			mograms = ((ServiceMogram)mog).getAllMograms();
			for (Contextion mogram : mograms) {
				if (mogram instanceof Routine) {
					List<Setter> ps = ((Subroutine) mogram).getPersisters();
					if (ps != null) {
						for (Setter p : ps) {
							try {
								if (p != null && p instanceof Prc) {
									String from = p.getName();
									Object obj;
									if (mogram instanceof Job)
										obj = ((Job) mogram).getJobContext().getValue(from);
									else {
										obj = mogram.getContext().getValue(from);
									}

									if (obj != null)
										p.setValue(obj);
								}
							} catch (RemoteException e) {
								throw new ServiceException(e);
							}
						}
					}
				}
			}
		}
		return mog;
	}

	private boolean isShellRemote() {
		return provider != null;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public File getMogramSource() {
		return mogramSource;
	}

	public void setMogramSource(File mogramSource) {
		this.mogramSource = mogramSource;
	}

	@Override
	public String toString() {
		if (mogram == null)
			return "ServiceShell";
		else
			return "ServiceShell for: " + mogram.getName();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.concurrent.Callable#prc()
	 */
	@Override
	public Object call() throws Exception {
		return mogram.exert(transaction);
	}

	public Object evaluate(Arg... args) throws MogramException {
		return evaluate(mogram, args);
	}

	public Object evaluate(Contextion mogram, Arg... args) throws MogramException {
		if (mogram instanceof Routine) {
			Routine exertion = (Routine)mogram;
			Object out;
			initialize(exertion, args);
			try {
				if (exertion.getClass() == Task.class) {
					if (((Task) exertion).getDelegate() != null)
						out = exert(((Task) exertion).getDelegate(), null, args);
					else
						out = exertOpenTask(exertion, args);
				} else {
					if (exertion instanceof Task && ((Task)exertion).getDelegate() != null) {
						out = ((Task)exertion).doTask();
					} else {
						out = exert(exertion, null, args);
					}
				}
				return finalize((Routine) out, args);
			} catch (Exception e) {
				logger.error("Failed in compute", e);
				throw new RoutineException(e);
			}
		} else {
			try {
				return ((Model)mogram).getResponse();
			} catch (RemoteException e) {
				throw new MogramException(e);
			}
		}
	}

	private static Routine initialize(Routine xrt, Arg... args) throws ContextException {
		for (Arg a : args) {
			if (a instanceof Context.Return) {
				xrt.getDataContext().setContextReturn((Context.Return) a);
			} else if (a instanceof Fi && ((Fi)a).getFiType().equals(Fi.Type.MMTF)) {
				try {
					// select the both a signature fidelity and a multitype fidelity
					if (xrt.getProcessSignature() instanceof MultiFiSignature) {
						xrt.getProcessSignature().getMultiFi().setSelect(((Fi)a).getPath());
						xrt.getProcessSignature().getMultiFi().selectSelect(a.getName());
					} else {
						xrt.getProcessSignature().getMultiFi().selectSelect(a.getName());
					}
				} catch (ConfigurationException e) {
					throw new ContextException(e);
				}
			}
		}
		return xrt;
	}

	private static Object finalize(Routine xrt, Arg... args) throws ContextException, RemoteException {
		// if the exertion failed return exceptions instead of requested eval
		if (xrt.getExceptions().size() > 0) {
			return xrt.getExceptions();
		}
		Context dcxt = xrt.getDataContext();
		Context.Return rPath = dcxt.getContextReturn();
		// check if it was already finalized
		if (((ServiceContext) dcxt).isFinalized()) {
			return dcxt.getValue(rPath.returnPath);
		}
		// lookup arguments to consider here
		Context.Out outputs = null;
		for (Arg arg : args) {
			if (arg instanceof Context.Out) {
				outputs = (Context.Out)arg;
			}
		}
		// getValue the compound service context
		Context acxt = xrt.getContext();

		if (rPath != null && xrt.isCompound()) {
			// if Path.outPaths.length > 1 return subcontext
			if (rPath.outPaths != null && rPath.outPaths.size() == 1) {
				Object val = acxt.getValue(rPath.outPaths.get(0).path);
				dcxt.putValue(rPath.returnPath, val);
				return val;
			} else {
				Context.Return rp = ((ServiceContext) dcxt).getContextReturn();
				if (rp != null && rPath.returnPath != null) {
					Object result = acxt.getValue(rp.returnPath);
					if (result instanceof Context)
						return ((Context) acxt.getValue(rp.returnPath))
								.getValue(rPath.returnPath);
					else if (result == null) {
						Context out = new ServiceContext();
						logger.debug("\nselected paths: " + rPath.outPaths
								+ "\nfrom context: " + acxt);
						for (Path p : rPath.outPaths) {
							out.putValue(p.path, acxt.getValue(p.path));
						}
						dcxt.setReturnValue(out);
						result = out;
					}
					return result;
				} else {
					return xrt.getContext().getValue(rPath.returnPath);
				}
			}
		} else if (rPath != null) {
			if (rPath.outPaths != null) {
				if (rPath.outPaths.size() == 1) {
					Object val = acxt.getValue(rPath.outPaths.get(0).path);
					if (rPath.returnPath != null) {
						acxt.putValue(rPath.returnPath, val);
					}
					return val;
				} else if (rPath.outPaths.size() > 1) {
					if (rPath.returnPath != null) {
						Object result = acxt.getValue(rPath.returnPath);
						if (result instanceof Context) {
							return result;
						}
					} else {
						Context cxtOut = ((ServiceContext) acxt).getSubcontext(rPath.outPaths);
						if (rPath.outPaths.size() == 1) {
							return ((ServiceContext)cxtOut).get(rPath.outPaths.get(0).getName());
						} else {
							return cxtOut;
						}
					}
				}
			}
		}

		Object obj = xrt.getReturnValue(args);
		if (obj == null) {
			if (rPath != null) {
				return xrt.getReturnValue(args);
			} else {
				return xrt.getContext();
			}
		} else if (obj instanceof Context && rPath != null && rPath.returnPath != null) {
			return (((Context)obj).getValue(rPath.returnPath));
		}
		if (outputs != null) {
			obj = ((ServiceContext) acxt).getSubcontext(outputs);
		}
		return obj;
	}

	public static Routine exertOpenTask(Routine exertion, Arg... args)
			throws EvaluationException {
		Routine closedTask = null;
		List<Object> items = new ArrayList<>();
		for (Arg arg : args) {
			if (arg instanceof ControlContext
					&& ((ControlContext) arg).getSignatures().size() > 0) {
				List<Signature> sigs = ((ControlContext) arg).getSignatures();
				ControlContext cc = (ControlContext) arg;
				cc.setSignatures(null);
				Context tc;
				try {
					tc = exertion.getContext();
				} catch (ContextException e) {
					throw new EvaluationException(e);
				}
				items.add(exertion.getName());
				items.add(tc);
				items.add(cc);
				items.addAll(sigs);
				closedTask = task(items.toArray());
			}
		}
		try {
			closedTask = closedTask.exert(args);
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
		return closedTask;
	}

	public <T extends Mogram> T exert(Service srv, Contextion mog, Transaction txn)
		throws TransactionException, ServiceException, RemoteException {
		this.service = srv;
		this.mogram = mog;
		if (service instanceof Signature) {
			Exerter prv;
			try {
				prv = (Exerter) Accessor.get().getService((Signature) service);
			} catch (SignatureException e) {
				throw new MogramException(e);
			}
			return (T) prv.exert(mogram, txn);
		} else if (service instanceof Jobber) {
			Task out = (Task) ((Jobber)service).exert(mogram, txn);
			return (T) out.getContext();
		} if (service instanceof Spacer) {
			Task out = (Task) ((Spacer) service).exert(mogram, txn);
			return (T) out.getContext();
		} if (service instanceof Concatenator) {
			Task out = (Task) ((Concatenator) service).exert(mogram, txn);
			return (T) out.getContext();
		} else if (service instanceof Mogram) {
			Context cxt;
			if (mogram instanceof Routine) {
				cxt = exert(mogram).getContext();
			} else {
				cxt = (Context) mogram;
			}
			((Mogram) service).setScope(cxt);
			return (T) exert((Contextion) service);
		} else try {
			if (((Signature) service).getServiceType() == RemoteServiceShell.class) {
                Exerter prv = (Exerter) Accessor.get().getService((Signature) service);
                return (T) prv.exert(mogram, txn).getContext();
            } else if (service instanceof Prc) {
                ((Prc)service).setScope(mogram);
                Object val =((Prc)service).evaluate();
                ((Context)mogram).putValue(((Prc)service).getName(), val);
                return (T) mogram;
            }
		} catch (SignatureException | ServiceException e) {
			throw new MogramException(e);
		}
		return (T) ((Exertion)service).exert(mogram, txn);
	}

	public Object exec(Service service, Arg... args) throws ServiceException {
		try {
			if (service != null )
				this.service = service;
			else
				return null;

			if (service instanceof NetletSignature) {
				ServiceScripter se = new ServiceScripter(System.out, null, Sorcer.getWebsterUrl(), true);
				se.readFile(new File(((NetletSignature)service).getServiceSource()));
				return evaluate((Contextion) se.interpret());
			} else if (service instanceof Entry) {
				return exec(service, args);
			} else if (service instanceof EntryModel) {
				((Model)service).getResponse(args);
			} else if (service instanceof Context) {
				ServiceContext cxt = (ServiceContext)service;
				cxt.substitute(args);
				Context.Return returnPath = cxt.getContextReturn();
				if (cxt instanceof EntryModel) {
					return ((Model)service).getResponse(args);
				} else if (returnPath != null){
					return cxt.getValue(returnPath.returnPath, args);
				} else {
					throw new RoutineException("No return contextReturn in the context: " + cxt.getName());
				}
			} else if (service instanceof MorphMogram) {
				Object out;
				MorphFidelity morphFidelity = (( MorphMogram )service).getMorphFidelity();
				ServiceFidelity sfi = (ServiceFidelity) (( MorphMogram )service).getServiceFidelity();
				if (sfi == null) {
					ServiceFidelity fi = (ServiceFidelity) (( MorphMogram )service).getMorphFidelity().getFidelity();
					Object select = fi.getSelect();
					if (select != null) {
						if (select instanceof Mogram)
							out = ((Mogram) select).exert(args);
						else {
							Context cxt = (( MorphMogram )service).getScope();
							if (select instanceof Signature && cxt != null)
								out = ((Service) select).execute(cxt);
							else
								out = ((Service) select).execute(args);
						}
					}
				}
				Context cxt = (( MorphMogram )service).getScope();
				if (morphFidelity != null && morphFidelity.getInMorpher() != null) {
					morphFidelity.getFidelity().setFiType(Fi.Type.IN);
					morphFidelity.setChanged();
					morphFidelity.notifyObservers(cxt);
				}
				if (sfi.getSelect() instanceof Signature && cxt != null) {
					out = sfi.getSelect().execute(cxt);
				} else {
					out = sfi.getSelect().execute(args);
				}

				if (morphFidelity != null) {
					morphFidelity.getFidelity().setFiType(Fi.Type.OUT);
					morphFidelity.setChanged();
					morphFidelity.notifyObservers(out);
				}
				return out;
			}
		} catch (Throwable ex) {
			throw new ServiceException(ex);
		}
		return null;
	}

	@Override
	public Context exec(Service service, Context context, Arg[] args) throws ServiceException {
		Arg[] extArgs = new Arg[args.length+1];
		Arrays.copyOf(args, args.length+1);
		extArgs[args.length] = context;
		return (Context) exec(service, extArgs);
	}


	public <T extends Contextion> T exert(Arg... args) throws ServiceException {
		return exert(null, (String) null, args);
	}

	@Override
	public Object execute(Arg... args) throws ServiceException {
		return evaluate(args);
	}

	@Override
	public Data act(Arg... args) throws ServiceException, RemoteException {
		return null;
	}

	@Override
	public Data act(String entryName, Arg... args) throws ServiceException, RemoteException {
		return null;
	}

}
