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

import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.Dispatcher;
import sorcer.core.exertion.NetTask;
import sorcer.core.provider.*;
import sorcer.core.signature.RemoteSignature;
import sorcer.core.signature.ServiceSignature;
import sorcer.service.*;

import java.rmi.RemoteException;
import java.util.Set;

import static sorcer.service.Exec.*;

@SuppressWarnings("rawtypes")

abstract public class CatalogExertDispatcher extends ExertDispatcher {
    private final Logger logger = LoggerFactory.getLogger(CatalogExertDispatcher.class);

    public CatalogExertDispatcher(Routine job,
                                  Set<Context> sharedContext,
                                  boolean isSpawned,
                                  Exerter provider,
                                  ProvisionManager provisionManager) {
        super(job, sharedContext, isSpawned, provider, provisionManager);
    }

    protected Routine execExertion(Routine ex, Arg... args) throws SignatureException,
            RoutineException {
        beforeExec(ex);
        // setValue subject before task goes out.
        // ex.setSubject(subject);
        Subroutine result = null;
        try {
            if (ex.isTask()) {
                result = execTask((Task) ex, args);
            } else if (ex.isJob()) {
                result = execJob((Job) ex, args);
            } else if (ex.isBlock()) {
                result = execBlock((Block) ex, args);
            } else {
                logger.warn("Unknown Subroutine: {}", ex);
            }
            afterExec(ex, result);
            // setValue subject after result is received
            // result.setSubject(subject);
            result.setStatus(DONE);
        } catch (Exception e) {
            logger.warn("Error while executing exertion: ", e);
            // return original exertion with exception
            result = (Subroutine) ex;
            result.reportException(e);
            result.setStatus(FAILED);
            setState(Exec.FAILED);
            return result;
        }
        return result;
    }

    protected void afterExec(Routine ex, Routine result)
            throws SignatureException, RoutineException, ContextException {
        Subroutine ser = (Subroutine) result;
		((Transroutine)xrt).setMogramAt(result, ((ServiceMogram)ex).getIndex());
        if (ser.getStatus() > FAILED && ser.getStatus() != SUSPENDED) {
            ser.setStatus(DONE);
            // update all outputs from sharedcontext only for tasks. For jobs,
            // spawned governor does it.
			try {
				if (result.isTask()) {
					collectOutputs(result);
				}
				//notifyExertionExecution(ex, result);
			} catch (ContextException e) {
				throw new RoutineException(e);
			}
        }
        afterExec(result);
    }

    protected Task execTask(Task task, Arg... args) throws ServiceException,
            SignatureException, RemoteException {
		if (task instanceof NetTask) {
			return execServiceTask(task, args);
        } else {
            return task.doTask(args);
        }
    }

    protected Task execServiceTask(Task task, Arg... args) throws RoutineException {
        logger.info("Starting execServiceTask for: " + task.getName());
		Task result = null;
        int maxTries = 6;
        int tried=0;
        try {
            if (((RemoteSignature) task.getProcessSignature()).getService()!=null) {
                logger.info("\n*** service is set in signature testing if it is the same provider ***\n");
                try {
                    if (((RemoteSignature) task.getProcessSignature()).getService().equals(provider)) {
                        logger.info("\n*** getting result from delegate of "
                                + provider.getProviderName() + "... ***\n");
                        result = ((ServiceExerter) provider).getDelegate().doTask(
                                task, null);
                        result.getControlContext().appendTrace(
                                (provider.getProviderName() != null ? provider.getProviderName() + " " : "")
                                        + "executed task: " + task.getName() + " governor: " + getClass().getName());
                        return result;
                    }
                } catch (RemoteException re) {
                    logger.warn("Got exception trying to run using the provider in the signature: " + re.getMessage());
                    re.printStackTrace();
                }
            }

            RemoteSignature sig = (RemoteSignature) task.getProcessSignature();
            // Catalog lookup or use Lookup Service for the particular
            // service
            Service service = (Service) Accessor.get().getService(sig);
            /*if (service == null && task.isProvisionable()) {
                MonitoringSession monSession = MonitorUtil.getMonitoringSession(task);
                if (task.isMonitorable() && monSession!=null) {
                    logger.debug("Notifying monitor about Provisioning of exertion: " + task.getName());
                    try {
                        monSession.changed(task.getContext(), task.getControlContext(), Exec.State.PROVISION.ordinal());
                    } catch (Exception ce) {
                        logger.warn("Unable to notify monitor about Provisioning of exertion: " + task.getName() + " " + ce.getMessage());
                    }
                }
                try {
                    logger.info("Provisioning "+sig);
                    service = ServiceDirectoryProvisioner.getProvisioner().provision(sig);
                } catch (ProvisioningException pe) {
                    Throwable rootCause = pe;
                    while (rootCause.getCause()!=null && rootCause.getCause()!=rootCause) {
                        rootCause = rootCause.getCause();
                    }
                    String msg = "Problem provisioning " + sig + " in task " + task.getName() + ": " +rootCause.getMessage();
                    logger.warn(msg);
                    throw new RoutineException(msg, task);
                }
            }*/
            if (service == null) {
                String msg;
                // getValue the PROCESS Method and grab provider key + interface
                msg = "No Provider Available\n" + "Provider Tag:      "
                        + sig.getProviderName() + "\n"
                        + "Provider Interface: " + sig.getServiceType();

                logger.info(msg);
                throw new RoutineException(msg, task);
            } else {
                tried=0;
                while (result==null && tried < maxTries) {
                    tried++;
                    try {
                        // setTaskProvider(task, provider.getProviderName());
                        task.setService(service);
                        // client security
                        /*
                         * ClientSubject cs = null;
                         * try{ // //cs =
                         * (ClientSubject)ServerContext.getServerContextElement
                         * (ClientSubject.class); }catch (Exception ex){
                         * Util.debug(this, ">>>No Subject in the server prc");
                         * cs=null; } Subject client = null; if(cs!=null){
                         * client=cs.getClientSubject(); Util.debug(this,
                         * "Abhijit::>>>>> CS was not null"); if(client!=null){
                         * Util.debug(this,"Abhijit::>>>>> Client Subject was not
                         * null"+client); }else{ Util.debug(this,"Abhijit::>>>>>>
                         * CLIENT SUBJECT WAS
                         * NULL!!"); } }else{ Util.debug(this, "OOPS! NULL CS"); }
                         * if(client!=null&&task.getPrincipal()!=null){
                         * Util.debug(this,"Abhijit:: >>>>>--------------Inside
                         * Client!=null, PRINCIPAL != NULL, subject="+client);
                         * result = (RemoteServiceTask)provider.service(task);
                         * }else{ Util.debug(this,"Abhijit::
                         * >>>>>--------------Inside null Subject"); result =
                         * (RemoteServiceTask)provider.service(task); }
                         */
                        logger.debug("getting result from provider...");
                        result = ((Exertion)service).exert(task, null);

                    } catch (Exception re) {
                        if (tried >= maxTries) {
                            logger.error("+++++++++++++++Problem exerting task, already tried " + tried + " times for: " + xrt.getName() + " " + re.getMessage());
                            throw re;
                        }
                        else {
                            logger.info("Problem exerting task, retrying " + tried + " time: " + xrt.getName() + " " + re.getMessage());
                            service = (Service) Accessor.get().getService(sig);
                            try {
                                logger.info("+++++++++++++++Got service: " + ((Exerter)service).getProviderID());
                            } catch (Exception e) {
                                System.out.println("+++++++++++++++The service we got is not valid");
                            }
                        }
                    }
                }
                if (result!=null)
                    result.getControlContext().appendTrace(
                            (provider != null ? provider.getProviderName() + " " : "") + "governor: "
                                        + getClass().getName());
            }
            logger.debug("got result: {}", result);
        } catch (Exception re) {
            logger.error("+++++++++++++++Dispatch failed for task, tried: " + tried + " : "
                    + xrt.getName());
            task.setStatus(State.FAILED.ordinal());

            /*if (task.isMonitorable()) {
                MonitoringSession mSession = MonitorUtil.getMonitoringSession(task);
                if (mSession!=null){
                    try {
                        mSession.changed(task.getContext(), task.getControlContext(), Exec.FAILED);
                    } catch (Exception ce) {
                        logger.error("Problem reporting failed state of task: " + task.getName());
                    }
                }
            } */
            task.reportException(re);
            throw new RoutineException("Dispatch failed for task, tried: " + tried + " : "
                    + xrt.getName(), re);
        }
        return result;
    }

    protected Job execJob(Job job, Arg ... args)
            throws DispatchException, InterruptedException,
            RemoteException {

        runningExertionIDs.add(job.getId());

        // create a new instance of a governor
        Dispatcher dispatcher = MogramDispatcherFactory.getFactory()
                .createDispatcher(job, sharedContexts, true, provider);
        dispatcher.exec(args);
        // wait until serviceJob is done by governor
        Job out = (Job) dispatcher.getResult().exertion;
        // Not sure if good place
        out.stopExecTime();
        out.getControlContext().appendTrace((provider.getProviderName() != null ?
                provider.getProviderName() + " " : "")  + "executed job: " + job.getName()
                + " governor: " + getClass().getName());
        return out;
    }

	private Block execBlock(Block block, Arg... args)
			throws MogramException {
		try {
		    if (((ServiceSignature)block.getProcessSignature()).isRemote()) {
                ServiceTemplate st = new ServiceTemplate(null, new Class[]{Concatenator.class}, null);
                ServiceItem[] concatenators = Accessor.get().getServiceItems(st, null);
			/*
			 * check if there is any available concatenator in the network and
			 * delegate the inner block to the available Concatenator. In the future, a
			 * efficient load balancing algorithm should be implemented for
			 * dispatching inner jobs. Currently, it only does round robin.
			 */
                for (int i = 0; i < concatenators.length; i++) {
                    if (concatenators[i] != null) {
                        if (!provider.getProviderID().equals(concatenators[i].serviceID)) {
                            logger.trace("Concatenator: [{}] ServiceID: {}", i, concatenators[i].serviceID);
                            Exerter rconcatenator = (Exerter) concatenators[i].service;
                            return rconcatenator.exert(block, null);
                        }
                    }
                }
            }

			/*
			 * Create a new governor thread for the inner job, if no available
			 * Jobber is found in the network
			 */
			Dispatcher dispatcher;
			runningExertionIDs.add(block.getId());

			// create a new instance of a governor
			dispatcher = MogramDispatcherFactory.getFactory()
					.createDispatcher(block, sharedContexts, true, provider);

            for (Contextion mog : block.getMograms()) {
                if (((ServiceMogram)mog).getDataContext() != null && ((ServiceMogram)mog).getDataContext().getScope() == null) {
                    if (block.getContext() != null) {
                        ((ServiceMogram)mog).getDataContext().setScope(block.getContext());
                    }
                }
            }
            dispatcher.exec(args);
			// wait until a block is done by governor
			Block out = (Block) dispatcher.getResult().exertion;
            out.getControlContext().appendTrace((provider.getProviderName() != null
                    ? provider.getProviderName() + " " : "")
                    + "executed block: " +  block.getName() + " governor: " + getClass().getName());
			return out;
		} catch (RemoteException | DispatchException | ServiceException ex) {
			throw new MogramException(ex);
		}
	}
}
