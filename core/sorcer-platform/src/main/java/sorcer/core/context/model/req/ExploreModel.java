package sorcer.core.context.model.req;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.co.tuple.ExecDependency;
import sorcer.core.context.ContextList;
import sorcer.core.context.ModelStrategy;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.Transmodel;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.core.context.model.ent.Entry;
import sorcer.service.*;
import sorcer.service.modeling.Exploration;
import sorcer.service.modeling.Functionality;

import java.rmi.RemoteException;
import java.util.*;

import static sorcer.mo.operator.result;

/**
 * Created by Mike Sobolewski on 12/28/2019.
 */
public class ExploreModel extends RequestModel implements Transmodel, Configurable {

    private static final Logger logger = LoggerFactory.getLogger(Transmodel.class);

    protected Map<String, Contextion> children = new HashMap<>();

    protected Map<String, Context> childrenContexts = new HashMap<>();

    protected Paths childrenPaths;

    protected Fidelity<Analysis> analyzerFi;

    protected Fidelity<Exploration> explorerFi;

    public ExploreModel() {
        super();
        type = Functionality.Type.TRANS;
    }

    public ExploreModel(String name) {
        super(name);
        type = Functionality.Type.TRANS;
    }

    public static ExploreModel instance(Signature builder) throws SignatureException {
        ExploreModel model = ExploreModel.instance(null, builder);
        model.setEvaluated(false);
        return model;
    }

    public static ExploreModel instance(String name, Signature builder) throws SignatureException {
        ExploreModel model = (ExploreModel) sorcer.co.operator.instance(builder);
        model.setBuilder(builder);
        if (name != null) {
            model.setName(name);
        }
        model.setEvaluated(false);
        return model;
    }

    public ExploreModel(String name, List<Transmodel> models) {
        super(name);
        for (Transmodel vm : models)
            children.put(vm.getName(), vm);
    }

    public ExploreModel(String name, Transmodel... models) {
        super(name);
        for (Transmodel vm : models)
            children.put(vm.getName(), vm);
    }

    public Paths getChildrenPaths() {
        return childrenPaths;
    }

    public void setChildrenPaths(Paths childrenPaths) {
        this.childrenPaths = childrenPaths;
    }

    public void addChildren(List<Contextion> domains) throws RemoteException {
        for (Contextion vm : domains) {
            this.children.put(vm.getName(), vm);
            ((ServiceMogram)vm).setParent(this);
        }
    }

    @Override
    public Fidelity<Analysis> getAnalyzerFi() {
        return analyzerFi;
    }

    public void setAnalyzerFi(Fidelity<Analysis> analyzerFi) {
        this.analyzerFi = analyzerFi;
    }

    @Override
    public boolean configure(Object... configs) throws ConfigurationException, RemoteException {
        return false;
    }

    @Override
    public Map<String, Contextion> getChildren() {
        return children;
    }

    @Override
    public Contextion getChild(String name) {
        return children.get(name);
    }

    @Override
    synchronized public Context evaluate(Context inContext, Arg... args) throws EvaluationException {
        if (inContext == null) {
            inContext = new ServiceContext(key);
        }
        ServiceContext context = (ServiceContext) inContext;
        if (dataContext == null) {
            dataContext = new ServiceContext(key);
        }
        getDomainStrategy().setOutcome(dataContext);
        context.setScope(dataContext);
        try {
            // set mda if available
            analyzerFi = getAnalysisFi(context);

            execDependencies(key, context, args);
            // TODO why scope is not set?
            //setScope(dataContext);
            append(dataContext);
            Context evalOut = super.evaluate(inContext, args);
            dataContext.append(evalOut);
            // put results of component domains
            for (String mn : children.keySet()) {
                dataContext.put(mn, result((Mogram)children.get(mn)));
            }
            if (analyzerFi != null && analyzerFi.getSelect() != null) {
                dataContext.putValue(Functionality.Type.DOMAIN.toString(), key);
                analyzerFi.getSelect().analyze(this, dataContext);
            }
        } catch (ServiceException | AnalysisException | RemoteException e) {
            throw new EvaluationException(e);
        }
        return dataContext;
    }

    @Override
    public Fidelity<Exploration> getExplorerFi() {
        return explorerFi;
    }

    public void setExplorerFi(Fidelity<Exploration> explorerFi) {
        this.explorerFi = explorerFi;
    }

    public Map<String, Context> getChildrenContexts() {
        return childrenContexts;
    }

    public void setChildrenContexts(Map<String, Context> childrenContexts) {
        this.childrenContexts = childrenContexts;
    }

    public void addChildrenContexts(ContextList componentContexts) {
        if (childrenContexts == null) {
            childrenContexts = new HashMap();
        }
        for (Context cxt : componentContexts) {
            childrenContexts.put(cxt.getName(), cxt);
        }
        this.childrenContexts = childrenContexts;
    }

    @Override
    public Context analyze(Context modelContext, Arg... args) throws EvaluationException, RemoteException {
        try {
            out = modelContext;
            analyzerFi.getSelect().analyze(this, modelContext);
            return (Context) out;
        } catch (ServiceException | AnalysisException e) {
            throw new EvaluationException(e);
        }
    }

    @Override
    public Context explore(Context context, Arg... args) throws ContextException, RemoteException {
        return explorerFi.getSelect().explore(context);
    }

    @Override
    public Object get(String path$domain) {
        String path = null;
        String domain = null;
        if (path$domain.indexOf("$") > 0) {
            int ind = path$domain.indexOf("$");
            path = path$domain.substring(0, ind);
            domain = path$domain.substring(ind + 1);
            return ((ServiceMogram)getChild(domain)).get(path);
        } else if (path$domain != null){
            return data.get(path$domain);
        } else {
            return Context.none;
        }
    }

    public void execDependencies(String path, Context inContext, Arg... args) throws ServiceException, RemoteException {
        Map<String, List<ExecDependency>> dpm = ((ModelStrategy) domainStrategy).getDependentDomains();
        if (dpm != null && dpm.get(path) != null) {
            List<Path> dpl = null;
            List<ExecDependency> del = dpm.get(path);
            Entry entry = entry(path);
            if (del != null && del.size() > 0) {
                for (ExecDependency de : del) {
                    if (de.getName().equals(key)) {
                        dpl = de.getData();
                        for (Path p : dpl) {
                            Contextion domain = children.get(p.getName());
                            execDependencies(p.getName(), inContext, args);
                            Context cxt = null;
                            if (children.get(p.path) instanceof EntryModel) {
                                EntryModel mdl = (EntryModel) children.get(p.path);
                                mdl.evaluate(inContext, args);
                                cxt = mdl.getDomainStrategy().getOutcome();
                                dataContext.append(cxt);
                            } else {
                                domain.setScope(dataContext);
                                Domain child = null;
                                try {
                                    child = ((Mogram)domain).exert(args);
                                } catch (RemoteException e) {
                                    throw new ServiceException(e);
                                }
                                if (domain instanceof Job) {
                                    cxt = ((Job) child).getJobContext();
                                } else if (domain instanceof Routine) {
                                    cxt = ((ServiceMogram)child).getDataContext();
                                }
                                logger.info("exertion domain context: " + cxt);
                                Context.Return rp = ((ServiceMogram)child).getProcessSignature().getContextReturn();
                                if (rp != null && rp.outPaths != null && rp.outPaths.size() > 0) {
                                    cxt = cxt.getDirectionalSubcontext(rp.outPaths);
                                    if (rp.outPaths.getName().equals(rp.outPaths.get(0).getName())) {
                                        ((ServiceContext)child).append(cxt);
                                    } else {
                                        put(rp.outPaths.getName(), cxt);
                                    }
                                } else {
                                    dataContext.append(cxt);
                                }
                            }
                            if (analyzerFi != null && analyzerFi.getSelect() != null) {
                                cxt.getContext().putValue(Functionality.Type.DOMAIN.toString(), domain.getName());
                                try {
                                    analyzerFi.getSelect().analyze(domain, cxt);
                                } catch (AnalysisException | RemoteException e) {
                                   throw new ServiceException(e);
                                }
                            }
                        }
                    }
                    if (de.getType().equals(Functionality.Type.FIDELITY)
                        && ((Fidelity) entry.getMultiFi().getSelect()).getName().equals(((Fidelity) de.annotation()).getName())) {
                        dpl = de.getData();
                        if (dpl != null && dpl.size() > 0) {
                            for (Path p : dpl) {
                                getValue(p.path, args);
                            }
                        }
                    }
                }
            }
        }
    }

}
