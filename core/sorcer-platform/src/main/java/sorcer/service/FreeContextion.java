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

	protected Projection inPathProjection;

	protected Projection outPathProjection;

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
	public Context evaluate(Context context, Arg... args) throws ServiceException, RemoteException {
		return contextion.evaluate(context, args);
	}

	@Override
	public <T extends Contextion> T exert(Arg... args) throws ServiceException, RemoteException {
		return contextion.exert(args);
	}

	@Override
	public <T extends Contextion> T exert(Transaction txn, Arg... args) throws ServiceException, RemoteException {
		return contextion.exert(txn, args);
	}

	@Override
	public String getDomainName() {
		return name;
	}

	@Override
	public Context getContext() throws ContextException {
		return null;
	}

	@Override
	public Context getOutput(Arg... args) throws ContextException, RemoteException {
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
	public Context appendContext(Context context) throws ContextException {
		return null;
	}

	@Override
	public Context getDomainData() throws ContextException {
		return null;
	}

	@Override
	public Context getContext(Context contextTemplate) throws ContextException {
		return null;
	}

	@Override
	public Context appendContext(Context context, String path) throws ContextException {
		return null;
	}

	@Override
	public Context getContext(String path) throws ContextException {
		return null;
	}

	@Override
	public boolean isExec() {
		return false;
	}

	@Override
	public Context.Return getContextReturn() {
		return null;
	}

	@Override
	public ServiceStrategy getDomainStrategy() {
		return null;
	}

	@Override
	public List<Contextion> getContextions(List<Contextion> contextionList) throws RemoteException {
		if (contextion != null) {
			return contextion.getContextions(contextionList);
		} else {
			return contextionList;
		}
	}

	@Override
	public FidelityManagement getFidelityManager() throws RemoteException {
		return null;
	}

	@Override
	public void selectFidelity(Fi fi) throws ConfigurationException {

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
	public Object execute(Arg... args) throws ServiceException {
		if (contextion != null) {
			try {
				return contextion.execute(args);
			} catch (RemoteException e) {
				throw new ServiceException(e);
			}
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
	public Projection getInPathProjection() {
		return inPathProjection;
	}

	public void setInPathProjection(Projection inPathProjection) {
		this.inPathProjection = inPathProjection;
	}

	@Override
	public Projection getOutPathProjection() {
		return outPathProjection;
	}

	public void setOutPathProjection(Projection outPathProjection) {
		this.outPathProjection = outPathProjection;
	}

	@Override
	public void bind(Object object) {
		contextion = (Contextion)object;
	}
}
