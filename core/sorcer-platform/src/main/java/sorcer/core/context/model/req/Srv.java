package sorcer.core.context.model.req;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.co.tuple.MogramEntry;
import sorcer.co.tuple.SignatureEntry;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.ent.Entry;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.core.context.model.ent.Function;
import sorcer.core.plexus.MorphFidelity;
import sorcer.service.*;
import sorcer.service.ContextDomain;
import sorcer.service.modeling.Model;
import sorcer.service.modeling.Functionality;
import sorcer.service.modeling.Serviceableness;
import sorcer.service.modeling.func;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import static sorcer.eo.operator.task;

/**
 * A service function <code>srv</code> is a functional entry which is dependent on its encapsulated service.
 * Created by Mike Sobolewski
 */
public class Srv extends Function<Object> implements Serviceableness,
        Comparable<Object>, Reactive<Object>, Serializable, func<Object> {

    private static Logger logger = LoggerFactory.getLogger(Srv.class.getName());

    protected String name;

    protected Service service;

    protected String[] paths;

    protected Context.Return returnPath;

    public Srv(String name) {
        super(name);
        this.name = name;
        type = Functionality.Type.SRV;
    }

    public Srv(String name, String path, Service service, String[] paths) {
        key = path;
        impl = service;
        this.name = name;
        this.paths = paths;
        type = Functionality.Type.SRV;
    }

    public Srv(String name, String path, Client service) {
        key = path;
        impl = service;
        this.name = name;
        type = Functionality.Type.SRV;
    }


    public Srv(String name, Object value) {
        if(name == null)
            throw new IllegalArgumentException("key must not be null");
        this.key = name;
        this.name = name;
        if (value instanceof Fidelity) {
            multiFi = (Fidelity) value;
            this.impl = multiFi.get(0);
        } else {
            this.key = key;
            this.impl = value;
        }
        type = Functionality.Type.SRV;
    }

    public Srv(String name, Object value, String[] paths) {
        this(name, value);
        this.paths = paths;
        type = Functionality.Type.SRV;
    }

    public Srv(String name, Object value, Context.Return returnPath) {
        this(name, value);
        this.returnPath = returnPath;
    }

    public Srv(String name, String path, Object value, Context.Return returnPath) {
        super(path, value);
        this.returnPath = returnPath;
        type = Functionality.Type.SRV;
    }

    public Srv(String name, Object value, String path) {
        super(path, value);
        this.name = name;
    }

    public Srv(String name, Object value, String path, Type type) {
        this(name, value, path);
        this.type = type;
    }

    public Srv(String name, Model model, String path) {
        super(path, model);
        this.name = name;
        type = Functionality.Type.SRV;
    }

    @Override
    public Class<?> getValueType() {
        return null;
    }

    public String[] getPaths() {
        return paths;
    }

    @Override
    public void addArgs(ArgSet set) throws EvaluationException {

    }

    @Override
    public Object getArg(String varName) throws ArgException {
        return null;
    }

    @Override
    public boolean isValueCurrent() {
        return isValid;
    }

    @Override
    public void valueChanged(Object obj) throws EvaluationException {
        out = obj;
    }

    public Mogram exert(Mogram mogram) throws ServiceException {
        return exert(mogram, null);
    }

    @Override
    public void valueChanged() throws EvaluationException {
        isValid = false;
    }

    @Override
    public Object getPerturbedValue(String varName) throws EvaluationException {
        return null;
    }

    @Override
    public Object evaluate(Arg... args) throws EvaluationException {

        if (impl instanceof EntryCollable) {
            Entry entry = null;
            try {
                entry = ((EntryCollable) impl).call((Model) scope);
            } catch (ServiceException e) {
                throw new EvaluationException(e);
            }
            out = entry.asis();
            return out;
        }
        
        if (impl instanceof Invocation) {
            return super.evaluate(args);
        } else if (out != null && isValid) {
            return out;
        } else if (multiFi != null) {
            impl = multiFi.getSelect();
        }

        try {
            substitute(args);
            if (impl instanceof Callable) {
                return ((Callable) impl).call();
            } else if (impl instanceof SignatureEntry) {
                if (scope != null && scope instanceof RequestModel) {
                    try {
                        return ((RequestModel) scope).evalSignature((Signature) ((SignatureEntry) impl).getImpl(), getKey());
                    } catch (Exception e) {
                        throw new EvaluationException(e);
                    }
                } else if (((SignatureEntry) impl).getContext() != null) {
                    try {
                        return execSignature((Signature) ((SignatureEntry) impl).getImpl(),
                                ((SignatureEntry) impl).getContext());
                    } catch (MogramException e) {
                        throw new EvaluationException(e);
                    }
                }
                throw new EvaluationException("No model available for entry: " + this);
            } else if (impl instanceof MogramEntry) {
                Context cxt = ((MogramEntry) impl).getValue().exert(args).getContext();
                Object val = cxt.getValue(Context.RETURN);
                if (val != null) {
                    return val;
                } else {
                    return cxt;
                }
            } else if (impl instanceof MorphFidelity) {
                return execMorphFidelity((MorphFidelity) impl, args);
            } else {
                return super.evaluate(args);
            }
        } catch (Exception e) {
            throw new EvaluationException(e);
        }
    }

    private Object execMorphFidelity(MorphFidelity mFi, Arg... entries) throws ServiceException {
        Object obj = mFi.getSelect();
        Object out;
        if (obj instanceof Scopable) {
            ((Scopable)obj).setScope(scope);
            setValid(false);
        }
        try {
            if (obj instanceof Function) {
                out = ((Function) obj).evaluate(entries);
            } else if (obj instanceof Mogram) {
                Context cxt = ((Mogram) obj).exert(entries).getContext();
                Object val = cxt.getValue(Context.RETURN);
                if (val != null) {
                    return val;
                } else {
                    return cxt;
                }
            } else if (obj instanceof Service) {
                out = ((Service) obj).execute(entries);
            } else {
                return obj;
            }
            mFi.setChanged();
            mFi.notifyObservers(out);
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
        return out;
    }

    public Object execSignature(Signature sig, Context scope) throws MogramException {
        Context.Return rp = (Context.Return) sig.getContextReturn();
        Context.Out ops = null;
        if (rp != null) {
            ops = ((Context.Return) sig.getContextReturn()).outPaths;
        }
        Context incxt = scope;
        if (sig.getContextReturn() != null) {
            incxt.setContextReturn(sig.getContextReturn());
        }
        Context outcxt;
        try {
            outcxt = task(sig, incxt).exert().getContext();
            if (ops != null && ops.size() > 0) {
                return outcxt.getDirectionalSubcontext(ops);
            } else if (sig.getContextReturn() != null) {
                return outcxt.getReturnValue();
            }
        } catch (Exception e) {
            throw new MogramException(e);
        }
        if (contextSelector != null) {
            try {
                return contextSelector.doSelect(outcxt);
            } catch (ContextException e) {
                throw new EvaluationException(e);
            }
        } else
            return outcxt;
    }

    @Override
    public Object execute(Arg... args) throws ServiceException, RemoteException {
        ContextDomain mod = Arg.selectDomain(args);
        if (mod != null) {
            if (mod instanceof EntryModel && impl instanceof ValueCallable) {
                try {
                    return ((ValueCallable) impl).call((Context) mod);
                } catch (RemoteException e) {
                    throw new ServiceException(e);
                }
            } else if (mod instanceof Context && impl instanceof SignatureEntry) {
                return ((ServiceContext) mod).execSignature((Signature) ((SignatureEntry) impl).getImpl(), args);
            } else {
                impl = mod;
                return evaluate(args);
            }
        } else {
            return getValue(args);
        }
    }
    
    public Context.Return getReturnPath() {
        return returnPath;
    }

    public void setReturnPath(Context.Return returnPath) {
        this.returnPath = returnPath;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getPath() {
        return key;
    }

    @Override
    public double getPerturbation() {
        return 0;
    }

    @Override
    public Service getService() throws ContextException {
        return service;
    }
    @Override
    public void setService(Service service) {
        this.service = service;
    }
}
