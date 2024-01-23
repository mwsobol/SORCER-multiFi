package sorcer.arithmetic.provider.legal_consultation_services;

import sorcer.service.Context;
import sorcer.service.ContextException;
import java.rmi.RemoteException;

public interface SchedulingService {

    Context scheduleAppointment(Context context) throws RemoteException, ContextException;

    Context rescheduleAppointment(Context context) throws RemoteException, ContextException;

    Context cancelAppointment(Context context) throws RemoteException, ContextException;
}