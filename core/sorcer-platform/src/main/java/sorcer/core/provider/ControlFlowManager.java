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
package sorcer.core.provider;

import net.jini.config.ConfigurationException;
import net.jini.core.transaction.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.ThrowableTrace;
import sorcer.core.exertion.*;
import sorcer.core.provider.rendezvous.SorcerExerterBean;
import sorcer.core.provider.rendezvous.ServiceConcatenator;
import sorcer.core.provider.rendezvous.ServiceJobber;
import sorcer.core.provider.rendezvous.ServiceSpacer;
import sorcer.service.*;
import sorcer.service.Strategy.Access;
import sorcer.service.jobber.JobberAccessor;
import sorcer.service.modeling.Conditional;
import sorcer.service.spacer.SpacerAccessor;
import sorcer.util.AccessorException;

import java.rmi.RemoteException;
import java.util.List;

import static sorcer.eo.operator.task;

/**
 * The ControlFlowManager class is responsible for handling control flow
 * domains ({@link Conditional}, {@link NetJob}, {@link NetTask}).
 * 
 * This class is used by the {@link sorcer.core.provider.exerter.ServiceShell} class for executing
 * Exertions.
 * 
 * @author Mike Sobolewski
 */
@SuppressWarnings("rawtypes")
public class ControlFlowManager {

    /**
     * Logger for this ExerterController logging.
     */
    protected static final Logger logger = LoggerFactory.getLogger(ControlFlowManager.class);

	/**
	 * ExertionDelegate reference needed for handling domains.
	 */
	protected ProviderDelegate delegate;

	/**
	 * The Routine that is going to be executed.
	 */
	protected Routine exertion;

    /**
     * Reference to a jobber proxy if available.
     */
    protected Jobber jobber;

	/**
	 * Reference to a concatenator proxy if available.
	 */
	protected Concatenator concatenator;

	/**
	 * Reference to a spacer service.
	 */
	protected Spacer spacer;

	/**
	 * Default Constructor.
	 */
	public ControlFlowManager() {
	}

	/**
	 * Overloaded constructor which takes in an Routine and an ExerterDelegate.
	 * 
	 * @param exertion
	 *            Routine
	 * @param delegate
	 *            ExerterDelegate
     * @throws ConfigurationException
     * @throws RemoteException
     */
	public ControlFlowManager(Routine exertion, ProviderDelegate delegate)
            throws RemoteException, ConfigurationException {
		this.delegate = delegate;
		this.exertion = exertion;
        init();
	}

    /**
     * Overloaded constructor which takes in an Routine, ExerterDelegate, and
     * Spacer. This constructor is used when handling {@link sorcer.service.Job}s.
     *
     * @param exertion
     *            Routine
     * @param delegate
     *            ExerterDelegate
     * @param serviceBean
     *            Rendezvous
     * @throws ConfigurationException
     * @throws RemoteException
     */
    public ControlFlowManager(Routine exertion, ProviderDelegate delegate,
							  SorcerExerterBean serviceBean) throws RemoteException, ConfigurationException {
        this.delegate = delegate;
        this.exertion = exertion;
        if (serviceBean instanceof Concatenator) {
            concatenator = (Concatenator) serviceBean;
        }
        else if (serviceBean instanceof Spacer){
            spacer = (Spacer) serviceBean;
        }
        else if (serviceBean instanceof Jobber) {
            jobber = (Jobber) serviceBean;
        }
        init();
    }


    /**
     * Overloaded constructor which takes in an Routine, ExerterDelegate, and
     * Spacer. This constructor is used when handling {@link sorcer.service.Job}s.
     *
     * @param exertion
     *            Routine
     * @param delegate
     *            ExerterDelegate
 	 * @param jobber
	 *            Jobber
     * @throws ConfigurationException
     * @throws RemoteException
     */
	public ControlFlowManager(Routine exertion, ProviderDelegate delegate,
							  Jobber jobber) throws RemoteException, ConfigurationException {
        this(exertion, delegate);
		this.jobber = jobber;
	}
	/**
	 * Overloaded constructor which takes in an Routine, ExerterDelegate, and
	 * Concatenator. This constructor is used when handling {@link sorcer.service.Block}s.
	 * 
	 * @param exertion
	 *            Routine
	 * @param delegate
	 *            ExerterDelegate
	 * @param concatenator
	 *            Concatenator
     * @throws ConfigurationException
     * @throws RemoteException
	 */
	public ControlFlowManager(Routine exertion, ProviderDelegate delegate,
							  Concatenator concatenator) throws RemoteException, ConfigurationException {
		this(exertion, delegate);
		this.concatenator = concatenator;
	}

    private void init() throws RemoteException, net.jini.config.ConfigurationException {
        if (concatenator==null) {
            Concatenator c = (Concatenator) delegate.getBean(Concatenator.class);
            if (c != null) {
                concatenator = c;
            }
        }
        if (jobber==null) {
            Jobber j = (Jobber) delegate.getBean(Jobber.class);
            if (j != null) {
                jobber = j;
            }
        }
        if (spacer==null) {
            Spacer s = (Spacer) delegate.getBean(Spacer.class);
            if (s != null) {
                spacer = s;
            }
        }
    }


    /**
     * Process the Routine accordingly if it is a job, task, or a Conditional
     * Routine.
     *
     * @return Routine the result
     * @see NetJob
     * @see NetTask
     * @see Conditional
     * @throws RoutineException
     *             exception from other methods
     */
    public Mogram process() throws RoutineException {
        logger.info("compute exertion: " + exertion.getName());
        try {
            Mogram result = null;
            if (exertion.isConditional()) {
                logger.info("exertion Conditional");
                result = doConditional(exertion);
                logger.info("exertion Conditional; result: " + result);
            } else if (exertion.isJob()) {
                logger.info("exertion isJob()");
                result = doRendezvousExertion((Job) exertion);
                logger.info("exertion isJob(); result: " + result);
            } else if (exertion.isBlock()) {
                logger.info("exertion isBlock()");
                result = doBlock((Block) exertion);
                logger.info("exertion isBlock(); result: " + result);
            } else if (exertion.isTask()) {
                logger.info("exertion isTask()");
                result = doTask((Task) exertion);
                logger.info("exertion isTask(); result: " + result);
                for(ThrowableTrace t : ((Task)result).getExceptions()) {
                    logger.warn("Exception processing Task", t.getThrowable());
                }
            }
            return result;
        } catch (RuntimeException | RoutineException e) {
            throw e;
        } catch (Exception e) {
            throw new RoutineException(e.getMessage(), e);
        }
    }

    /**
     * This method delegates the doTask method to the ExertionDelegate.
     *
     * @param task
     *            ServiceTask
     * @return ServiceTask
     * @throws RemoteException
     *             exception from ExertionDelegate
     * @throws RoutineException
     *             exception from ExertionDelegate
     * @throws SignatureException
     *             exception from ExertionDelegate
     * @throws TransactionException
     * @throws ContextException
     */
    public Task doTask(Task task) throws RemoteException, ServiceException,
        SignatureException, TransactionException {
        Task result;
        if (task.getControlContext().getAccessType() == Access.PULL) {
            result = (Task)doRendezvousExertion(task);
        } else if (delegate != null) {
            result = delegate.doTask(task, null);
        } else if (task.isConditional()) {
            result = doConditional(task);
        } else {
            result = doFidelityTask(task);
        }

        return result;
    }

	public Block doBlock(Block block) throws RemoteException, ServiceException,
        SignatureException, TransactionException {
        if (concatenator == null) {
            String spacerName = block.getRendezvousName();
            if (spacerName != null) {
                concatenator = Accessor.get().getService(spacerName, Concatenator.class);
            } else {
                try {
                    concatenator = Accessor.get().getService(null, Concatenator.class);
                } catch (Exception x) {
                    throw new RoutineException("Could not find Concatenator", x);
                }
            }
            logger.info("Got Concatenator: " + concatenator);
            return concatenator.exert(block, null);
        }
        return (Block)((ServiceConcatenator)concatenator).localExert(block, null);
	}

    /**
     * Selects a Jobber or Spacer for exertion processing. If own Jobber or
     * Spacer is not available then fetches one and forwards the exertion for
     * processing.
     *
     * @param xrt
     * 			the exertion to be processed
     * @throws RemoteException
     * @throws RoutineException
     */
    public Mogram doRendezvousExertion(Subroutine xrt) throws RemoteException {
        try {
            if (xrt.isSpacable()) {
                logger.info("exertion isSpacable");

                if (spacer == null) {
                    String spacerName = xrt.getRendezvousName();
                    Spacer spacerService;
                    if (spacerName != null) {
                        spacerService = SpacerAccessor.getSpacer(spacerName);
                    } else {
                        try {
                            spacerService = SpacerAccessor.getSpacer();
                        } catch (Exception x) {
                            throw new RoutineException("Could not find Spacer", x);
                        }
                    }
                    logger.info("Got Spacer: " + spacerService);
                    return spacerService.exert(xrt, null);
                }
				Mogram job = ((ServiceSpacer)spacer).localExert(xrt, null);
                logger.info("spacable exerted = " + job);
                return job;
            }
            else {
                logger.info("exertion NOT Spacable");
                if (jobber == null) {
                    String jobberName = xrt.getRendezvousName();
					Jobber jobberService;
                    if (jobberName != null)
                        jobberService = JobberAccessor.getJobber(jobberName);
                    else
                        try {
                            jobberService = JobberAccessor.getJobber();
                        } catch (AccessorException e) {
                            throw new RoutineException("Could not find Jobber", e);
                        }
                    logger.info("Got Remote Jobber: " + jobber);
                    return jobberService.exert(xrt, null);
                }
				Mogram job = ((ServiceJobber)jobber).localExert(xrt, null);
                logger.info("job exerted = " + job);
                return job;
            }
        } catch (TransactionException | ServiceException e) {
            logger.error( "Error", e);
        }
        return null;
    }
    /**
     * This method handles the {@link Conditional} Exertions. It determines if
     * the conditional Routine: {@link OptTask}, {@link AltTask}, and
     * {@link LoopTask}.
     *
     * @param exertion
     *            Conditional multitype Routine
     * @return Routine
     * @throws SignatureException
     * @throws RoutineException
     * @throws RemoteException
     */
    public Task doConditional(Routine exertion) throws ServiceException {
        return ((Task) exertion).doTask();
    }

    /*
     * This mehtod saves all the data nodes of a context and put it on a Map.
     *
     * @param mapBackUp
     *            HashMap where the ServiceContext data nodes are saved
     * @param context
     *            ServiceContext to be saved into the HashMap

    public static void saveState(Map<String, Object> mapBackUp, Context context) {
        try {
            Enumeration e = context.contextPaths();
			String contextReturn;
            while (e.hasMoreElements()) {
				contextReturn = (String) e.nextElement();
				mapBackUp.put(contextReturn, context.getValue(contextReturn));
            }
        } catch (ContextException ce) {
            logger.info("problem saving state of the ServiceContext " + ce);
            ce.printStackTrace();
        }
    }
*/
    /*
	 * Copies the backup map of the dataContext to the passed dataContext.
     *
     * @param mapBackUp
     *            Saved HashMap which is used to restore from
     * @param context
     *            ServiceContext that gets restored from the saved HashMap

    public static void restoreState(Map<String, Object> mapBackUp,
                                    Context context) {
        Iterator iter = mapBackUp.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String contextReturn = (String) entry.getKey();
			Object eval = entry.execute();

            try {
                context.putValue(contextReturn, eval);
            } catch (ContextException e) {
                e.printStackTrace();
            }
        }
    }
*/

    /*
	 * Copies the data nodes from one dataContext to another (shallow copy).
     *
     * @param fromContext
     *            ServiceContext
     * @param toContext
     *            ServiceContext

    public static void copyContext(Context fromContext, Context toContext) {
        try {
            Enumeration e = fromContext.contextPaths();
			String contextReturn;

            while (e.hasMoreElements()) {
				contextReturn = (String) e.nextElement();
                toContext.putValue(contextReturn, fromContext.execute(contextReturn));
            }
        } catch (ContextException ce) {
            ce.printStackTrace();
        }
    }
*/

    /*
     * Checks if the Routine is valid for this provider. Returns true if it is
     * valid otherwise returns false.
     *
     * @param exertion
     *            Routine interface
     * @return boolean

    public boolean isValidExertion(Routine exertion) {
        String pn = exertion.getProcessSignature().getProviderName();

        if (!(pn == null || pn.equals(SorcerConstants.NULL) || SorcerConstants.ANY
                .equals(pn.trim()))) {
            if (!pn.equals(delegate.config.getProviderName()))
                return false;
        }

        for (int i = 0; i < delegate.publishedServiceTypes.length; i++) {
            if (delegate.publishedServiceTypes[i].equals(exertion
                    .getProcessSignature().getMultitype()))
                return true;
        }

        return false;
    }

    public void setConcatenator(ServiceConcatenator concatenator) {
        this.concatenator = concatenator;
    }

    public void setJobber(ServiceJobber rendezvous) {
        this.rendezvous = rendezvous;
    }

    public void setSpacer(Spacer spacer) {
        this.spacer = spacer;
    }
*/

    /*
     * Traverses the Job hierarchy and reset the task status to INITIAL.
     *
     * @param exertion
     *            Either a task or job
    public void resetExertionStatus(Routine exertion) {
		if (exertion.isTask()) {
            ((Task) exertion).setStatus(Exec.INITIAL);
		} else if (exertion.isJob()) {
            for (int i = 0; i < ((Job) exertion).size(); i++) {
                this.resetExertionStatus(((Job) exertion).getValue(i));
            }
        }
    }
*/

    public Task doFidelityTask(Task task) throws ServiceException {
        ServiceFidelity tf = (ServiceFidelity)task.getSelectedFidelity();
        task.correctBatchSignatures();
        task.startExecTime();
        // append context from Contexters
        if (task.getApdProcessSignatures().size() > 0) {
            Context cxt = apdProcess(task);
            cxt.setRoutine(task);
            task.setContext(cxt);
        }
        // do preprocessing
        if (task.getPreprocessSignatures().size() > 0) {
            Context cxt = preprocess(task);
            cxt.setRoutine(task);
            task.setContext(cxt);
        }
        // exert service task
		ServiceFidelity ts = new ServiceFidelity(task.getName());
        Signature tsig = task.getProcessSignature();
        ((ServiceContext)task.getContext()).getDomainStrategy().setCurrentSelector(tsig.getSelector());
        ((ServiceContext)task.getContext()).setCurrentPrefix(tsig.getPrefix());

        ts.getSelects().add(tsig);
        task.setSelectedFidelity(ts);
        ts.setSelect(tsig);
        if (tsig.getContextReturn() != null)
            ((ServiceContext)task.getContext()).setContextReturn(tsig.getContextReturn());

        task = task.doTask();
        if (task.getStatus() <= Exec.FAILED) {
            task.stopExecTime();
            RoutineException ex = new RoutineException("Batch service task failed: "
                    + task.getName());
            task.reportException(ex);
            task.setStatus(Exec.FAILED);
            task.setSelectedFidelity(tf);
            return task;
        }
        // reverse fidelity
        task.setSelectedFidelity(tf);
        // do postprocessing
        if (task.getPostprocessSignatures().size() > 0) {
            Context cxt = postprocess(task);
            cxt.setRoutine(task);
            task.setContext(cxt);
        }
        if (task.getStatus() <= Exec.FAILED) {
            task.stopExecTime();
            RoutineException ex = new RoutineException("Batch service task failed: "
                    + task.getName());
            task.reportException(ex);
            task.setStatus(Exec.FAILED);
            task.setSelectedFidelity(tf);
            return task;
        }
        task.setSelectedFidelity(tf);
        task.stopExecTime();
        return task;
    }

    private Context apdProcess(Task task) throws RoutineException, ContextException {
        return processContinousely(task, task.getApdProcessSignatures());
    }

    private Context preprocess(Task task) throws RoutineException, ContextException {
        return processContinousely(task, task.getPreprocessSignatures());
    }

    private Context postprocess(Task task) throws RoutineException, ContextException {
        return processContinousely(task, task.getPostprocessSignatures());
    }

    public Context processContinousely(Task task, List<Signature> signatures)
            throws RoutineException, ContextException {
        Signature.Type type = signatures.get(0).getExecType();
        Task t;
        Context shared = task.getContext();
        for (int i = 0; i < signatures.size(); i++) {
            try {
                t = task(task.getName() + "-" + i, signatures.get(i), shared);
                signatures.get(i).setType(Signature.SRV);
                ((ServiceContext)shared).getDomainStrategy().setCurrentSelector(signatures.get(i).getSelector());
                ((ServiceContext)shared).setCurrentPrefix(signatures.get(i).getPrefix());

                ServiceFidelity tmp = new ServiceFidelity();
                tmp.getSelects().add(signatures.get(i));
                t.setSelectedFidelity(tmp);
                t.setContinous(true);
                t.setContext(shared);

                logger.info("Sending one of the batch tasks to exert: " + t.getName() + " " + t.getSelectedFidelity());
                t = t.doTask();
                signatures.get(i).setType(type);
                shared = t.getContext();
                if (t.getStatus() <= Exec.FAILED) {
                    task.setStatus(Exec.FAILED);
                    RoutineException ne = new RoutineException(
                            "Batch signature failed: " + signatures.get(i));
                    task.reportException(ne);
                    task.setContext(shared);
                    return shared;
                }
            } catch (Exception e) {
                logger.error("Error", e);
                task.setStatus(Exec.FAILED);
                task.reportException(e);
                task.setContext(shared);
                return shared;
            }
        }
        // return the service context of the last exertion
        return shared;
    }
}
