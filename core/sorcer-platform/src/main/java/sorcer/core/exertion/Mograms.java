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

package sorcer.core.exertion;

import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import sorcer.core.SorcerConstants;
import sorcer.core.context.ControlContext;
import sorcer.core.context.ServiceContext;
import sorcer.service.*;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;

import java.rmi.RemoteException;
import java.util.*;

public class Mograms implements SorcerConstants {

	private Mograms() {
		// Utility class
	}
	
	public static boolean isCatalogSingleton(Job job) {
		ControlContext cc = job.getControlContext();
		return job.size() == 1
				&& Access.PUSH.equals(cc.get(ControlContext.EXERTION_ACCESS));
	}

	public static boolean isCatalogParallel(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.PAR.equals(cc.get(ControlContext.EXERTION_FLOW))
				&& Access.PUSH.equals(cc.get(ControlContext.EXERTION_ACCESS));
	}

	public static boolean isCatalogSequential(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.SEQ.equals(cc.get(ControlContext.EXERTION_FLOW))
				&& Access.PUSH.equals(cc.get(ControlContext.EXERTION_ACCESS));
	}

	public static boolean isCatalogBlock(Routine exertion) {
		ControlContext cc = (ControlContext)exertion.getControlContext();
		return exertion instanceof Block
				&& Access.PUSH.equals(cc.get(ControlContext.EXERTION_ACCESS));
	}

	public static boolean isSpaceBlock(Routine exertion) {
		ControlContext cc = (ControlContext)exertion.getControlContext();
		return exertion instanceof Block
				&& Access.PULL.equals(cc.get(ControlContext.EXERTION_ACCESS));
	}
	
	public static boolean isSWIFSequential(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.SEQ.equals(cc.get(ControlContext.EXERTION_FLOW))
				&& Access.SWIF.equals(cc.get(ControlContext.EXERTION_ACCESS));
	}

	public static boolean isSpaceSequential(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.SEQ.equals(cc.get(ControlContext.EXERTION_FLOW))
				&& Access.PULL.equals(cc.get(ControlContext.EXERTION_ACCESS));
	}

	public static boolean isSpaceParallel(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.PAR.equals(cc.get(ControlContext.EXERTION_FLOW))
				&& Access.PULL.equals(cc.get(ControlContext.EXERTION_ACCESS));
	}
	
	public static boolean isSpaceSingleton(Job job) {
		ControlContext cc = job.getControlContext();
		return job.size() == 1
				&& Access.PULL.equals(cc.get(ControlContext.EXERTION_ACCESS));
	}

	public static boolean isParallel(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.PAR.equals(cc.get(ControlContext.EXERTION_FLOW));
	}

	public static boolean isSequential(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.SEQ.equals(cc.get(ControlContext.EXERTION_FLOW));
	}

	public static boolean isMonitorable(Job job) {
		ControlContext cc = job.getControlContext();
		return cc.isMonitorable();
	}

	public static List<Contextion> getInputExertions(Job job) throws ContextException {
		if (job == null || job.size() == 0)
			return null;
		List<Contextion> exertions = new ArrayList();
		Routine master = job.getMasterExertion();
		for (int i = 0; i < job.size(); i++)
			if (!(job.get(i).equals(master) || job
					.getControlContext().isSkipped(job.get(i))))
				exertions.add(job.get(i));
		return exertions;
	}

	public static ControlContext getCC(Context sc) {
		ControlContext cc = new ControlContext();
		for (Enumeration e = ((Hashtable) sc).keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			cc.put(key, ((Hashtable) sc).get(key));
		}
		cc.setName(sc.getName());
		cc.setId(((ServiceMogram)sc).getId());
		cc.setParentPath(sc.getParentPath());
		cc.setParentId((((ServiceMogram)sc).getParentId() == null) ? null : ((ServiceMogram)sc).getParentId());
		cc.setSubject(sc.getSubjectPath(), sc.getSubjectValue());
		cc.setLastUpdateDate(((ServiceMogram)sc).getLastUpdateDate());
		cc.setDescription(((ServiceMogram)sc).getDescription());
		cc.setOwnerId(((ServiceMogram)sc).getOwnerId());
		cc.setSubjectId(((ServiceMogram)sc).getSubjectId());
		cc.setProjectName(((ServiceMogram)sc).getProjectName());
		cc.setAccessClass(sc.getAccessClass());
		cc.isExportControlled(((ServiceMogram)sc).isExportControlled());
		cc.setGoodUntilDate(((ServiceMogram)sc).getGoodUntilDate());
		cc.setDomainId(((ServiceMogram)sc).getDomainId());
		cc.setSubdomainId(((ServiceMogram)sc).getSubdomainId());
		try {
			cc.setDomainName(sc.getDomainName());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		cc.setMetacontext(sc.getMetacontext());
		cc.isPersistantTaskAssociated = ((ServiceContext) sc).isPersistantTaskAssociated;
		return cc;
	}

	public static void removeExceptions(Job job) throws ContextException {
		removeExceptions(job.getContext());
		for (int i = 0; i < job.size(); i++) {
			if (job.get(i).isJob())
				removeExceptions((Job) job.get(i));
			else
				removeExceptions(job.get(i).getContext());
		}
	}

	public static void removeExceptions(Context sc) {
		Iterator e = ((ServiceContext) sc).keyIterator();
		while (e.hasNext()) {
			String path = (String) e.next();
			if (path.startsWith(SorcerConstants.EXCEPTIONS))
				try {
					sc.removePath(path);
				} catch (Exception ex) {
					// do nothing
				}
		}
		// sc.removeAttribute(SORCER.EXCEPTIONS);
	}

	public static void replaceNullIDs(Routine ex) throws ContextException {
		if (ex == null)
			return;
		if (ex.isJob()) {
			Job job = (Job) ex;
			if (job.getId() == null)
				job.setId(getId());
			if (job.getContext().getId() == null)
				((ServiceMogram)job.getContext()).setId(getId());
			for (int i = 0; i < job.size(); i++)
				replaceNullIDs(job.get(i));
		} else
			replaceNullIDs((Subroutine) ex);
	}

	public static void replaceNullIDs(Subroutine task) throws ContextException {
		if (task.getId() == null)
			task.setId(getId());
		if (task.getContext() != null) {
			if (task.getContext().getId() == null)
				((ServiceMogram)task.getContext()).setId(getId());
		}
	}

	public static synchronized Uuid getId() {
		 return UuidFactory.generate();
	}
	
	public static synchronized String getID() {
		try {
			Thread.sleep(1);
		} catch (InterruptedException ie) {
			// do Nothing
		}
		return Long.toHexString(new Date().getTime());
	}

	public static ExertionEnvelop getEntryEnvelop(Routine ex)
			throws RoutineException {
		if (ex == null)
			return null;
		else if (ex.getProcessSignature() != null)
			throw new RoutineException("No Method For Routine e=" + ex);

		ExertionEnvelop eenv = ExertionEnvelop.getTemplate();
		eenv.serviceType = ex.getProcessSignature().getServiceType();
		eenv.providerName = ex.getProcessSignature().getProviderName().getName();
		eenv.exertion = ex;
		eenv.exertionID = ((ServiceMogram)ex).getId();
		eenv.isJob = ex.isJob();
		eenv.state = Exec.INITIAL;
		return eenv;
	}

	public static List<Context> getTaskContexts(Routine ex) throws ContextException {
		List<Context> v = new ArrayList<>();
		collectTaskContexts(ex, v);
		return v;
	}

	// For Recursion
	private static void collectTaskContexts(Routine exertion, List<Context> contexts) throws ContextException {
		try {
			if (exertion.isConditional())
				contexts.add(exertion.getDataContext());
			else if (exertion instanceof Job) {
				contexts.add(exertion.getDataContext());
				for (int i = 0; i < exertion.getMograms().size(); i++)
					collectTaskContexts(((Job) exertion).get(i),
						contexts);
			} else if (exertion instanceof Task || exertion instanceof Block) {
				contexts.add(exertion.getDataContext());
			}
		} catch (RemoteException e) {
			throw new ContextException(e);
		}
	}
}
