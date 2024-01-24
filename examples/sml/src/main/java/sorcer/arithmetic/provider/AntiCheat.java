package sorcer.arithmetic.provider;

import java.rmi.RemoteException;
import sorcer.service.Context;
import sorcer.service.ContextException;

@FunctionalInterface
public interface AntiCheat {
	public Context checkPlayer(Context context) throws ContextException, RemoteException;
}
