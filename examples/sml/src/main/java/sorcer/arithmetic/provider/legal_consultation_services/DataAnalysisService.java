package sorcer.arithmetic.provider.legal_consultation_services;

import sorcer.service.Context;
import sorcer.service.ContextException;
import java.rmi.RemoteException;

public interface DataAnalysisService {

    Context performHistoricalAnalysis(Context context) throws RemoteException, ContextException;

    Context conductSensitivityAnalysis(Context context) throws RemoteException, ContextException;
}