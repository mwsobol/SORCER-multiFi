package sorcer.service;

import net.jini.core.transaction.Transaction;
import sorcer.core.context.ServiceContext;
import sorcer.service.modeling.Functionality;

import java.rmi.RemoteException;
import java.util.List;

public class FreeContextion implements FreeService, Contextion, Arg {

	private String name;

	private Contextion contextion;

	protected Functionality.Type type = Functionality.Type.CONTEXTION;

	public FreeContextion(String name) {
		this.name = name;
	}

	public FreeContextion(String name, Functionality.Type type) {
		this(name);
		this.type = type;
	}

	public Contextion getContextion() {
		return contextion;
	}

	public void setContextion(Contextion contextion) {
		this.contextion = contextion;
	}

	@Override
	public ServiceContext evaluate(Context context, Arg... args) throws EvaluationException, RemoteException {
		return null;
	}

	@Override
	public <T extends Contextion> T exert(Transaction txn, Arg... args) throws ContextException, RemoteException {
		return null;
	}

	@Override
	public Context getContext() throws ContextException {
		return null;
	}

	@Override
	public Context getOutput(Arg... args) throws ContextException {
		 if (contextion != null) {
			 return contextion.getOutput(args);
		} else {
		 	return null;
		 }
	}

	@Override
	public void setContext(Context input) throws ContextException {

	}

	@Override
	public Context appendContext(Context context) throws ContextException, RemoteException {
		return null;
	}

	@Override
	public Context getContext(Context contextTemplate) throws RemoteException, ContextException {
		return null;
	}

	@Override
	public Context appendContext(Context context, String path) throws ContextException, RemoteException {
		return null;
	}

	@Override
	public Context getContext(String path) throws ContextException, RemoteException {
		return null;
	}

	@Override
	public Context.Return getContextReturn() {
		return null;
	}

	@Override
	public MogramStrategy getMogramStrategy() {
		return null;
	}

	@Override
	public List<Contextion> getContextions(List<Contextion> contextionList) {
		if (contextion != null) {
			return contextion.getContextions(contextionList);
		} else {
			return contextionList;
		}
	}

	@Override
	public void selectFidelity(Fidelity fi) throws ConfigurationException {

	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Fi getMultiFi() {
		return null;
	}

	@Override
	public Morpher getMorpher() {
		return null;
	}

	@Override
	public Object getId() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object execute(Arg... args) throws ServiceException, RemoteException {
		if (contextion != null) {
			return contextion.execute(args);
		} else {
			throw new ServiceException("contextion not bind yet");
		}
	}

	public Functionality.Type getType() {
		return type;
	}

	public void setType(Functionality.Type type) {
		this.type = type;
	}

	@Override
	public Context getScope() {
		return null;
	}

	@Override
	public void setScope(Context scope) {

	}

	@Override
	public void bind(Object object) {
		contextion = (Contextion)object;
	}
}
