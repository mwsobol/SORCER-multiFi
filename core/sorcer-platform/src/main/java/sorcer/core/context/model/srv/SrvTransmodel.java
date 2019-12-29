package sorcer.core.context.model.srv;

import net.jini.core.transaction.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.co.tuple.ExecDependency;
import sorcer.core.context.ModelStrategy;
import sorcer.core.context.ServiceContext;
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
    }

    public SrvTransmodel(String name) {
        super(name);
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
        type = Functionality.Type.MADO;
        for (SrvTransmodel vm : models)
            children.put(vm.getName(), vm);
    }

    public SrvTransmodel(String name, SrvTransmodel... models) {
        super(name);
        type = Functionality.Type.MADO;
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
    synchronized public Context evaluate(Context modelContext, Arg... args) throws EvaluationException {
        ServiceContext context = (ServiceContext) modelContext;
        ServiceContext out = context;
        try {
            if (modelContext.get(Context.MDA_PATH) != null) {
                return analyze(modelContext, args);
            }

            Exec.State state = context.getMogramStrategy().getExecState();
            // set mda if available
            if (mdaFi == null) {
                setMdaFi(modelContext);
            }

            if (mdaFi != null && state.equals(State.INITIAL)) {
                modelContext.remove(Context.PRED_PATH);
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
                logger.info("=======> MDA DONE: " + modelContext.getOutputs());
                context.getMogramStrategy().setExecState(State.DONE);
            }
            state = context.getMogramStrategy().getExecState();

            if (mdaFi == null || state.equals(State.DONE)) {
                if (mdaFi == null) {
                    execDependencies(key, modelContext, args);
                }
                super.evaluate(context, args);
                if (mdaFi == null) {
                    // collect all domain snapshot
                    out = new ServiceContext(key);
                    out.setType(Functionality.Type.MADO);
                    out.put(key, context);
                    for (String mn : children.keySet()) {
                        out.put(mn, ((SrvModel) children.get(mn)).getResult());
                    }
                }
            } else {
                execDependencies(key, modelContext, args);
                super.evaluate(context, args);
                // collect all domain snapshots
                out = new ServiceContext(key);
                // remove predicted values
                out.setType(Functionality.Type.MADO);
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

    protected void execDependencies(String path, Context context, Arg... args) throws MogramException, RemoteException, TransactionException {
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
                            execDependencies(p.getName(), context, args);
                            ServiceContext snapshot = null;
                            if (mdaFi != null) {
                                snapshot = (ServiceContext) context;
                            } else {
                                snapshot = new ServiceContext(key + ":" + p.path);
                            }
                            if (children.get(p.path) instanceof SrvModel) {
                                SrvModel mdl =  (SrvModel) children.get(p.path);
                                mdl.evaluate(snapshot, args);
                            } else {
                                Mogram xrt = children.get(p.getName());
                                xrt.setScope(this);
                                Subroutine out = xrt.exert(args);
                                Context cxt = null;
                                if (xrt instanceof Job) {
                                    cxt = ((Job)out).getJobContext();
                                } else {
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
                                    SrvModel domain = this;
                                    if (p.info != null) {
                                        if (children.get(p.info.toString()) instanceof SrvModel) {
                                            SrvModel vm = ((SrvModel)children.get(p.info.toString()));
                                            if (vm != null) {
                                                domain = vm;
                                            }
                                        }
                                    }
                                    Srv v = (Srv) domain.getEntry(p.getName());
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
