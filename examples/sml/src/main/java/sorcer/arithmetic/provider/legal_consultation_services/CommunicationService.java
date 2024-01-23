package sorcer.arithmetic.provider.legal_consultation_services;

import sorcer.service.Context;
import sorcer.service.ContextException;
import java.rmi.RemoteException;

public interface CommunicationService {

    Context initiateChat(Context context) throws RemoteException, ContextException;

    Context startVideoConference(Context context) throws RemoteException, ContextException;

    Context shareDocument(Context context) throws RemoteException, ContextException;
}