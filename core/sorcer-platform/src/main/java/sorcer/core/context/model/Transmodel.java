package sorcer.core.context.model;

import net.jini.core.transaction.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.co.tuple.ExecDependency;
import sorcer.core.context.ContextList;
import sorcer.core.context.ModelStrategy;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.ent.Entry;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.core.context.model.req.RequestModel;
import sorcer.core.plexus.FidelityManager;
import sorcer.service.*;
import sorcer.service.modeling.Exploration;
import sorcer.service.modeling.Finalization;
import sorcer.service.modeling.Functionality;
import sorcer.service.modeling.Model;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static sorcer.mo.operator.result;

/**
 * Created by Mike Sobolewski on 11/29/2020.
 */
public interface Transmodel extends Model, Transdomain, Configurable {

    public Paths getChildrenPaths();

    public void setChildrenPaths(Paths childrenPaths);

    public void addChildren(List<Contextion> domains)
        throws SignatureException, RemoteException;

    public Map<String, Context> getChildrenContexts();

    public void setChildrenContexts(Map<String, Context> childrenContexts);

    public void addChildrenContexts(ContextList componentContexts);

    public void execDependencies(String path, Context inContext, Arg... args)
        throws ServiceException, RemoteException;
}
