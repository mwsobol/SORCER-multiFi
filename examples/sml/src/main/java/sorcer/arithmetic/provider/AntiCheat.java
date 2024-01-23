package sorcer.arithmetic.provider;

import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.MonitorException;
import java.rmi.RemoteException;

@FunctionalInterface
public interface AntiCheat {
	
	public Context checkPlayer(Context context) throws RemoteException, ContextException, MonitorException;
}
