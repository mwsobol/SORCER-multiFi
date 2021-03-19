package sorcer.core.service;

import sorcer.core.context.ModelStrategy;
import sorcer.service.*;

import java.rmi.RemoteException;
import java.util.List;

public interface Region extends Transdiscipline {

    public Supervision getSupervisor();

    public void setSupervisor(Supervision supervisor);

    public Context evaluate(Context context, Arg... args) throws ServiceException;

}
