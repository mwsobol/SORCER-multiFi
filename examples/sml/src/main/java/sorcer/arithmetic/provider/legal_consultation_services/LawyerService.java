package sorcer.arithmetic.provider.legal_consultation_services;

import sorcer.service.Context;
import sorcer.service.ContextException;
import java.rmi.RemoteException;

public interface LawyerService {

    Context createLawyerProfile(Context context) throws RemoteException, ContextException;

    Context updateLawyerProfile(Context context) throws RemoteException, ContextException;

    Context getLawyerAvailability(Context context) throws RemoteException, ContextException;
}
