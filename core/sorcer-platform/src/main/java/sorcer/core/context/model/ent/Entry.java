package sorcer.core.context.model.ent;

import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.context.ContextSelection;
import sorcer.core.context.ServiceContext;
import sorcer.service.*;
import sorcer.service.modeling.*;
import sorcer.util.bdb.objects.UuidObject;
import sorcer.util.url.sos.SdbUtil;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entry<V> extends MultiFiSlot<String, V>
        implements Identifiable, ElementaryRequest, Evaluation<V>, Activity, Callable<V>, Setter, Reactive<V>, ent<V> {

    private static final long serialVersionUID = 1L;

    protected static Logger logger = LoggerFactory.getLogger(Entry.class.getName());

    protected Uuid id = UuidFactory.generate();

    protected ArgSet args = new ArgSet();

    protected String domain;

    protected boolean negative;

    // its arguments is persisted
    protected boolean isPersistent = false;

    // if reactive then its values are evaluated if active (either Evaluation or Invocation multitype)
    protected boolean isReactive = false;

    protected ContextSelection contextSelector;

    private Path.State state;

    protected Fidelity<Path> multiFiPath;

    protected Map<String, Entry> subvalueMap;

    protected String supername;

    public Entry() {
    }

    public Entry(String key) {
        this.key = key;
    }

    public Entry(String key, Object item) {
       super(key, item);
        if (sorcer.util.url.sos.SdbUtil.isSosURL(item)) {
            isPersistent = true;
        }
        if (item != null && item.getClass().getName().indexOf("Lambda") > 0) {
            type = Functionality.Type.LAMBDA;
        } else {
            type = Functionality.Type.ENT;
        }
    }

    @Override
    public Uuid getId() {
        return id;
    }

    public Object selectImpl() {
        if (!isValid && multiFi != null) {
            Object select = multiFi.getSelect();
            if (select instanceof Entry) {
                Object selectImpl = ((Entry) multiFi.getSelect()).getImpl();
                if (selectImpl != null) {
                    impl = (V) ((Entry) multiFi.getSelect()).getImpl();
                }
            } else {
                impl = (V) multiFi.getSelect();
            }
        }
        isValid = true;
        return impl;
    }

    public Object applyFidelity() {
        if (multiFi != null && multiFi.isChanged()) {
            impl = (V) multiFi.getSelect();;
        }
        return impl;
    }

    public ArgSet getArgs() {
        return args;
    }

    public void setArgs(ArgSet args) {
        this.args = args;
    }

    @Override
    public void setValue(Object value) throws SetterException {
        if (isPersistent) {
            try {
                if (SdbUtil.isSosURL(value)) {
                    this.out = (V) value;
                } else if (SdbUtil.isSosURL(this.impl)) {
                    if (((URL) this.impl).getRef() == null) {
                        this.impl = (V) SdbUtil.store(value);
                    } else {
                        SdbUtil.update((URL) this.impl, value);
                    }
                }
            } catch (ServiceException | SignatureException | RemoteException e) {
                throw new SetterException(e);
            }
        } else {
            this.out = (V) value;
        }
        isValid = true;
        isChanged = true;
    }

    public void setId(Uuid id) {
        this.id = id;
    }

    public void setContextSelector(ContextSelection contextSelector) {
        this.contextSelector = contextSelector;
    }

    public V getData(Arg... args) throws ContextException {
        if (out != null) {
            if (multiFi == null && isValid) {
                return out;
            } else if (!multiFi.isChanged() && isValid) {
                return out;
            }
        } else {
            out = getValue(args);
            isValid = true;
        }
        return out;
    }

    @Override
    public V getValue(Arg... args) throws ContextException {
        if (multiFi != null && multiFi.isChanged()) {
            impl = (V) multiFi.getSelect();
            multiFi.setChanged(false);
        }
        if (impl instanceof Entry && ((Entry) impl).getKey().equals(key)) {
            out = (V) ((Entry) impl).getData(args);
            isValid = true;
            return out;
        }
        Object val = impl;
        URL url = null;
        try {
            substitute(args);
            if (isPersistent) {
                if (SdbUtil.isSosURL(val)) {
                    val = (V) ((URL) val).getContent();
                    if (val instanceof UuidObject)
                        val = (V) ((UuidObject) val).getObject();
                } else {
                    if (val instanceof UuidObject) {
                        url = SdbUtil.store(val);
                    } else {
                        UuidObject uo = new UuidObject(val);
                        uo.setName(key);
                        url = SdbUtil.store(uo);
                    }
                    impl = (V) url;
                    out = null;
                }
                return (V) val;
            } else if (val instanceof Invocation) {
                Context cxt = (Context) Arg.selectDomain(args);
                out = (V) ((Invocation) val).invoke(cxt, args);
            } else if (val instanceof Evaluation) {
                if (val instanceof Entry && ((Entry)val).getName().equals(key)) {
                    out = (V) ((Entry)val).getValue(args);
                } else {
                    out = ((Evaluation<V>) val).evaluate(args);
                }
            } else if (val instanceof Valuation) {
                out = (V) ((Valuation) val).valuate();
            } else if (val instanceof Ref) {
                Object deref = ((Ref)val).getValue();
                if (deref instanceof  Evaluation) {
                    if (deref instanceof Scopable) {
                        ((Scopable)deref).setScope(((Ref)val).getScope());
                    }
                    out = (V) ((Evaluation)deref).evaluate(args);
                } else {
                    out = (V) ((Entry)deref).getValue(args);
                }
            } else if (val instanceof ServiceFidelity) {
                // return the selected fidelity of this entry
                for (Arg arg : args) {
                    if (arg instanceof Fidelity) {
                        if (((Fidelity)arg).getPath().equals(key)) {
                            ((ServiceFidelity)val).selectSelect(arg.getName());
                            break;
                        }
                    }
                }
                out = (V) ((Entry)((ServiceFidelity) val).getSelect()).getValue(args);
            } else if (val instanceof Callable) {
                out = (V) ((Callable)val).call(args);
            } else if (val instanceof Service) {
                out = (V) ((Service)val).execute(args);
            } else {
                // impl is just the out
                // it is recommended to set out and impl to the same valuate
                // when the impl is implementation of the out valuate
                if (out == null && impl != null) {
                    out = (V) impl;
                }
            }
        } catch (Exception e) {
            throw new ContextException(e);
        }
        if (contextSelector != null && out instanceof Context) {
            try {
                out = (V) contextSelector.doSelect(val);
            } catch (ContextException e) {
                throw new ContextException(e);
            }
        }
        if (out instanceof Number && negative) {
            Number result = (Number) val;
            Double rd = result.doubleValue() * -1;
            out = (V) rd;
        }
        return (V) out;
    }

    @Override
    public boolean isReactive() {
        return isReactive;
    }

    public Entry<V> setReactive(boolean isReactive) {
        this.isReactive = isReactive;
        return this;
    }

    public boolean isPersistent() {
        return isPersistent;
    }

    public void setPersistent(boolean isPersistent) {
        this.isPersistent = isPersistent;
    }

    public boolean isNegative() {
        return negative;
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    @Override
    public Entry act(Arg... args) throws ServiceException, RemoteException {
        Object result = this.execute(args);
        if (result instanceof Entry) {
            return (Entry)result;
        } else {
            return new Entry(key, result);
        }
    }

    @Override
    public Data act(String entryName, Arg... args) throws ServiceException, RemoteException {
        Object result = this.execute(args);
        if (result instanceof Entry) {
            return (Entry)result;
        } else {
            return new Entry(entryName, result);
        }
    }

    public Object execute(Arg... args) throws ServiceException, RemoteException {
        ContextDomain cxt = Arg.selectDomain(args);
        if (cxt != null) {
            // entry substitution
            ((ServiceContext)cxt).putValue(key, impl);
            return cxt;
        } else {
            return impl;
        }
    }

    @Override
    public V call(Arg... args) throws EvaluationException {
        try {
            return getData(args);
        } catch (ContextException e) {
            throw new EvaluationException(e);
        }
    }

    public void substitute(Arg... entries) throws SetterException {
        if (entries != null) {
            for (Arg a : entries) {
                if (a instanceof ContextSelection) {
                    setContextSelector((ContextSelection) a);
                }
            }
        }
    }

    @Override
    public String getName() {
        return key;
    }

    public String getDomainName() {
        if (domain != null) {
            return key + "$" + domain;
        } else {
            return key;
        }
    }

    public String getProperName() {
        int i = key.indexOf('$');
        if (i > 0) {
            return key.substring(0, i);
        } else {
            return key;
        }
    }

    static public String getProperName(String name) {
        int i = name.indexOf('$');
        if (i > 0) {
            return name.substring(0, i);
        } else {
            return name;
        }
    }

    static public String[] getProperNames(String[] names) {
        String[] properNames =  new String[names.length];
        for (int i = 0; i < names.length; i++) {
            properNames[0] = getProperName(names[i]);
        }
        return properNames;
    }

    public List<String> getProperNames(List<String> names) {
        List<String> properNames =  new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            properNames.add(getProperName(names.get(i)));
        }
        return properNames;
    }

    public void setName(String name) {
        key = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getKey() {
        return key;
    }

    public Path.State getState() {
        return state;
    }

    public void setState(Path.State state) {
        this.state = state;
    }

    public V asis() {
        if (impl != null && impl instanceof Entry) {
            return (V) ((Entry) impl).asis();
        } else {
            return (V) impl;
        }
    }

    @Override
    public V evaluate(Arg... args) throws EvaluationException {
        try {
            Object result;
            if (multiFi == null && impl == null) {
                Context context = (Context) Arg.selectDomain(args);
                if (context != null) {
                    Object inCxt = context.getValue(key);
                    if (inCxt != null) {
                        out = (V) inCxt;
                        isValid = true;
                        isChanged = true;
                    }
                }
                return out;
            } else if (this instanceof Functionality) {
                result = (V) ((Functionality)this).evaluate(args);
            } else {
                result = this.getValue(args);
            }

            if (result instanceof Routine) {
                // we assume that exrtion is asked to be evaluated
                return (V) ((Routine) result).getContext();
            } else {
                return (V) result;
            }
        } catch (ContextException | RemoteException e) {
            throw new EvaluationException(e);
        }
    }

    public String getAtDomainName() {
        if (domain == null) {
            return key;
        }
        return key+"@"+domain;
    }

    public Entry appendSubvalues(Entry... subValues) {
        if (subvalueMap ==  null) {
            subvalueMap = new HashMap<>();
        }
        for (Entry ent : subValues) {
            subvalueMap.put(ent.getName(), ent);
        }
        return this;
    }

    public String getSupername() {
        return supername;
    }

    public void setSupername(String name) {
        supername = name;
    }

    public Map<String, Entry> getSubvalueMap() {
        return subvalueMap;
    }

    public Fidelity<Path> getMultiFiPath() {
        return multiFiPath;
    }

    public void setMultiFiPath(Fidelity<Path> multiFiPath) {
        this.multiFiPath = multiFiPath;
    }
}
