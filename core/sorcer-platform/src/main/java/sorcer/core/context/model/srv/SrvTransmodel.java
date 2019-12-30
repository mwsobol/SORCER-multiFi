package sorcer.core.context.model.srv;

import net.jini.core.transaction.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.co.tuple.ExecDependency;
import sorcer.core.context.ModelStrategy;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.EntModel;
import sorcer.core.context.model.ent.Entry;
import sorcer.core.plexus.FidelityManager;
import sorcer.service.*;
import sorcer.service.modeling.Functionality;
import sorcer.service.modeling.Transmodel;

import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by Mike Sobolewski on 12/28/2019.
 */
public class SrvTransmodel extends SrvModel implements Transmodel {

    private static final Logger logger = LoggerFactory.getLogger(SrvTransmodel.class);

    protected Map<String, Domain> children = new HashMap<>();

    protected Paths childrenPaths;

    protected FidelityManager collabFiManager;

    public SrvTransmodel() {
        super();
        type = Functionality.Type.TRANS;
    }

    public SrvTransmodel(String name) {
        super(name);
        type = Functionality.Type.TRANS;
    }

    public static SrvTransmodel instance(Signature builder) throws SignatureException {
        SrvTransmodel model = SrvTransmodel.instance(null, builder);
        model.setEvaluated(false);
        return model;
    }

    public static SrvTransmodel instance(String name, Signature builder) throws SignatureException {
        SrvTransmodel model = (SrvTransmodel) sorcer.co.operator.instance(builder);
        model.setBuilder(builder);
        if (name != null) {
            model.setName(name);
        }
        model.setEvaluated(false);
        return model;
    }

    public SrvTransmodel(String name, List<SrvTransmodel> models) {
        super(name);
        for (SrvTransmodel vm : models)
            children.put(vm.getName(), vm);
    }

    public SrvTransmodel(String name, SrvTransmodel... models) {
        super(name);
        for (SrvTransmodel vm : models)
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

    public FidelityManager getCollabFiManager() {
        return collabFiManager;
    }

    public void setCollabFiManager(FidelityManager collabFiManager) {
        this.collabFiManager = collabFiManager;
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

    public Context analyze(Context modelContext, Arg... args) throws EvaluationException {
        try {
            setMdaFi(modelContext);
            if (mdaFi == null || mdaFi.getSelect() == null) {
                throw new EvaluationException("No MDA specified in the context");
            }
        } catch (ContextException | ConfigurationException e) {
            throw new EvaluationException(e);
        }
        ((ServiceContext)modelContext).getMogramStrategy().setExecState(Exec.State.NULL);
        Context out = evaluate(modelContext, args);
        return out;
    }

    @Override
    synchronized public Context evaluate(Context inContext, Arg... args) throws EvaluationException {
        if (inContext == null) {
            inContext = new ServiceContext(key);
        }
        ServiceContext context = (ServiceContext) inContext;
        ServiceContext out = (ServiceContext) mogramStrategy.getOutcome();
        try {
            if (context.get(Context.MDA_PATH) != null) {
                return analyze(context, args);
            }

            Exec.State state = context.getMogramStrategy().getExecState();
            // set mda if available
            if (mdaFi == null) {
                setMdaFi(context);
            }

            if (mdaFi != null && state.equals(State.INITIAL)) {
                context.remove(Context.PRED_PATH);
//                    modelContext.remove(Context.MDA_PATH);
                context.getMogramStrategy().setExecState(State.RUNNING);
                // select mda Fi if provided
                List<Fidelity> fis = Arg.selectFidelities(args);
                for (Fi fi : fis) {
                    if (mdaFi.getName().equalsIgnoreCase(fi.getPath())) {
                        mdaFi.selectSelect(fi.getName());
                    }
                }
                logger.info("*** mdaFi: {}", mdaFi.getSelect().getName());
                mdaFi.getSelect().analyze(this, context);
                logger.info("=======> MDA DONE: " + context.getOutputs());
                context.getMogramStrategy().setExecState(State.DONE);
            }
            state = context.getMogramStrategy().getExecState();

            if (mdaFi == null || state.equals(State.DONE)) {
                if (mdaFi == null) {
                    execDependencies(key, context, args);
                }
                out = (ServiceContext) super.evaluate(context, args);
                out.setType(Functionality.Type.TRANS);
                if (mdaFi == null) {
                    // collect all domain results
//                    out.put(key, context);
                    for (String mn : children.keySet()) {
                        out.put(mn, children.get(mn).getOutput());
                    }
                }
            } else {
                execDependencies(key, context, args);
                super.evaluate(context, args);
                // collect all domain snapshots
                out = new ServiceContext(key);
                // remove predicted values
                out.put(key, context);
                for (String mn : children.keySet()) {
                    out.put(mn, ((ServiceContext) children.get(mn)).getResult());
                }
            }
        } catch (ServiceException | TransactionException | ConfigurationException | RemoteException e) {
            throw new EvaluationException(e);
        }
        return out;
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
                            execDependencies(p.getName(), inContext, args);
                            ServiceContext snapshot = null;
                            if (mdaFi != null) {
                                snapshot = (ServiceContext) inContext;
                            } else {
                                snapshot = new ServiceContext(key + ":" + p.path);
                            }
                            Context cxt = null;
                            if (children.get(p.path) instanceof EntModel) {
                                EntModel mdl = (EntModel) children.get(p.path);
                                mdl.evaluate(snapshot, args);
                                cxt = mdl.getMogramStrategy().getOutcome();
                                append(cxt);
                            } else {
                                Mogram xrt = children.get(p.getName());
                                xrt.setScope(this);
                                Mogram out = xrt.exert(args);
                                if (xrt instanceof Job) {
                                    cxt = ((Job)out).getJobContext();
                                } else if (xrt instanceof Routine) {
                                    cxt = out.getDataContext();
                                }
                                logger.info("exertion domain context: " + cxt);
                                Context.Return rp = out.getProcessSignature().getContextReturn();
                                if (rp != null && rp.outPaths != null && rp.outPaths.size() > 0) {
                                    cxt = cxt.getDirectionalSubcontext(rp.outPaths);
                                    if (rp.outPaths.getName().equals(rp.outPaths.get(0).getName())) {
                                        append(cxt);
                                    } else {
                                        put(rp.outPaths.getName(), cxt);
                                    }
                                } else {
                                    append(cxt);
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
                        } else {
                            // other dependencies
                            dpl = de.getData();
                            if (dpl != null && dpl.size() > 0) {
                                for (Path p : dpl) {
                                    Domain domain = this;
                                    if (p.info != null) {
                                        if (children.get(p.info.toString()) instanceof Domain) {
                                            Domain dmn = children.get(p.info.toString());
                                            if (dmn != null) {
                                                domain = dmn;
                                            }
                                        }
                                    }
                                    Entry v = (Entry) domain.get(p.getName());
                                    if (v!= null) {
                                        v.getValue(args);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
