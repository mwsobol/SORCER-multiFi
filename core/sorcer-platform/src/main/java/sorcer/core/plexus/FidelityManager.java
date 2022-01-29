/*
* Copyright 2015 SORCERsoft.org.
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

package sorcer.core.plexus;

import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.co.tuple.Tuple2;
import sorcer.core.context.model.ent.Entry;
import sorcer.core.invoker.Observable;
import sorcer.core.invoker.Observer;
import sorcer.core.service.Transdesign;
import sorcer.service.*;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static sorcer.eo.operator.fi;
import static sorcer.eo.operator.rFi;

/**
 * Created by Mike Sobolewski on 6/14/15.
 */
public class FidelityManager implements Service, FidelityManagement, Observer, Identifiable {

    protected final static Logger logger = LoggerFactory.getLogger(FidelityManager.class.getName());

    // sequence number for unnamed instances
    protected static int count = 0;

    protected String name;

    protected String selectedProjection;

    Uuid id = UuidFactory.generate();

    // fidelities for signatures and other selection of T
    protected Map<String, Fidelity> fidelities = new ConcurrentHashMap<>();

    // fidelities for fidelites
    protected Map<String, MetaFi> metafidelities = new ConcurrentHashMap<>();

    // fidelities for signatures and other selection of T
    protected Map<String, MorphFidelity> morphFidelities = new ConcurrentHashMap<>();

    // fidelity projections
    protected Map<String, Projection> projections = new ConcurrentHashMap<>();

    // changed fidelities by morphers
    protected List<Fi> fiTrace = new ArrayList();

    protected Contextion mogram;

    protected Contextion child;

    protected boolean isTraced = false;

    Map<String, List<RemoteEventListener>> subscribers;

    protected Map<Long, Session> sessions;

    public FidelityManager() {
        name = "fiManager" + count++;
    }

    public FidelityManager(String name) {
        this.name = name;
    }

    public FidelityManager(Contextion contextion) {
        this(contextion.getName());
        this.mogram = contextion;
    }

    public String getSelectedProjection() {
        return selectedProjection;
    }

    public Map<String, MetaFi> getMetafidelities() {
        return metafidelities;
    }

    public void setMetafidelities(Map<String, MetaFi> metafidelities) {
        this.metafidelities = metafidelities;
    }

    public void addMetaFidelity(String path, Metafidelity fi) {
        if (fi != null)
            this.metafidelities.put(path, fi);
    }

    @Override
    public Map<String, Fidelity> getFidelities() {
        return fidelities;
    }


    public Map<String, MorphFidelity> getMorphFidelities() {
        return morphFidelities;
    }

    public void setMorphFidelities(Map<String, MorphFidelity> morphFidelities) {
        this.morphFidelities = morphFidelities;
    }

    public Map<String, Projection> getProjections() {
        return projections;
    }

    public void setProjections(Map<String, Projection> projections) {
        this.projections = projections;
    }

    public void setProjections(List<Projection> projections) {
        for (Projection p : projections) {
            this.projections.put(p.getName(), p);
        }
    }

    public void setFidelities(Map<String, Fidelity> fidelities) {
        this.fidelities = fidelities;
    }

    public void addFidelity(String path, Fidelity fi) {
        if (fi != null)
            this.fidelities.put(path, fi);
    }

    public void addMorphedFidelity(String path, MorphFidelity mFi) {
        if (mFi != null)
            this.morphFidelities.put(path, mFi);
    }

    public List<Fi> getFiTrace() {
        return fiTrace;
    }

    public void setFiTrace(List<Fi> fiTrace) {
        this.fiTrace = fiTrace;
    }

    public void addTrace(ServiceFidelity fi) {
        if (fi != null)
            this.fiTrace.add(fi);
    }

    @Override
    public void publish(net.jini.core.entry.Entry entry) throws RemoteException, ContextException {
        PublishEvent event = null;
        if (entry instanceof Tuple2) {
            String path = (( Tuple2 ) entry).getName();
            Object value = mogram.getContext().getValue(path);
            try {
                for (RemoteEventListener subscriber : subscribers.get(path)) {
                    event = new PublishEvent(getId(), 0, 0, null, 0);
                    event.setEntry(entry);
                    subscriber.notify(event);
                }
            } catch (UnknownEventException e) {
                throw new ContextException(e);
            }
        }
    }

    public void addMetafidelity(String path, Metafidelity fi) {
        this.metafidelities.put(path, fi);
    }

    public Contextion getMogram() {
        if (child != null)
            return child;
        else
            return mogram;
    }

    public void setMogram(Mogram mogram) {
        this.mogram = mogram;
    }

    public <M extends Mogram> M exert(M mogram, Transaction txn, Arg... args) throws ServiceException, TransactionException, RemoteException {
        this.mogram = mogram;
        return ( M ) mogram.exert(txn);
    }

    public <T extends Mogram> T exert(T mogram) throws TransactionException, ServiceException, RemoteException {
        return exert(mogram, null);
    }

    public void initialize() {
        // implement is subclasses
    }

    public void init(List<Metafidelity> fidelities) {
        if (fidelities == null || fidelities.size() == 0) {
            initialize();
            return;
        }
        for (Metafidelity fi : fidelities) {
            this.metafidelities.put(fi.getName(), fi);
        }
    }

    @Override
    public EventRegistration register(long eventID, String path, RemoteEventListener toInform, long leaseLenght)
        throws UnknownEventException, RemoteException {
        if (sessions == null) {
            sessions = new HashMap();
        }
        String source = getClass().getName() + "-" + UUID.randomUUID();
        Session session = new Session(eventID, source, path, toInform,
            leaseLenght);
        sessions.put(eventID, session);
        EventRegistration er = new EventRegistration(eventID, source, null,
            session.seqNum);
        return er;
    }

    @Override
    public void deregister(long eventID) throws UnknownEventException,
        RemoteException {
        if (sessions.containsKey(eventID)) {
            sessions.remove(eventID);
        } else
            throw new UnknownEventException("No registration for eventID: "
                + eventID);
    }

    public Map<Long, Session> getSessions() {
        return sessions;
    }

    public void selectFidelity(Fi fi) throws ContextException, ConfigurationException {
        // implement in subclasses
    }

    public void morph(List<String> fiNames) throws EvaluationException {
        String[] array = new String[fiNames.size()];
        morph(fiNames.toArray(array));
    }

    @Override
    public void project(String... fiNames) throws EvaluationException {
        logger.info("morphing model: {} with: {}",  mogram.getName(), Arrays.toString(fiNames));
        for (String fiName : fiNames) {
            try {
                // first check fi dependencies on projection
                ((ServiceMogram)mogram).execDependencies(fiName);
                Fidelity mFi = projections.get(fiName);
                if (mFi == null) {
                    throw new EvaluationException("No such projection: " + fiName);
                } else {
                    selectedProjection = fiName;
                }
                List<Service> fis = mFi.getSelects();
                if (isTraced) {
                    fiTrace.add(new ServiceFidelity(fiName, fis));
                }
                for (Service fi : fis) {
                    if (fi instanceof Projection) {
                        Fidelity[] fa = new Fidelity[((ServiceFidelity) fi).getSelects().size()];
                        ((ServiceFidelity) fi).getSelects().toArray(fa);
                        reconfigure(fa);
                        continue;
                    }
                    selectFidelity((Fidelity) fi);
                }
            } catch (ContextException | ConfigurationException e) {
                throw new EvaluationException(e);
            }
        }
        ((ServiceMogram)mogram).applyFidelities();
    }

    @Override
    public void morph(String... fiNames) throws EvaluationException {
        for (String fiName : fiNames) {
            Metafidelity mFi = ( Metafidelity ) metafidelities.get(fiName);
            List<Fi> fis = mFi.getSelects();
            String name = null;
            String path = null;
            if (isTraced) {
                fiTrace.add(new Fidelity(fiName, fis));
            }
            try {
                for (Fi fi : fis) {
                    name = fi.getName();
                    path = fi.getPath();
                    Fidelity fiEnt = fidelities.get(path);
                    if (fiEnt != null) {
                        if (morphFidelities.get(path) != null && fi.getFiType().equals(Fi.Type.MORPH)) {
                            morphFidelities.get(path).setMorpherSelect(name);
                        } else {
                            fiEnt.selectSelect(name, path);
                        }
                        (( ServiceMogram ) mogram).applyFidelity(path);
                    }
                }
            } catch (ConfigurationException e) {
                throw new EvaluationException(e);
            }
        }
    }

    public ServiceFidelityList resetFidelities() throws ContextException {
        ServiceFidelityList fl = new ServiceFidelityList();
        Collection<Fidelity> fc = fidelities.values();
        for (Fidelity sf : fc) {
            sf.setSelect(sf.get(0));
            try {
                fl.add(rFi(sf.getPath(), (( Identifiable ) sf.getSelect()).getName()));
            } catch (ConfigurationException e) {
                throw new ContextException(e);
            }
        }
        return fl;
    }

    public ServiceFidelityList getCurrentFidelities() throws ContextException {
        ServiceFidelityList fl = new ServiceFidelityList();
        Iterator<Map.Entry<String, Fidelity>> it = fidelities.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Fidelity> me = it.next();
            try {
                fl.add(rFi(me.getKey(), (( Identifiable ) me.getValue().getSelect()).getName()));
            } catch (ConfigurationException e) {
                throw new ContextException(e);
            }
        }
        return fl;
    }

    @Override
    public FidelityList getDefaultFidelities() throws RemoteException {
        FidelityList fl = new FidelityList();
        Iterator<Map.Entry<String, Fidelity>> it = fidelities.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Fidelity> me = it.next();
            Object defaultFi = me.getValue().getSelects().get(0);
            if (defaultFi instanceof ServiceFidelity) {
                fl.add(new Fidelity((( ServiceFidelity ) defaultFi).getName(), (( ServiceFidelity ) defaultFi).getPath()));
            } else {
                fl.add(new Fidelity(me.getKey(), (( Identifiable ) me.getValue().get(0)).getName()));
            }
        }
        return fl;
    }

    @Override
    public void reconfigure(String... fiNames) throws ConfigurationException {
        // applies to MultiFiRequests
        if (fidelities.size() == 1 && fiNames.length == 1) {
            Fidelity fi = fidelities.get(name);
            if (fi == null) {
                fi = getFidelity(fiNames[0]);
            }
            fi.selectSelect(fiNames[0]);
            fi.setChanged(true);
            if (isTraced) {
                ServiceFidelity nsf = new ServiceFidelity(fiNames[0]);
                nsf.setPath(name);
                fiTrace.add(nsf);
            }
        }
    }

    public Fidelity getFidelity(String name) {
        Fidelity fi = fidelities.get(name);
        if (fi == null) {
            // getValue unknown fidelity
            Set<String> keys = fidelities.keySet();
            Iterator<String> i = keys.iterator();
            String key = i.next();
            Fidelity sf = fidelities.get(key);
            List<Service> sfl = sf.getSelects();
            for (Service f : sfl) {
                if ((( Identifiable ) f).getName().equals(name))
                    return sf;
            }
        }
        return fi;
    }

    public void reconfigure(List<Fi> fiList) throws ConfigurationException {
        Fidelity[] list = new Fidelity[fiList.size()];
        reconfigure(fiList.toArray(list));
    }

    @Override
    public void reconfigure(Fi... fidelities) throws ConfigurationException {
        if (fidelities == null || fidelities.length == 0) {
            return;
        }
        for (Fi fi : fidelities) {
            Fidelity sFi = this.fidelities.get(fi.getPath());
            if (sFi != null) {
                sFi.selectSelect(fi.getName());
                sFi.setChanged(true);
                if (mogram instanceof Routine) {
                    (( ServiceMogram ) mogram).setSelectedFidelity(( ServiceFidelity ) sFi.getSelect());
                    if (mogram.getClass() == Task.class) {
                        (( Task ) mogram).setDelegate(null);
                    }
                }
            }
            if (isTraced)
                fiTrace.add(fi);
        }
    }

    protected Object getSelectFi(Collection<Fidelity> fidelities, String name) throws ConfigurationException {
        Object selected = null;
        for (Fidelity fi : fidelities) {
            if (fi instanceof Fidelity && (( Fidelity ) fi).getName().equals(name)) {
                selected = fi;
            }
            if (selected != null) {
                return selected;
            }
        }
        return selected;
    }

    public void add(List<Fidelity> fis) {
        for (Fidelity fi : fis){
            fidelities.put(fi.getName(), fi);
        }
    }

    public void add(Fidelity... fidelities) {
        for (Fidelity fi : fidelities){
            if (fi instanceof MetaFi) {
                this.metafidelities.put(fi.getName(), (Metafidelity)fi);
            } else {
                this.fidelities.put(fi.getName(), fi);
            }
        }
    }

    public void put(String fiName, Fi fi) {
        if (fi instanceof MetaFi) {
            metafidelities.put(fiName, (MetaFi)fi);
        } else {
            fidelities.put(fiName, (Fidelity)fi);
        }
    }

    public void put(Entry<Fi>... entries) throws ContextException {
        for(Entry<Fi> e : entries) {
            if (e.getOut() instanceof MetaFi) {
                metafidelities.put(e.getName(), (MetaFi) e.getData());
            } else {
                fidelities.put(e.getName(), (Fidelity) e.getData());
            }
        }
    }

    public Projection getProjection(String projectionName) {
        return projections.get(projectionName);
    }

    public boolean isTraced() {
        return isTraced;
    }

    public void setTraced(boolean traced) {
        isTraced = traced;
    }


    @Override
    public Uuid getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Contextion getChild() {
        return child;
    }

    public void setChild(Contextion child) {
        this.child = child;
    }

    protected void registerMorphFidelity(MorphFidelity morphFi) {
        try {
           morphFi.addObserver(this);
            if (morphFi.getMorpherFidelity() != null) {
                // set the default morpher
                morphFi.setMorpher(( Morpher ) (( Entry ) morphFi.getMorpherFidelity().get(0)).getValue());
            }
        } catch (ContextException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Observable observable, Object obj) throws EvaluationException, RemoteException {
        // implement in subclasses and use the morphers provided by MorphedFidelities (observables)

        MorphFidelity mFi = (MorphFidelity)observable;
        Morpher morpher = null;
        if (mFi.getFiType().equals(Fi.Type.IN)) {
            morpher = mFi.getInMorpher();
        } else {
            morpher = mFi.getMorpher();
        }
        if (morpher != null)
            try {
                morpher.morph(this, mFi, obj);
            } catch (ConfigurationException | ServiceException e) {
                throw new EvaluationException(e);
            }
    }

    @Override
    public Object execute(Arg... args) throws MogramException, RemoteException {
        return null;
    }

    @Override
    public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException {

    }

    static class Session {
        long eventID;
        Object source;
        long leaseLenght;
        long seqNum = 0;
        RemoteEventListener listener;
        String path;

        Session(long eventID, Object source, String path,
                RemoteEventListener toInform, long leaseLenght) {
            this.eventID = eventID;
            this.source = source;
            this.leaseLenght = leaseLenght;
            this.listener = toInform;
            this.path = path;
        }
    }

}
