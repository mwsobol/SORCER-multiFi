package sorcer.arithmetic.provider.legal_consultation_services;

import sorcer.service.Context;
import sorcer.service.ContextException;
import java.rmi.RemoteException;

public interface ClientService {

    Context createClientProfile(Context context) throws RemoteException, ContextException;

    Context updateClientProfile(Context context) throws RemoteException, ContextException;

    Context getClientConsultationHistory(Context context) throws RemoteException, ContextException;
}
