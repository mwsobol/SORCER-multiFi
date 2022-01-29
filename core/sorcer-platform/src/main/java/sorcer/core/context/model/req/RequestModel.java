/*
 * Copyright 2015 the original author or authors.
 * Copyright 2015 SorcerSoft.org.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sorcer.core.context.model.req;

import groovy.lang.Closure;
import net.jini.core.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.co.tuple.MogramEntry;
import sorcer.co.tuple.SignatureEntry;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.core.context.model.ent.*;
import sorcer.core.invoker.ServiceInvoker;
import sorcer.core.plexus.FidelityManager;
import sorcer.core.plexus.MorphFidelity;
import sorcer.core.plexus.MorphMogram;
import sorcer.core.provider.rendezvous.ServiceModeler;
import sorcer.service.Projection;
import sorcer.core.signature.ServiceSignature;
import sorcer.eo.operator;
import sorcer.service.*;
import sorcer.service.modeling.Model;
import sorcer.service.modeling.Functionality;

import java.rmi.RemoteException;
import java.util.*;

import static sorcer.eo.operator.*;
import static sorcer.so.operator.execMogram;

/**
 * A ContextDomain is a schematic description or representation of something, especially a system,
 * phenomenon, or service, that accounts for its properties and is used to study its characteristics.
 * Properties of a service model are represented by contextReturn of Context with values that depend
 * on other properties and can be evaluated as specified by ths model. Evaluations of the service 
 * model args of the Req multitype results in exerting a dynamic federation of services as specified by
 * these args. A rendezvous service provider orchestrating a choreography of the model
 * is a local or remote one specified by a service signature of the model.
 *   
 * Created by Mike Sobolewski on 1/29/15.
 */
public class RequestModel extends EntryModel implements Invocation<Object> {
    private static final Logger logger = LoggerFactory.getLogger(RequestModel.class);

    public static RequestModel instance(Signature builder) throws SignatureException {
        RequestModel model = (RequestModel) sorcer.co.operator.instance(builder);
        model.setBuilder(builder);
        return model;
    }

    public RequestModel() {
        super();
        key = SRV_MODEL;
        setSignature();
        setSubject("fxn/model", new Date());
        isRevaluable = true;
    }

    public RequestModel(String name) {
        super(name);
        setSignature();
        setSubject("requst/model", new Date());
        isRevaluable = true;
    }

    public RequestModel(Signature signature) {
        this();
        addSignature(signature);
    }

    public RequestModel(String name, Signature signature) {
        this(name);
        addSignature(signature);
    }

    private void setSignature() {
        subjectPath = "requst/model";
        try {
            subjectValue = sig("exert", ServiceModeler.class);
        } catch (SignatureException e) {
            // ignore it;
        }
    }

    private void setSignature(Signature signature) {
        setSignature(null, signature);
    }

    private void setSignature(String path, Signature signature) {
        if (path == null)
            subjectPath = "requst/model";
        else
            subjectPath = path;
        subjectValue = signature;
    }

    public boolean isBatch() {
        for (Object s : ((ServiceFidelity)multiFi.getSelect()).getSelects()) {
            if (s instanceof Signature && ((Signature)s).getExecType() != Signature.Type.PRO)
                return false;
        }
        return true;
    }

    @Override
    public Object getValue(String path, Arg... args) throws ContextException {
        return getReqValue(path, args);
    }

    // calls from VarModels to prc Req args of Vars
    public Object getReqValue(String path, Srv srv, Arg... args) throws ContextException {
        try {
            putValue(path, srv);
        } catch (ContextException e) {
            data.remove(path);
            throw e;
        }
        Object out = getReqValue(path, args);
        data.remove(path);
        return out;
    }

    // used as execute but renamed to alter polymorphic chaining
    public Object getReqValue(String path, Arg... args) throws EvaluationException {
        Object val = null;
        try {
            append(args);
            if (path != null) {
                execDependencies(path, args);
                val = get(path);
            } else {
                Context.Return rp = Arg.getReturnPath(args);
                if (rp != null)
                    val = getReturnValue(rp);
                else
                    val = super.getValue(path, args);
            }

            if (val instanceof Number) {
                return val;
            }
            if (val instanceof Entry && ((Entry) val).getMultiFi() != null) {
                ((FidelityManager) fiManager).reconfigure(Arg.selectFidelities(args));
                ((Entry) val).applyFidelity();
            }
            if (val instanceof Srv) {
                if (((Srv) val).isCached() && ((Srv) val).isValid()) {
                    return ((Srv) val).getOut();
                } else if (isChanged()) {
                    ((Srv) val).setValid(false);
                    ((Srv) val).setChanged(true);
                }
                Object carrier = ((Srv) val).getImpl();
                if (carrier instanceof Signature) {
                        return evalSignature((Signature) carrier, path, args);
                } else if (carrier instanceof SignatureEntry){
                    if (((Srv) val).getOut() != null && ((Srv) val).isValueCurrent() && !isChanged())
                        return ((Srv) val).getOut();
                    else {
                        Signature sig = (Signature) ((SignatureEntry)carrier).getImpl();
                        val = evalSignature(sig, path, args);
                    }
                } else if (carrier instanceof ServiceFidelity) {
                    Object selection = getFiService((ServiceFidelity) carrier, args, path);
                    if (selection instanceof Signature) {
                        val = evalSignature((Signature) selection, path, args);
                    } else if (selection instanceof Evaluation) {
                        val = ((Evaluation)selection).evaluate(args);
                    } else {
                        val = selection;
                    }
                } else if (carrier instanceof MorphFidelity) {
                    Object obj = getFiService((ServiceFidelity)((MorphFidelity) carrier).getFidelity(), args, path);
                    Object out = null;
                    if (obj instanceof Signature)
                        out = evalSignature((Signature)obj, path);
                    else if (obj instanceof Entry) {
                        ((Entry)obj).setScope(this);
                        out = ((Entry) obj).evaluate(args);
                    } else if (obj instanceof Mogram) {
                        ((Mogram)obj).setScope(this);
                        out = execMogram((Mogram) obj, args);
                    }
                    ((MorphFidelity) carrier).setChanged();
                    ((MorphFidelity) carrier).notifyObservers(out);
                    val = out;
                } else if (carrier instanceof MogramEntry) {
                    val = evalMogram((MogramEntry)carrier, path, args);
                } else if (carrier instanceof ValueCallable && ((Srv) val).getType() == Functionality.Type.LAMBDA) {
                    Context.Return rp = ((Srv) val).getReturnPath();
                    Object obj = null;
                    if (rp != null && rp.inPaths != null) {
                        Context cxt = getEvaluatedSubcontext(rp.inPaths, args);
                        obj = ((ValueCallable)carrier).call(cxt);
                    } else {
                        obj = ((ValueCallable) carrier).call(this);
                    }
                    ((Srv) get(path)).setOut(obj);
                    if (rp != null && rp.returnPath != null)
                        putValue(((Srv) val).getReturnPath().returnPath, obj);
                    val = obj;
                }  else if (carrier instanceof MorphMogram) {
                    (( MorphMogram ) carrier).setScope(this);
                    Object out = (( MorphMogram )carrier).exert(args);
                    Context cxt = null;
                    if (out instanceof Routine) {
                        cxt = ((Routine) out).getContext();
                        Context.Return rt = ((Routine) out).getProcessSignature().getContextReturn();
                        if (rt != null && rt.getReturnPath() != null) {
                            Object obj = cxt.getReturnValue();
                            putInoutValue(rt.getReturnPath(), obj);
                            ((Srv) get(path)).setOut(obj);
                            ((Routine) out).getContext().putValue(path, obj);
                            out = obj;
                        } else {
                            ((Srv) get(path)).setOut(cxt);
                            out = cxt;
                        }
                    }
                    val = out;
                } else if (carrier instanceof Client && ((Srv) val).getType() == Functionality.Type.LAMBDA) {
                    // getValue target entry for this cal
                    String entryPath = ((Srv)val).getPath();
                    Object out = ((Client)carrier).exec((Service) get(entryPath), this, args);
                    ((Srv) get(path)).setOut(out);
                    val = out;
                } else if (carrier instanceof EntryCollable && ((Srv) val).getType() == Functionality.Type.LAMBDA) {
                    Entry entry = ((EntryCollable)carrier).call(this);
                    ((Srv) get(path)).setOut(entry.getValue());
                    if (path != entry.getName())
                        putValue(entry.getName(), entry.getValue());
                    else if (asis(entry.getName()) instanceof Srv) {
                        ((Srv)asis(entry.getName())).setOut(entry.getValue());
                    }
                    val = entry;
                } else if (carrier instanceof Closure) {
                    Function entry = (Function) ((Closure)carrier).call(this);
                    ((Srv) get(path)).setOut(this.getValue());
                    putValue(path, this.getValue());
                    if (path != entry.getName())
                        putValue(entry.getName(), this.getValue());
                    val = entry;
                } else if (carrier instanceof ServiceInvoker) {
                    val =  ((ServiceInvoker)carrier).evaluate(args);
                } else if (carrier instanceof Service && ((Srv) val).getType() == Functionality.Type.LAMBDA) {
                    String[] paths = ((Srv)val).getPaths();
                    Arg[] nargs = null;
                    if (paths == null || paths.length == 0) {
                        nargs = new Arg[]{this};
                    } else {
                        nargs = new Arg[paths.length];
                        for (int i = 0; i < paths.length; i++) {
                            if (!(asis(paths[i]) instanceof Arg))
                                nargs[i] = new Value(paths[i], asis(paths[i]));
                            else
                                nargs[i] = (Arg) asis(paths[i]);
                        }
                    }
                    Object out = ((Service)carrier).execute(nargs);
                    ((Srv) get(path)).setOut(out);
                    val = out;
                } else if (((Entry)val).getImpl() instanceof Ref) {
                    // dereferencing Ref and executing
                    Ref ref = ((Ref) ((Entry) val).getImpl());
                    ref.setScope(this);
                    Object deref = ref.getValue();
                    if (deref instanceof Evaluation) {
                        if (deref instanceof Scopable) {
                            ((Scopable) deref).setScope(this);
                        }
                        val = ((Evaluation) deref).evaluate(args);
                    } else if (deref instanceof Signature) {
                        val = execSignature((Signature) deref, args);
                    } else {
                        // assume default dereference of Entry is inner Entry
                        val = ((Entry) deref).getValue(args);
                    }
                } else {
                    if (carrier == Context.none) {
                        val = getValue(((Srv) val).getName());
                    }
                }
            } else if (val instanceof Entry) {
                if (((Entry) val).getMultiFi() != null) {
                    ((FidelityManager) fiManager).reconfigure(Arg.selectFidelities(args));
                    ((Entry) val).applyFidelity();
                }
                // getData applies current fidelity
                if (((Entry)val).getImpl() instanceof Ref) {
                    // dereferencing Ref and executing
                    Ref ref = ((Ref)((Entry)val).getImpl());
                    ref.setScope(this);
                    Object deref = ref.getValue();
                    if (deref instanceof Evaluation) {
                        if (deref instanceof Scopable) {
                            ((Scopable)deref).setScope(this);
                        }
                        val = ((Evaluation) deref).evaluate(args);
                    } else {
                        // assume default dereference of Entry is inner Entry
                        val = ((Entry) deref).getValue(args);
                    }
                } else if (val instanceof Function){
                    val = ((Function)val).getValue(args);
                } else {
                    val = ((Entry)val).getValue(args);
                }
            } else if (val instanceof ServiceFidelity) {
                return ((Entry)((ServiceFidelity)val).getSelect()).getValue(args);
            } else if (val instanceof Model) {
                Context response = (Context) ((Model)val).getResponse(args);
                append(response);
                return response;
            } else {
                return super.getValue(path, args);
            }
        } catch (Exception e) {
            throw new EvaluationException(e);
        }
        Object obj = get(path);
        if (get(path) instanceof Entry) {
            ((Entry) get(path)).setOut(val);
            ((Entry) get(path)).setValid(true);
            ((Entry) get(path)).setChanged(true);
        }
        this.setChanged(true);
        return val;
    }

    private Object setSigResult(Signature signature, Object value) {
        Context.Return rp = signature.getContextReturn();
        if (rp != null && rp.returnPath != null) {
            put(rp.returnPath, value);
        }
        return value;
    }

    public Object evalSignature(Signature sig, String path, Arg... args) throws ServiceException, RemoteException {
        Context out = execSignature(sig, args);
        String sigrp = null;
        String  crp = null;
        if (sig.getContextReturn() != null) {
            sigrp = sig.getContextReturn().getReturnPath();
        }
        if (contextReturn != null) {
            crp = contextReturn.returnPath;
        }
        try {
            if (crp != null) {
                Object obj;
                obj = out.getValue(crp);
                if (obj == null)
                    obj = out.getValue(path);
                if (obj != null) {
                    ((Srv)get(path)).setOut(obj);
                    return obj;
                } else {
                    logger.warn("no eval for return contextReturn: {} in: {}", sig.getContextReturn().returnPath, out);
                    return out;
                }

            } else {
                if (sigrp != null) {
                    // add response for this signature
                    return ((ServiceContext)out).get(sigrp);
                }
            }
        } catch (RemoteException e) {
            throw new MogramException(e);
        }
        return out;
    }

    private Object evalMogram(MogramEntry mogramEntry, String path, Arg... entries)
            throws ServiceException, RemoteException {
        Mogram mogram = (Mogram) mogramEntry.getImpl();
		mogram.setScope(this);
        Mogram out = mogram.exert(entries);
        if (out instanceof Routine){
            Context outCxt = out.getContext();
            if (outCxt.getContextReturn() != null) {
                Object obj = outCxt.getReturnValue();
                ((Srv)get(path)).setOut(obj);
                return obj;
            } else if (outCxt.asis(Context.RETURN) != null) {
				((Srv)get(path)).setOut(outCxt.asis(Context.RETURN));
				return outCxt.asis(Context.RETURN);
			} else {
                ((Srv) get(path)).setOut(outCxt);
                return outCxt;
            }
        } else if (out instanceof Model) {
            Context outCxt = (Context) ((Model)out).getResponse(entries);
            append(outCxt);
            return outCxt;
        }
        return null;
    }

    protected Service getFiService(ServiceFidelity fi, Arg[] entries, String path) throws ContextException {
        Fi selected = null;
        List<Fi> fiList = Projection.selectFidelities(entries);
        for (Fi sfi : fiList) {
            if (sfi.getName().equals(path)) {
                selected = sfi;
                ((Entry) get(path)).setValid(false);
                isChanged();
                break;
            }
        }

        List<Service> choices = fi.getSelects(this);
        for (Service s : choices) {
            if (selected == null && fi.getSelect() != null) {
                Service req = fi.getSelect();
                return req;
            } else {
                String selectPath = null;
                if (selected != null) {
                    selectPath = selected.getPath();
                } else {
                    selectPath = ((Fidelity)choices.get(0)).getPath();
                }
                if (((Identifiable)s).getName().equals(selectPath)) {
                    fi.setSelect(s);
                    return s;
                }
            }
        }
        return null;
    }

    public Context execSignature(Signature sig, Arg... items) throws ServiceException, RemoteException {
        execDependencies(sig, items);
        return  super.execSignature(sig, items);
    }

    /**
     * Appends a signature <code>signature</code> for this model.
     **/
    public void addSignature(Signature signature) {
        if (signature == null)
            return;
        ((ServiceSignature) signature).setOwnerId(getOwnerId());
        ServiceFidelity sFi = (ServiceFidelity)multiFi.getSelect();
        sFi.getSelects().add(signature);
        sFi.setSelect(signature);
    }

    public void addSignatures(Signature... signatures) {
        ServiceFidelity sFi = (ServiceFidelity)multiFi.getSelect();
        if (sFi != null)
            sFi.getSelects().addAll(Arrays.asList(signatures));
        else {
            multiFi.addSelect(new ServiceFidelity(key));
            sFi.getSelects().addAll(Arrays.asList(signatures));
        }
    }

//    public Context evaluate(Context context, Arg... args) throws EvaluationException, RemoteException {
//        try {
//            return exert(null, args);
//        } catch (ContextException e) {
//            throw new EvaluationException(e);
//        }
//    }

    @Override
    public Context exert(Transaction txn, Arg... args) throws ContextException {
        Signature signature = null;
        ServiceFidelity sFi = (ServiceFidelity)multiFi.getSelect();
        try {
            if (sFi != null) {
                signature = (Signature) sFi.getSelect();
            } else if (subjectValue != null && subjectValue instanceof Signature) {
                signature = (Signature)subjectValue;
            }
            if (signature != null) {
                Routine out = operator.xrt(key, subjectValue, this).exert(txn, args);
                Routine xrt = out.exert();
                return xrt.getDataContext();
            } else {
                // compute model response
                getResponse(args);
                return this;
            }
        } catch (RemoteException | SignatureException | ServiceException e) {
            if (e instanceof ContextException) {
                throw (ContextException)e;
            }
            throw new ContextException(e);
        }
    }

    public RequestModel clearOutputs() {
        Iterator<Map.Entry<String, Object>> i = entryIterator();
        while (i.hasNext()) {
            Map.Entry e = i.next();
            if (e.getValue() instanceof Entry) {
                ((Entry) e.getValue()).setOut(null);
                ((Entry)e.getValue()).setValid(false);
            }
        }
        return this;
    }

    public RequestModel getInoutSubcontext(String... paths) throws ContextException {
        // bare-bones subcontext
        RequestModel subcntxt = new RequestModel();
        subcntxt.setSubject(subjectPath, subjectValue);
        subcntxt.setName(getName() + "-subcontext");
        subcntxt.setDomainId(getDomainId());
        subcntxt.setSubdomainId(getSubdomainId());
        if  (paths != null && paths.length > 0) {
            for (int i = 0; i < paths.length; i++)
                subcntxt.putInoutValueAt(paths[i], getValue(paths[i]), tally + 1);
        }
        return subcntxt;
    }

    public Object getItem(String path) {
        Object obj = get(path);
        if (obj instanceof Entry) {
            return ((Entry) obj).getImpl();
        }
        else {
            return null;
        }
    }
}
