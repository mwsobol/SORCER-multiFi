/*
 * Copyright 2009 the original author or authors.
 * Copyright 2009 SorcerSoft.org.
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
package sorcer.so;

import sorcer.Operator;
import sorcer.co.tuple.SignatureEntry;
import sorcer.core.context.ContextSelector;
import sorcer.core.context.DesignIntent;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.ThrowableTrace;
import sorcer.core.context.model.DataContext;
import sorcer.core.context.model.Transmodel;
import sorcer.core.context.model.ent.Developer;
import sorcer.core.context.model.ent.Entry;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.core.context.model.req.Srv;
import sorcer.core.plexus.*;
import sorcer.core.service.Collaboration;
import sorcer.core.service.Governance;
import sorcer.core.service.Transdesign;
import sorcer.core.signature.LocalSignature;
import sorcer.service.Exertion;
import sorcer.core.provider.exerter.ServiceShell;
import sorcer.service.*;
import sorcer.service.modeling.*;
import sorcer.service.Node;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static sorcer.mo.operator.*;

/**
 * Created by Mike Sobolewski on 9/10/20.
 */
public class operator extends Operator {

    public static Entry execEnt(Service service, Arg... args) throws ServiceException {
        try {
            ContextSelector contextSelector = selectContextSelector(args);
            Object result = service.execute(args);
            if (result instanceof Context && contextSelector != null) {
                    try {
                        result = contextSelector.doSelect(result);
                    } catch (ContextException e) {
                        throw new ServiceException(e);
                    }
            }
            return new Entry(((Identifiable)service).getName(), result);
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    public static ContextSelector selectContextSelector(Arg[] args) {
        for (Arg arg : args) {
            if (arg instanceof ContextSelector)
                return (ContextSelector)arg;
        }
        return null;
    }

    public static Entry execEnt(Service service, String selector, Arg... args) throws ServiceException {
        try {
            return new Entry(selector, service.execute(args));
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    public static <T> T exec(Entry<T> entry, Arg... args)
            throws ServiceException {
        try {
            synchronized (entry) {
                if (entry instanceof Entry && ((Entry) entry).getOut() instanceof ServiceContext) {
                    return (T) ((ServiceContext) ((Entry) entry).getOut()).getValue(entry.getName(), args);
                } else if (entry instanceof Incrementor) {
                    return ((Incrementor<T>) entry).next();
                } else if (entry instanceof Routine) {
                    return (T) ((Routine) entry).exert(args).getContext();
                } else if (entry instanceof Functionality) {
                    if (entry instanceof Srv && entry.getImpl() instanceof SignatureEntry) {
                        return  (T) entry.execute(args);
                    } else {
                        return (T) ((Functionality) entry).getValue(args);
                    }
                } else if (entry instanceof Evaluation) {
                    return (T) ((Entry) entry).evaluate(args);
                } else {
                    return (T)  entry.execute(args);
                }
            }
        } catch (Exception e) {
            throw new EvaluationException(e);
        }
    }

    public static <T> T eval(evr<T> invoker, Arg... args)
            throws EvaluationException {
        try {
            if (invoker instanceof Incrementor){
                return ((Incrementor<T>) invoker).next();
            } else {
                return invoker.evaluate(args);
            }
        } catch (RemoteException e) {
            throw new EvaluationException(e);
        }
    }

    public static Object exec(Mogram domain, String path, Arg... args) throws ServiceException, RemoteException {
        if (domain instanceof Model) {
            try {
                return ((Model)domain).getValue(path, args);
            } catch (RemoteException e) {
                throw new ContextException(e);
            }
        } else {
            // includes exertions
            return value(domain.getContext(), path, args);
        }
    }

    public static Object exec(ContextDomain domain, String path, Arg... args) throws ServiceException {
        if (path.indexOf("$") > 0) {
            String pn;
            String dn;
            int ind = path.indexOf("$");
            pn = path.substring(0, ind);
            dn = path.substring(ind + 1);
            return response(domain, pn, dn);
        }
        if (domain instanceof Model) {
            try {
                return domain.getValue(path, args);
            } catch (RemoteException e) {
                throw new ContextException(e);
            }
        } else {
            return value((Context) domain, path, args);
        }
    }

    public static Context evalDomain(Collaboration collab, Request request, Context context) throws ServiceException {
        return collab.evaluateDomain(request, context);
    }

    public static Context evalDomain(Collaboration collab, String domainName, Context context) throws ServiceException {
        return collab.evaluateDomain(domainName, context);
    }

    public static ServiceContext eval(Request request, Context context) throws ContextException {
        Context rc;
        try {
            if (request instanceof Contextion) {
                rc = ((Contextion) request).evaluate(context);
            } else {
                try {
                    if (context == null) {
                        rc = (Context) request.execute(new Arg[0]);
                    } else {
                        rc = (Context) request.execute(new Arg[]{context});
                    }
                } catch (ServiceException e) {
                    throw new ContextException(e);
                }
            }
        } catch (RemoteException | ServiceException e) {
            throw new ContextException(e);
        }
        return (ServiceContext)rc;
    }

    public static void clear(Node node) throws MogramException {
        ((ServiceNode) node).clear();
    }

    public static Context eval(Signature signature, Arg... args)
        throws ContextException {
        Context out = null;
        try {
            Object target = ((LocalSignature) signature).build();

            if (target instanceof Node) {
                out = (Context) ((Node) target).execute(args);
            } else if (target instanceof Model) {
                Context cxt = Arg.selectContext(args);
                out = ((Model)target).evaluate(cxt);
            }
        } catch (SignatureException | ServiceException | RemoteException e) {
            throw new ContextException(e);
        }
        return out;
    }

    public static Context dscOut(Node node) throws ContextException {
        return node.getOutput();
    }

    public static Context dspOut(Node node) throws ContextException {
        return ((ServiceNode) node).getOutDispatcher().getContext();
    }

    public static Context out(Governance governance) {
        return governance.getOutput();
    }

    public static Context result(Node node) throws ContextException {
        return ((Routine) node.getOutDispatcher()).getContext();
    }

    public static Dispatch dispatcher(Node node) {
        return node.getOutDispatcher();
    }

    public static Response query(Mogram mogram, Arg... args) throws ContextException {
        try {
            synchronized (mogram) {
                if (mogram instanceof Routine) {
                    return mogram.exert(args).getContext();
                } else {
                    return (Response) ((EntryModel) mogram).getValue(args);
                }
            }
        } catch (RemoteException | ServiceException e) {
            throw new ContextException(e);
        }
    }

    public static Fidelity<Path> response(String... paths) {
        Fidelity resp = new Fidelity("RESPONSE");
        resp.setSelects(Path.getPathList(paths));
        resp.setType(Fi.Type.RESPONSE);
        return resp;
    }

    public static Object resp(Mogram model, String path) throws ServiceException {
        return response(model, path);
    }

    public static Context resp(Mogram model) throws ServiceException {
        return response(model);
    }

    public static Object response(Routine exertion, String path) throws ServiceException {
        try {
            return ((ServiceContext)exertion.exert().getContext()).getResponseAt(path);
        } catch (RemoteException e) {
            throw new ContextException(e);
        }
    }

    public static Object exec(Request request, String path$domain) throws ServiceException {
        if (request instanceof DataContext) {
            return value((Context)request, path$domain);
        } else {
            return response((Context)request, path$domain);
        }
    }

    public static Object response(ContextDomain model, String path$domain) throws ServiceException {
        String path;
        String domain;
        if (path$domain.indexOf("$") > 0) {
            int ind = path$domain.indexOf("$");
            path = path$domain.substring(0, ind);
            domain = path$domain.substring(ind + 1);
            return response(model, path, domain);
        } else {
            return ((ServiceContext) model).getResponseAt(path$domain);
        }
    }

    public static Object exec(ContextDomain model, String path, String domain) throws ServiceException {
        return response(model, path, domain);
    }

    public static Object response(ContextDomain model, String path, String domain) throws ServiceException {
        if (((ServiceMogram)model).isEvaluated() && ((ServiceMogram)model).getMdaFi() == null) {
            return ((ServiceMogram)((ServiceContext)model).getChild(domain)).getEvaluatedValue(path);
        } else {
            try {
                return ((Context)((ServiceContext)model).getChild(domain)).getValue(path);
            } catch (RemoteException ex) {
                throw new ContextException(ex);
            }
        }
    }

    public static ServiceContext response(Signature signature, Object... items) throws ServiceException {
        Mogram mogram = null;
        try {
            mogram = (Mogram) ((LocalSignature)signature).initInstance();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return response(mogram, items);
    }

    public static Context outcome(Contextion domain, Context context, Arg... args) throws ServiceException {
        if (domain instanceof Model) {
            try {
                return ((Model) domain).getResponse(context, args);
            } catch (RemoteException e) {
                throw new ServiceException(e);
            }
        } else if (domain instanceof Routine) {
            ((Routine)domain).getDataContext().append(context);
            return exertionResponse((Routine) domain, (Object[])args);
        }
        return ((ServiceMogram)domain).getDataContext().append(context);
    }


    public static ServiceContext response(Mogram mogram, Object... items) throws ServiceException {
        if (mogram instanceof Routine) {
            return exertionResponse((Routine) mogram, items);
        } else if (mogram instanceof ContextDomain &&  ((ServiceMogram)mogram).getType().equals(Functionality.Type.MADO)) {
            if (((ServiceMogram)mogram).isEvaluated()) {
                return (ServiceContext) ((ServiceMogram)((ServiceContext) mogram).getChild((String) items[0])).getEvaluatedValue((String) items[1]);
            } else {
                return (ServiceContext) ((ServiceContext) ((ServiceContext) mogram).getChild((String) items[0])).getValue((String) items[1]);
            }
        } else if (mogram instanceof Transmodel &&
            ((Transmodel)mogram).getChildren() != null &&
            ((Transmodel)mogram).getChildren().size() > 0) {
            Context cxt = null;
            for (Object item : items) {
                if (item instanceof Context) {
                    cxt = (Context)item;
                    break;
                }
            }
            try {
                return (ServiceContext) mogram.evaluate(cxt);
            } catch (RemoteException e) {
                throw new ContextException(e);
            }
        } else if (mogram instanceof ContextDomain &&  ((ServiceMogram)mogram).getType().equals(Functionality.Type.DESIGN)) {
            return developDsign(( DesignIntent ) mogram, items);
        } else {
            return modelResponse((ContextDomain) mogram, items);
        }
    }

    public static Transdesign dzn(String name) throws ServiceException {
        return new Transdesign(name);
    }

    public static Transdesign dzn(Intent designIntent) throws ServiceException {
        return dzn(null, designIntent);
    }

    public static Transdesign dzn(String name, Intent designIntent) throws ServiceException {
        try {
            Transdesign design = new Transdesign(name, designIntent);
            FidelityManager fiManger = new DesignFidelityManager(design);
            design.setFidelityManager(fiManger);
            designIntent.setSubject(design.getName(),design);
            return design;
        } catch (SignatureException | RemoteException e) {
            throw new ServiceException(e);
        }
    }

    public static Transdesign dzn(String name, Discipline discipline, Context discContext, Development developer) throws ServiceException {
        return new Transdesign(name, discipline, discContext, developer);
    }

    public static Transdesign dzn(Discipline discipline, Context discContext, Development developer) throws ServiceException {
        return new Transdesign(null, discipline, discContext, developer);
    }

    public static ServiceContext dvlp(Request request, Object... items) throws ServiceException {
        return eval(request, items);
    }

    public static ServiceContext dvlp(Intent intent, Object... items) throws ServiceException {
        return eval(dzn(intent), items);
    }

    public static Context eval(Node node, Arg... args)
        throws ContextException {
        try {
            return (Context) node.execute(args);
        } catch (ServiceException | RemoteException e) {
            throw new ContextException(e);
        }
    }

    public static ServiceContext eval(Request request, Object... items) throws ServiceException {
        Context out = null;
        try {
            if (request instanceof Mogram) {
                out = response((Mogram)request, items);
            } else if (request instanceof Design) {
                return morphDesign(( Transdesign ) request, items);
            }else if (request instanceof Node) {
                Arg[] args = new Arg[items.length];
                System.arraycopy(items, 0, args, 0, items.length);
                return (ServiceContext) request.execute(args);
            } else if(items.length == 1) {
                if (items[0] instanceof Context) {
                    out = eval(request, (Context) items[0]);
                } else  if (items[0] instanceof Signature) {
                    //&& ((ServiceSignature)items[0]).isKindOf(Signature.Kind.CONTEXT)) {
                    Context cxt = (Context) ((LocalSignature)items[0]).initInstance();
                    out = eval(request, cxt);
                }
            } else if(items.length == 0) {
                out = eval(request, (Context)null);
            }
        } catch (SignatureException | RemoteException | ExecutiveException | ConfigurationException e) {
            throw new ContextException(e);
        }
        return (ServiceContext)out;
    }

    public static ServiceContext morphDesign(Transdesign design, Object... items)
        throws ServiceException, RemoteException, ExecutiveException, ConfigurationException {
        if (design.getDevelopmentFi() == null) {
            design.setDeveloperFi(design.getDesignIntent());
        }
        Fi developerFi =  design.getDevelopmentFi();
        // setup the design developer and discipline intent
        Context disciplineIntent = null;
        DesignFidelityManager fiManager = (DesignFidelityManager)design.getFidelityManager();
        for (Object item : items) {
            if (item instanceof Fidelity && (((Fidelity) item).getFiType().equals(Fi.Type.DEV)
            || ((Fidelity) item).getFiType().equals(Fi.Type.INTENT))) {
                if (fiManager != null) {
                    fiManager.reconfigure((Fidelity ) item);
                } else {
                    developerFi.selectSelect(((Fidelity) item).getName());
                }
            }
        }
        Context out = null;
        Morpher inMorpher = design.getInMorpher();
        Morpher outMorpher = design.getMorpher();
        if (developerFi != null) {
            Development developer = ( Development ) developerFi.getSelect();
            if (design.getDesignIntent() != null) {
                // setup a design intent for the design developer
                Object obj = design.getDesignIntent();
                if (inMorpher != null || outMorpher != null) {
                    if (obj != null && obj instanceof Context) {
                        if (fiManager != null && inMorpher != null) {
                            inMorpher.morph(fiManager, developerFi, design);
                        }
                        // handle the development morph fidelity
                        if (developerFi instanceof MorphFidelity && (( MorphFidelity ) developerFi).getMorpher() != null) {
                            (( MorphFidelity ) developerFi).setChanged();
                            (( MorphFidelity ) developerFi).notifyObservers(obj);
                            out = design.getOutput();
                        } else {
                            out = developer.develop(null, ( ServiceContext ) obj);
                            if (fiManager != null && outMorpher != null) {
                                outMorpher.morph(fiManager, developerFi, design);
                            }
                        }
                    }
                } else if (design.getDiscipline() != null) {
                    if (developerFi instanceof MorphFidelity) {
                        MorphFidelity morphFi = ( MorphFidelity ) developerFi;
                        if (morphFi.getInMorpherFi() != null) {
                            morphFi.getInMorpherFi().setSelect(null);
                            morphFi.getInMorpherFi().properSelect((( Developer ) developer).getName());
                            if (morphFi.getInMorpherFi().getProperSelect() != null) {
                                morphFi.setChanged();
                                morphFi.setFiType(Fi.Type.IN);
                                morphFi.notifyObservers(design);
                            }
                        }
                    }
                    out = developer.develop(( Discipline ) design.getDiscipline(), design.getDisciplineIntent());
                    if (developerFi instanceof MorphFidelity) {
                        MorphFidelity morphFi = ( MorphFidelity ) developerFi;
                        if (morphFi.getMorpherFi() != null) {
                            morphFi.getMorpherFi().setSelect(null);
                            morphFi.getMorpherFi().properSelect((( Developer ) developer).getName());
                            if ((( MorphFidelity ) developerFi).getMorpherFi().getProperSelect() != null) {
                                morphFi.setChanged();
                                morphFi.setFiType(Fi.Type.OUT);
                                morphFi.notifyObservers(design);
                                out = design.getDiscipline().getOutput();
                            }
                        }
                    }
                }
            } else if (design.getDiscipline() == null) {
                        out = developer.develop(null, design.getDisciplineIntent());
            }
        }
        return (ServiceContext)out;
    }

    public static ServiceContext developDsign(DesignIntent designIntent, Object... items) throws ContextException {
        Development developer = (Development) designIntent.getDeveloperFi().getSelect();
        Discipline discipline = null;
        try {
            if (designIntent.getDisciplineSignature() != null) {
                discipline =  (Discipline) ((LocalSignature) designIntent.getDisciplineSignature()).initInstance();
            } else {
                discipline = designIntent.getDiscipline();
            }
            return (ServiceContext) developer.develop(discipline, designIntent.getDisciplineIntent());
        } catch (SignatureException | ExecutiveException | ServiceException | RemoteException e) {
            throw new ContextException(e);
        }
    }

    public static ServiceContext modelResponse(ContextDomain model, Object... items) throws ContextException {
        try {
            List<Arg> argl = new ArrayList();
            List<Path> paths = new ArrayList();;
            FidelityList pthFis = new FidelityList();;
            Fidelity prjFi = null;
            Fidelity cxtFi = null;
            Projection cxtPrj = null;
            for (Object item : items) {
                if (item instanceof Path) {
                    paths.add((Path) item);
                } if (item instanceof String) {
                    paths.add(new Path((String) item));
                } else if (item instanceof FidelityList) {
                    argl.addAll((Collection<? extends Arg>) item);
                } else if (item instanceof List
                        && ((List) item).size() > 0
                        && ((List) item).get(0) instanceof Path) {
                    paths.addAll((List<Path>) item);
                } else if (item instanceof Projection) {
                    cxtPrj = (Projection)item;
                } else if (item instanceof Fidelity) {
                    Fidelity fi = (Fidelity)item;
                    if (fi.getFiType().equals(Fi.Type.FROM_TO)) {
                        pthFis.add((Fidelity) item);
                    } else if (fi.getFiType().equals(Fi.Type.CONTEXT)) {
                        cxtFi = fi;
                    } else if (fi.getFiType().equals(Fi.Type.PROJECTION)) {
                        prjFi = fi;
                    } else {
                        argl.add((Arg) item);
                    }
                } else if (item instanceof Arg) {
                    argl.add((Arg) item);
                }
            }
            if (pthFis.size() > 0) {
                ((ServiceContext)model).remap(new Projection(pthFis));
            }
            if (paths != null && paths.size() > 0) {
                model.getDomainStrategy().setResponsePaths(paths);
            }
            ContextFidelityManager cfmgr = ((ServiceContext)model).getContextFidelityManager();
            if (cxtFi != null) {
                ServiceContext dcxt = (ServiceContext) cfmgr.getDataContext();
                if (dcxt != null) {
                    dcxt.selectFidelity(cxtFi.getName());
                    ((ServiceContext) model).append(dcxt);
                }
            }
            if (prjFi != null) {
                Projection prj = (Projection) cfmgr.getFidelity(prjFi.getName());
                if (prj != null) {
                    if (model.getInPathProjection() != null) {
                        ((ServiceContext)cfmgr.getDataContext()).remap(model.getInPathProjection());
                    }
                    if (model.getOutPathProjection() != null) {
                        ((ServiceContext)cfmgr
                                .getDataContext()).setMultiFiPaths(((ServiceContext)model.getContext()).getMultiFiPaths());
                        ((ServiceContext)cfmgr.getDataContext()).remap(model.getOutPathProjection());
                    }
                    cfmgr.morph(prj.getName());
                }
            }
            if (cxtPrj != null) {
                Fidelity fi = cxtPrj.getContextFidelity();
                if (fi != null) {
                    ServiceContext dcxt = (ServiceContext) cfmgr.getDataContext();
                    dcxt.selectFidelity(cxtFi.getName());
                    ((ServiceContext) model).append((Context) dcxt.getMultiFi().getSelect());
                }
                ((ServiceContext) model).setProjection(cxtPrj);
            }
            Arg[] args = new Arg[argl.size()];
            argl.toArray(args);
            if (((ServiceMogram)model).getFidelityManager() != null) {
                try {
                    ((FidelityManager) ((ServiceMogram)model).getFidelityManager()).reconfigure(Arg.selectFidelities(args));
                } catch (ConfigurationException e) {
                   throw new ContextException(e);
                }
            }
            ((ServiceContext)model).substitute(args);
            ((ServiceMogram)model).setValid(false);
            if (cfmgr != null && cfmgr.getDataContext().getMorpher() != null) {
                ((ServiceContext)model).getContextFidelityManager().morph();
            }
            ServiceContext out = (ServiceContext) model.getResponse(args);
            ((ServiceMogram)model).setValid(true);
            if (cfmgr != null && cfmgr.getDataContext().getMorpher() != null) {
                ((ServiceContext)model).getContextFidelityManager().morph();
            }
            return out;
        } catch (RemoteException | ConfigurationException e) {
            throw new ContextException(e);
        }
    }

    public static ServiceContext exertionResponse(Routine exertion, Object... items) throws ServiceException {
        try {
            List<Arg> argl = new ArrayList();
            List<Path> paths = new ArrayList();;
            for (Object item : items) {
                if (item instanceof Path) {
                    paths.add((Path) item);
                } if (item instanceof String) {
                    paths.add(new Path((String) item));
                } else if (item instanceof FidelityList) {
                    argl.addAll((Collection<? extends Arg>) item);
                } else if (item instanceof List
                        && ((List) item).size() > 0
                        && ((List) item).get(0) instanceof Path) {
                    paths.addAll((List<Path>) item);
                } else if (item instanceof Arg) {
                    argl.add((Arg) item);
                }
            }
            if (paths != null && paths.size() > 0) {
                exertion.getDomainStrategy().setResponsePaths(paths);
            }
            Arg[] args = new Arg[argl.size()];
            argl.toArray(args);
            if (((ServiceMogram)exertion).getFidelityManager() != null) {
                ((FidelityManager) ((ServiceMogram)exertion).getFidelityManager()).reconfigure(Arg.selectFidelities(args));
            }
            return (ServiceContext) exertion.exert(args).getContext();
        } catch (RemoteException | ServiceException | ConfigurationException e) {
            throw new ContextException(e);
        }
    }

    public static Object eval(Routine exertion, String selector,
							  Arg... args) throws EvaluationException {
        try {
            exertion.getDataContext().setContextReturn(new Context.Return(selector));
            return exec(exertion, args);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e);
        }
    }

    /**
     * Assigns the tag for this context, for example "triplet|one|two|three" is a
     * tag (relation) named 'triplet' as a product of three "places" one, two, three.
     *
     * @param context
     * @param association
     */
    public static Context tagAssociation(Context context, String association) throws ContextException {
        context.setAttribute(association);
        return context;
    }

    public static Object execItem(Request item, Arg... args) throws ServiceException {
        try {
            return item.execute(args);
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    public static Object execMogram(Mogram mogram, Arg... args) throws ServiceException {
        Object out;
        synchronized (mogram) {
            if (mogram instanceof Routine) {
                out = new ServiceShell().evaluate(mogram, args);
            } else {
                out = ((ServiceContext) mogram).getValue(args);
            }
            ((ServiceMogram)mogram).setChanged(true);
            return out;
        }
    }

    public static Object exec(Service service, Arg... args) throws ServiceException {
        try {
            if (service instanceof Entry || service instanceof Signature ) {
                return service.execute(args);
            } else if (service instanceof Mogram) {
                if (service instanceof DataContext || service instanceof MorphMogram) {
                    return new sorcer.core.provider.exerter.ServiceShell().exec(service, args);
                } else if (service instanceof Node) {
                    return service.execute(args);
                } else {
                    return execMogram((Mogram) service, args);
                }
            } else if (service instanceof Evaluation) {
                return ((Evaluation) service).evaluate(args);
            } else if (service instanceof Modeling) {
                ContextDomain cxt = Arg.selectDomain(args);
                if (cxt != null) {
                    return ((Modeling) service).evaluate((ServiceContext)cxt);
                } else {
                    ((ServiceContext)service).substitute(args);
                    ((Modeling) service).evaluate();
                }
                return ((Model)service).getResult();
            } else {
                return service.execute(args);
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public static List<ThrowableTrace> exceptions(Routine exertion) throws RemoteException {
        return exertion.getExceptions();
    }

    public static <T extends Contextion> T exert(T mogram, Arg... args) throws ServiceException {
        try {
            return mogram.exert(null, args);
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

//    public static <T extends Contextion> T exert(T input, Transaction transaction, Arg... entries)
//            throws ServiceException {
//        return new sorcer.core.provider.exerter.ServiceShell().exert(input, transaction, entries);
//    }

    public static <T extends Contextion> T exert(Exertion service, T mogram, Arg... entries) throws ServiceException {
        try {
            return service.exert(mogram, null, entries);
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

//    public static <T extends Contextion> T exert(Exertion service, T mogram, Transaction txn, Arg... entries)
//            throws ServiceException {
//        try {
//            return service.exert(mogram, txn, entries);
//        } catch (RemoteException e) {
//            throw new ServiceException(e);
//        }
//    }

}