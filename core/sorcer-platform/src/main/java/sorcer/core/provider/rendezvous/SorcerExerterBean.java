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
package sorcer.core.provider.rendezvous;

import net.jini.core.lookup.ServiceID;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.id.UuidFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.context.Contexts;
import sorcer.core.exertion.ObjectBlock;
import sorcer.core.exertion.ObjectJob;
import sorcer.core.provider.*;
import sorcer.service.*;
import sorcer.service.modeling.Model;
import sorcer.service.modeling.ModelingTask;
import sorcer.util.Sorcer;
import sorcer.util.SorcerUtil;

import javax.security.auth.Subject;
import java.rmi.RemoteException;
import java.util.Vector;

/**
 * ServiceBean - The SORCER superclass of service components of ServiceExerter.
 * 
 * @author Mike Sobolewski
 */
abstract public class SorcerExerterBean implements Exertion, ServiceBean {
	private Logger logger = LoggerFactory.getLogger(SorcerExerterBean.class.getName());

	protected ServiceExerter provider;

	protected ProviderDelegate delegate;
	
	//protected TaskManager threadManager;
	
	public SorcerExerterBean() throws RemoteException {
		// do nothing
	}
	
	public void init(Exerter provider) {
		this.provider = (ServiceExerter)provider;
		this.delegate = ((ServiceExerter)provider).getDelegate();
		//this.threadManager = ((ServiceExerter)provider).getThreadManager();

	}

    public String getProviderName() throws RemoteException {
        return provider.getProviderName();
	}
	
	/** {@inheritDoc} */
	public boolean isAuthorized(Subject subject, Signature signature) {
		return true;
	}
	
	protected void replaceNullExertionIDs(Routine ex) {
		if (ex != null && ((Subroutine) ex).getId() == null) {
			((ServiceMogram)ex).setId(UuidFactory.generate());
			if (ex.isJob()) {
				for (int i = 0; i < ((Job) ex).size(); i++)
					replaceNullExertionIDs(((Job) ex).get(i));
			}
		}
	}

	protected void notifyViaEmail(Routine ex) throws ContextException {
		if (ex == null || ex.isTask())
			return;
		Job job = (Job) ex;
		Vector recipents = null;
		String notifyees = job.getControlContext().getNotifyList();
		if (notifyees != null) {
			String[] list = SorcerUtil.tokenize(notifyees, ",");
			recipents = new Vector(list.length);
			for (int i = 0; i < list.length; i++)
				recipents.addElement(list[i]);
		}
		String to = "", admin = Sorcer.getProperty("sorcer.admin");
		if (recipents == null) {
			if (admin != null) {
				recipents = new Vector();
				recipents.addElement(admin);
			}
		} else if (admin != null && !recipents.contains(admin))
			recipents.addElement(admin);

		if (recipents == null)
			to = to + "No e-mail notifications will be sent for this job.";
		else {
			to = to + "e-mail notification will be sent to\n";
			for (int i = 0; i < recipents.size(); i++)
				to = to + "  " + recipents.elementAt(i) + "\n";
		}
		String comment = "Your job '" + job.getName()
				+ "' has been submitted.\n" + to;
		job.getControlContext().setFeedback(comment);
		if (job.getMasterExertion() != null
				&& job.getMasterExertion().isTask()) {
			job.getMasterExertion().getContext()
					.putValue(Context.JOB_COMMENTS, comment);

			Contexts.markOut((job.getMasterExertion()).getContext(), Context.JOB_COMMENTS);

		}
	}
	
    public void setServiceID(Mogram ex) {
        if (provider == null) {
            provider = new ServiceExerter();
            init (provider);
        }
        ServiceID id = provider.getProviderID();
        if (id != null) {
            logger.trace(id.getLeastSignificantBits() + ":"
                          + id.getMostSignificantBits());
            ((ServiceMogram) ex).setLsbId(id.getLeastSignificantBits());
            ((ServiceMogram) ex).setMsbId(id.getMostSignificantBits());
        }
    }

    private String getDataURL(String filename) {
        return delegate.getProviderConfig().getProperty(
				"provider.dataURL")
				+ filename;
	}

	private String getDataFilename(String filename) {
		return delegate.getProviderConfig().getDataDir() + "/"
				+ filename;
	}

	/* (non-Javadoc)
	 * @see sorcer.core.provider.ServiceBean#service(sorcer.service.Routine, net.jini.core.transaction.Transaction)
	 */
	public Contextion exert(Contextion exertion, Transaction transaction, Arg... args) throws ContextException, RemoteException {
		Mogram out = null;
		Mogram mogram = (Mogram)exertion;
		try {

			setServiceID(mogram);
			((ServiceMogram)mogram).appendTrace("mogram: " + mogram.getName() + " rendezvous: " +
					(provider != null ? provider.getProviderName() + " " : "")
					+ this.getClass().getName());
            if (mogram instanceof ObjectJob || mogram instanceof ObjectBlock
					|| mogram instanceof Model || mogram instanceof ModelingTask) {
				logger.info("{} is a local exertion", mogram.getName());
				out = localExert(mogram, transaction, args);
			} else {
                logger.info("{} is a remote exertion", mogram.getName());
				out = getControlFlownManager(mogram).process();
			}

			if (mogram instanceof Routine)
				((ServiceMogram)mogram).getDataContext().setRoutine(null);
        }
		catch (Exception e) {
			logger.debug("exert failed for: " + mogram.getName(), e);
			throw new ContextException(e);
		}
		return out;
	}

	protected ControlFlowManager getControlFlownManager(Mogram exertion) throws RoutineException {
        try {
            if (exertion instanceof Routine) {
                if (exertion.isMonitorable())
                    return new MonitoringControlFlowManager((Routine)exertion, delegate, this);
                else
                    return new ControlFlowManager((Routine)exertion, delegate, this);
            }
            else
                return null;
        } catch (Exception e) {
            ((Task) exertion).reportException(e);
            throw new RoutineException(e);
        }
    }

	abstract public Mogram localExert(Mogram mogram, Transaction txn, Arg... args)
			throws TransactionException, ContextException, RemoteException;

	@Override
	public Object execute(Arg... args) throws ContextException, RemoteException {
		Mogram mog = Arg.selectMogram(args);
		if (mog != null)
			try {
				return exert(mog, null, args);
			} catch (Exception e) {
				throw new ContextException(e);
			}
		else
			return null;
	}

}
