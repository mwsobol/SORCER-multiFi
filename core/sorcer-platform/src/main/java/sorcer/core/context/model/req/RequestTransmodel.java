package sorcer.core.context.model.req;

import net.jini.core.transaction.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.co.tuple.ExecDependency;
import sorcer.core.context.ModelStrategy;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.core.context.model.ent.AnalysisEntry;
import sorcer.core.context.model.ent.Entry;
import sorcer.core.plexus.FidelityManager;
import sorcer.service.*;
import sorcer.service.modeling.Functionality;
import sorcer.service.modeling.Transmodel;

import java.rmi.RemoteException;
import java.util.*;

import static sorcer.mo.operator.result;

/**
 * Created by Mike Sobolewski on 12/28/2019.
 */
public class RequestTransmodel extends RequestModel implements Transmodel {

    private static final Logger logger = LoggerFactory.getLogger(RequestTransmodel.class);

    protected Map<String, Domain> children = new HashMap<>();

    protected Paths childrenPaths;

    protected Fidelity<Analysis> analyzerFi;

    public RequestTransmodel() {
        super();
        type = Functionality.Type.TRANS;
    }

    public RequestTransmodel(String name) {
        super(name);
        type = Functionality.Type.TRANS;
    }

    public static RequestTransmodel instance(Signature builder) throws SignatureException {
        RequestTransmodel model = RequestTransmodel.instance(null, builder);
        model.setEvaluated(false);
        return model;
    }

    public static RequestTransmodel instance(String name, Signature builder) throws SignatureException {
        RequestTransmodel model = (RequestTransmodel) sorcer.co.operator.instance(builder);
        model.setBuilder(builder);
        if (name != null) {
            model.setName(name);
        }
        model.setEvaluated(false);
        return model;
    }

    public RequestTransmodel(String name, List<RequestTransmodel> models) {
        super(name);
        for (RequestTransmodel vm : models)
            children.put(vm.getName(), vm);
    }

    public RequestTransmodel(String name, RequestTransmodel... models) {
        super(name);
        for (RequestTransmodel vm : models)
            children.put(vm.getName(), vm);
    }

    public Paths getChildrenPaths() {
        return childrenPaths;
    }

    public void setChildrenPaths(Paths childrenPaths) {
        this.childrenPaths = childrenPaths;
    }

    public void addDomains(List<Domain> domains) {
        for (Domain vm : domains) {
            this.children.put(vm.getName(), vm);
            vm.setParent(this);
        }
    }

    @Override
    public Domain getDomain(String domainName) {
        return children.get(domainName);
    }

    @Override
    public boolean configure(Object... configs) throws ConfigurationException, RemoteException {
        return false;
    }

    @Override
    public Map<String, Domain> getChildren() {
        return children;
    }

    @Override
    public Mogram getChild(String name) {
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
        getMogramStrategy().setOutcome(dataContext);
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
                dataContext.put(mn, result(children.get(mn)));
            }
            if (analyzerFi != null && analyzerFi.getSelect() != null) {
                dataContext.putValue(Functionality.Type.DOMAIN.toString(), key);
                analyzerFi.getSelect().analyze(this, dataContext);
            }
        } catch (ServiceException | TransactionException | ConfigurationException | RemoteException e) {
            throw new EvaluationException(e);
        }
        return dataContext;
    }

    public Fidelity<Analysis> getAnalyzerFi() {
        return analyzerFi;
    }

    public void setAnalyzerFi(Fidelity<Analysis> analyzerFi) {
        this.analyzerFi = analyzerFi;
    }

    @Override
    public Object get(String path$domain) {
        String path = null;
        String domain = null;
        if (path$domain.indexOf("$") > 0) {
            int ind = path$domain.indexOf("$");
            path = path$domain.substring(0, ind);
            domain = path$domain.substring(ind + 1);
            return getChild(domain).get(path);
        } else if (path$domain != null){
            return data.get(path$domain);
        } else {
            return Context.none;
        }
    }

    protected void execDependencies(String path, Context inContext, Arg... args) throws MogramException, RemoteException, TransactionException {
        Map<String, List<ExecDependency>> dpm = ((ModelStrategy) mogramStrategy).getDependentDomains();
        if (dpm != null && dpm.get(path) != null) {
            List<Path> dpl = null;
            List<ExecDependency> del = dpm.get(path);
            Entry entry = entry(path);
            if (del != null && del.size() > 0) {
                for (ExecDependency de : del) {
                    if (de.getName().equals(key)) {
                        dpl = de.getData();
                        for (Path p : dpl) {
                            Domain domain = children.get(p.getName());
                            execDependencies(p.getName(), inContext, args);
                            Context cxt = null;
                            if (children.get(p.path) instanceof EntryModel) {
                                EntryModel mdl = (EntryModel) children.get(p.path);
                                mdl.evaluate(inContext, args);
                                cxt = mdl.getMogramStrategy().getOutcome();
                                dataContext.append(cxt);
                            } else {
                                domain.setScope(dataContext);
                                Domain child = domain.exert(args);
                                if (domain instanceof Job) {
                                    cxt = ((Job) child).getJobContext();
                                } else if (domain instanceof Routine) {
                                    cxt = child.getDataContext();
                                }
                                logger.info("exertion domain context: " + cxt);
                                Context.Return rp = child.getProcessSignature().getContextReturn();
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
                                analyzerFi.getSelect().analyze(domain, cxt);
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
