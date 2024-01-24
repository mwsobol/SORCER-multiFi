package sorcer.arithmetic.provider;

public interface ServerHosting {
    public Context hostServer(Context context) throws RemoteException, ContextException, MonitorException;
}