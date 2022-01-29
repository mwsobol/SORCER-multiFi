/*
 * Distribution Statement
 * 
 * This computer software has been developed under sponsorship of the United States Air Force Research Lab. Any further
 * distribution or use by anyone or any data contained therein, unless otherwise specifically provided for,
 * is prohibited without the written approval of AFRL/RQVC-MSTC, 2210 8th Street Bldg 146, Room 218, WPAFB, OH  45433
 * 
 * Disclaimer
 * 
 * This material was prepared as an account of work sponsored by an agency of the United States Government. Neither
 * the United States Government nor the United States Air Force, nor any of their employees, makes any warranty,
 * express or implied, or assumes any legal liability or responsibility for the accuracy, completeness, or usefulness
 * of any information, apparatus, product, or process disclosed, or represents that its use would not infringe privately
 * owned rights.
 */
package sorcer.core.context;

import net.jini.core.transaction.Transaction;
import sorcer.core.signature.LocalSignature;
import sorcer.service.*;
import sorcer.service.modeling.Model;

import java.rmi.RemoteException;

/**
 *  The SORCER model task extending the basic task implementation {@link Task}.
 * 
 * @author Mike Sobolewski
 */
public class ModelTask extends Task {
	private static final long serialVersionUID = 1L;
	protected Model model;
    protected ContextSelection contextFilter;
	protected ServiceContext modelContext;

	public ModelTask() {
		// do nothing
	}

	public ModelTask(String name) {
		super(name);
	}

	public ModelTask(String name, Signature signature) {
		super(name);
		addSignature(signature);
	}

	public Task doTask(Transaction txn, Arg... args) throws ServiceException {
		try {
			if (model != null) {
				model =model.exert(txn, args);
			} else {
				super.doTask(args);
			}
		} catch (RemoteException e) {
			throw new EvaluationException(e);
		}
		return this;
	}

	protected Context createModelContext(ServiceContext context, Signature targetSignature) throws ContextException {
		if (contextFilter == null)
			return (Context) model;
		else
			return (Context) contextFilter.doSelect(model);
	}
	
	private  Object instance(LocalSignature signature) throws SignatureException {
		if (signature.getSelector() == null || signature.getSelector().equals("new"))
			return signature.newInstance();
		else
			return signature.initInstance();
	}
	
	public Model getModel() {
		return model;
	}
	
	public void setModel(Model model) {
		this.model = model;
	}

    public ContextSelection getContextFilter() {
        return contextFilter;
    }

    public void setContextFilter(ContextSelection contextFilter) {
        this.contextFilter = contextFilter;
    }

}
