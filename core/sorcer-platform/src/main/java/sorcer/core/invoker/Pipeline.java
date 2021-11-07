package sorcer.core.invoker;

import net.jini.core.transaction.Transaction;
import sorcer.core.context.PositionalContext;
import sorcer.core.context.ServiceContext;
import sorcer.service.*;
import sorcer.service.modeling.Functionality;
import sorcer.service.modeling.cxtn;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Pipeline extends ServiceInvoker<Context> implements Contextion, cxtn {

    private Context newInvokeContext;

    private boolean isNewInput = false;

    private List<Opservice> opservices = new ArrayList<>();

    protected Projection inPathProjection;

    protected Projection outPathProjection;

    public Pipeline(Opservice... opservices) {
        this(null, opservices);
    }

    public Pipeline(String name, Context data, Opservice... opservices) {
        super(name);
        if (data != null) {
            invokeContext = data;
        }
        for (Opservice eval : opservices) {
            this.opservices.add(eval);
        }
    }

    public Pipeline(String name, Opservice... opservices) {
        this(name, null, opservices);
    }

    public Pipeline(List<Opservice> opservices) {
        this(null, opservices);
    }

    public Pipeline(String name, List<Opservice> Opseropservicesvice) {
        super(name);
        this.opservices = opservices;
    }

    /* (non-Javadoc)
	 * @see sorcer.service.Evaluation#execute(sorcer.service.Args[])
	 */
    @Override
    public Context evaluate(Arg... args) throws InvocationException {

        Context out = Arg.selectContext(args);
        if (out == null) {
            out = new PositionalContext(getClass().getSimpleName() + "-" + name);
        }

        Context returnContext = null;
        if (contextReturn != null && contextReturn.getDataContext() != null) {
            returnContext = contextReturn.getDataContext();
            try {
                out.append(returnContext);
            } catch (ContextException e) {
                throw new InvocationException(e);
            }
        } else {
            try {
                out.append(invokeContext);
            } catch (ContextException e) {
                throw new InvocationException(e);

            }
        }

        Object opout;
        for (Opservice opsrv : opservices) {
            try {
                if (opsrv instanceof Scopable) {
                    if (((Scopable) opsrv).getScope() == null) {
                        ((Scopable) opsrv).setScope(out);
                    } else {
                        ((Scopable) opsrv).getScope().append(out);

                    }
                }

                if (returnContext != null) {
                    if (((Scopable) opsrv).getScope() == null) {
                        ((Scopable) opsrv).setScope(returnContext);
                    } else {
                        ((Scopable) opsrv).getScope().append(returnContext);
                    }
                }

                if (opsrv instanceof Appender) {
                    Context appendContext = (Context) opsrv.execute();
                    if (((Appender) opsrv).isNew()) {
                        if (((Appender) opsrv).getType().equals(Appender.ContextType.INPUT)) {
                            newInvokeContext = appendContext;
                            isNewInput = true;
                        } else {
                            scope = appendContext;
                        }
                    } else {
                        if (((Appender) opsrv).getType().equals(Appender.ContextType.INPUT)) {
                            out.append(appendContext);
                        } else {
                            scope.append(appendContext);
                        }
                    }
                    continue;
                }

                if (opsrv instanceof Invocation) {
                    if (isNewInput) {
                        opout = ((Invocation) opsrv).invoke(newInvokeContext, args);
                    } else {
                        opout = ((Invocation) opsrv).invoke(out, args);
                    }
                } else if (opsrv instanceof Evaluation) {
                    opout = ((Evaluation) opsrv).evaluate(args);
                } else {
                    if (opsrv instanceof Signature) {
                        if (isNewInput) {
                            opout = opsrv.execute(newInvokeContext);
                        } else {
                            opout = opsrv.execute(out);
                        }
                    } else {
                        opout = opsrv.execute(args);
                    }
                }

                if (opout instanceof Context) {
                    out.append((Context)opout);
                    invokeContext.putValue(((Identifiable) opsrv).getName(), opout);
                } else {
                    ((ServiceContext) out).put(((Identifiable) opsrv).getName(), opout);
                    invokeContext.putValue(((Identifiable) opsrv).getName(), opout);
                }
            } catch (ServiceException | RemoteException e) {
                throw new InvocationException(e);
            }
        }
        return out;
    }

    public List<Opservice> getEvaluators() {
        return opservices;
    }

    public void setEvaluators(List<Opservice> opservices) {
        this.opservices = opservices;
    }


    @Override
    public Context evaluate(Context context, Arg... args) throws EvaluationException, RemoteException {
        invokeContext = context;
        return evaluate(args);
    }

    @Override
    public <T extends Contextion> T exert(Arg... args) throws ServiceException, RemoteException {
        return (T) evaluate(args);
    }

    @Override
    public String getDomainName() {
        return name;
    }

    @Override
    public Context invoke(Context context, Arg... args) throws EvaluationException, RemoteException {
        invokeContext = context;
        return evaluate(args);
    }

    @Override
    public Pipeline exert(Transaction txn, Arg... args) throws ContextException, RemoteException {
        Context cxt = Arg.selectContext(args);
        if (cxt != null) {
            evaluate(cxt, args);
        } else {
            evaluate(args);
        }
         return this;
    }

    @Override
    public Context getContext() throws ContextException {
        Context rpc = null;
        if (contextReturn != null) {
            rpc = contextReturn.getDataContext();
            if (rpc != null && rpc.size() > 0) {
                try {
                    invokeContext.appendContext(rpc);
                    contextReturn.setDataContext(null);
                } catch (RemoteException e) {
                    throw new ContextException(e);
                }
            }
        }
        return invokeContext;
    }

    @Override
    public Context getOutput(Arg... args) throws ContextException {
        return getResult();
    }

    @Override
    public void setContext(Context input) throws ContextException {
        invokeContext = input;
    }

    public Context getResult() throws ContextException {
        Context.Return rp = invokeContext.getContextReturn();
        if (rp == null) {
            rp = contextReturn;
        }
        Context out = null;
        if (rp != null) {
            if (rp.outPaths != null && rp.outPaths.size() > 0) {
                out = invokeContext.getDirectionalSubcontext(rp.outPaths);
                if (rp.outPaths.size() == 1) {
                    out.setReturnValue(((ServiceContext)invokeContext).get(rp.outPaths.get(0).getName()));
                } else {
                    out.setReturnValue(invokeContext);
                }
                ((ServiceContext)out).setFinalized(true);
            } else {
                out = invokeContext;
            }
        } else if (out != null && out.getScope() != null) {
            out.getScope().append(invokeContext);
        } else {
            out = invokeContext;
        }

        return out;
    }

    public Functionality.Type getDependencyType() {
        return Functionality.Type.PIPELINE;
    }

    @Override
    public Projection getInPathProjection() {
        return inPathProjection;
    }

    public void setInPathProjection(Projection inPathProjection) {
        this.inPathProjection = inPathProjection;
    }

    @Override
    public Projection getOutPathProjection() {
        return outPathProjection;
    }

    public void setOutPathProjection(Projection outPathProjection) {
        this.outPathProjection = outPathProjection;
    }

    @Override
    public Context appendContext(Context context) throws ContextException, RemoteException {
        return invokeContext.appendContext(context);
    }

    @Override
    public Context getContext(Context contextTemplate) throws RemoteException, ContextException {
        return null;
    }

    @Override
    public Context appendContext(Context context, String path) throws ContextException, RemoteException {
        return null;
    }

    @Override
    public Context getContext(String path) throws ContextException, RemoteException {
        return null;
    }

    @Override
    public boolean isExec() {
        return false;
    }

    @Override
    public ServiceStrategy getDomainStrategy() {
        return null;
    }

    @Override
    public List<Contextion> getContextions(List<Contextion> contextionList) {
        return contextionList;
    }

    @Override
    public FidelityManagement getFidelityManager() throws RemoteException {
        return null;
    }

    @Override
    public void selectFidelity(Fi fi) throws ConfigurationException {

    }
}
