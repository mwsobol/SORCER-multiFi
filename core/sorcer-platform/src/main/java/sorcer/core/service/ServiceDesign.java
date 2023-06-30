/*
 * Copyright 2021 the original author or authors.
 * Copyright 2021 SorcerSoft.org.
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
package sorcer.core.service;

import net.jini.core.transaction.Transaction;
import sorcer.core.context.Intent;
import sorcer.core.context.ModelStrategy;
import sorcer.core.context.ModelTask;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.ent.cntrl.Developer;
import sorcer.core.signature.LocalSignature;
import sorcer.service.*;
import sorcer.service.modeling.ExecutiveException;
import sorcer.service.modeling.Finalization;
import sorcer.service.modeling.Initialization;

import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * A service design is a design class holding its transdiscipline, intent context,
 * developer (development controller), and morphers.
 *
 * @author Mike Sobolewski 02/27/2023
 */
public class ServiceDesign extends MultiFiSlot implements Design {

    // transdiscipline
    private Contextion discipline;
    private Context disciplineIntent;

    private Context developmentIntent;
    private Fi developmentFi;

    private Context designIntent;
    private Map<String, Projection> projections;

    // the output of this collaboration
    protected Context output;

    protected Fidelity<Finalization> finalizerFi;

    protected Fidelity<Initialization> initializerFi;

    public void setDevelopmentFi(ServiceFidelity developmentFi) {
        this.developmentFi = developmentFi;
    }

    protected ServiceStrategy serviceStrategy;

    private static int count = 0;

    public ServiceDesign(String name) {
        if (name == null) {
            this.key = getClass().getSimpleName() + "-" + count++;
        } else {
            this.key = name;
        }
        serviceStrategy = new ModelStrategy(this);
    }

    public ServiceDesign(Discipline disc) {
        discipline = disc;
        this.key = disc.getName();
        serviceStrategy = new ModelStrategy(this);
    }

    public ServiceDesign(String name, Context designIntent) throws SignatureException {
        this(name);
        this.designIntent = designIntent;
        Intent dznCxt = (Intent)designIntent;
        discipline = dznCxt.getDiscipline();
        Signature discIntentSig = dznCxt.getDisciplineIntentSignature();
        if (discIntentSig != null) {
            disciplineIntent = (Context) ((LocalSignature)discIntentSig).initInstance();
        } else {
            disciplineIntent = dznCxt.getDisciplineIntent();
        }
        developmentFi = dznCxt.getControllingFi();
        if (developmentFi == null) {
            setDeveloperFi(designIntent);
        }
        developmentIntent = dznCxt.getDevelopmentIntent();
    }

    public ServiceDesign(String name, Discipline discipline, Context disciplineIntent, Development developer ) {
        this(name);
        this.discipline = discipline;
        this.disciplineIntent = disciplineIntent;
        developmentFi = new ServiceFidelity(name);
        developmentFi.addSelect((Developer)developer);
        developmentFi.setSelect((Developer)developer);
        ((Developer)developer).setDiscipline(discipline);
    }

    public Fi setDeveloperFi(Context context) {
        if(developmentFi == null) {
            Object devComponent = ((ServiceContext)context).get(Context.DEV_PATH);
            if (devComponent != null) {
                if (devComponent instanceof Development) {
                    developmentFi = new ServiceFidelity(((Developer)devComponent).getName());
                    developmentFi.addSelect((Developer) devComponent);
                    developmentFi.setSelect((Developer)devComponent);
                    ((Developer)devComponent).setDiscipline(discipline);
                } else if (devComponent instanceof ServiceFidelity
                    && ((ServiceFidelity) devComponent).getFiType().equals(Fi.Type.DEV)) {
                    developmentFi = (ServiceFidelity) devComponent;
                    ((Developer) developmentFi.getSelect()).setDiscipline(discipline);
                }
            }
        }
        ((ServiceContext)context).getDomainStrategy().setExecState(Exec.State.INITIAL);
        if (output == null) {
            output = new ServiceContext(getName());
        }
        return developmentFi;
    }

    // get a discipline intent from the designIntent for a given select fidelity in itens
    public Object getIntentContext(Fidelity fi) throws ConfigurationException {
        Object obj = null;
        // select intFi fidelity intFi(fidelity, multiFiIntent, "designIntent")
        if (((Fidelity)fi).getFiType().equals(Fi.Type.INTENT)) {
            obj = ((ServiceFidelity)designIntent.getMultiFi()).selectSelect((String) fi.getSelect());
            if (obj instanceof Fidelity && fi.getFiType().equals(Fi.Type.INTENT)) {
                obj = ((ServiceFidelity)obj).selectSelect(fi.getPath());
            }
            if (obj instanceof Fidelity && fi.getFiType().equals(Fi.Type.INTENT)) {
                obj = ((Fidelity)obj).getSelect();
            }
            if (obj instanceof ServiceContext && ((ServiceContext)obj).getMultiFi().size()>0) {
                obj = ((ServiceContext)obj).getMultiFi().selectSelect(fi.getName());
            }
            if (obj instanceof Fidelity && fi.getFiType().equals(Fi.Type.INTENT)) {
                obj = ((Fidelity)obj).getSelect();
            }
        }

        return obj;
    }

    public Map<String, Projection> getProjections() {
        return projections;
    }

    public void setProjections(Map<String, Projection> projections) {
        this.projections = projections;
    }

    public Context getDesignIntent() {
        return designIntent;
    }

    public void setDesignIntent(Context designIntent) {
        this.designIntent = designIntent;
    }

    @Override
    public Context evaluate(Context context, Arg... args) throws ServiceException, RemoteException {
        return discipline.evaluate(context, args);
    }

    @Override
    public <T extends Contextion> T exert(Arg... args) throws ServiceException, RemoteException {
        return null;
    }

    @Override
    public <T extends Contextion> T exert(Transaction txn, Arg... args) throws ServiceException, RemoteException {
         return (T) discipline.evaluate(disciplineIntent, args);
    }

    @Override
    public String getDomainName() {
        return (String) key;
    }

    @Override
    public Context getOutput(Arg... args) throws ContextException {
        return output;
    }

    public Context getDisciplineIntent() {
        return disciplineIntent;
    }

    public void setDisciplineIntent(Context disciplineIntent) {
        this.disciplineIntent = disciplineIntent;
    }

    @Override
    public void setContext(Context input) throws ContextException {
        developmentIntent = input;
    }

    @Override
    public Context appendContext(Context context) throws ContextException, RemoteException {
        return developmentIntent;
    }

    @Override
    public Context getDomainData() throws ContextException, RemoteException {
        return disciplineIntent;
    }

    @Override
    public Context getContext(Context contextTemplate) throws ContextException, RemoteException {
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
    public Projection getInPathProjection() {
        return null;
    }

    @Override
    public Projection getOutPathProjection() {
        return null;
    }

    @Override
    public List<Contextion> getContextions(List<Contextion> contextionList) {
        contextionList.add(discipline);
        return contextionList;
    }

    @Override
    public void setParent(Contextion parent) {

    }

    @Override
    public Contextion getParent() {
        return null;
    }

    @Override
    public Context getContext() throws ContextException {
        return developmentIntent;
    }

    @Override
    public Type getArchitectureType() throws RemoteException {
        Contextion obj = getDiscipline();
        if (obj instanceof ServiceNode) {
            return (( ModelTask )((ServiceNode)obj).getOutDispatcher()).getModel().getClass();
        }
        return getDiscipline().getClass();
    }

    @Override
    public Contextion getDiscipline() throws RemoteException {
        return discipline;
    }

    public void setDiscipline(Discipline discipline) {
        this.discipline = discipline;
    }

    @Override
    public Fi getDevelopmentFi() throws RemoteException {
        return developmentFi;
    }

    @Override
    public Context design(Discipline discipline, Context context) throws DesignException, RemoteException {
       this.discipline = discipline;
        try {
            return ((Development) developmentFi.getSelect()).develop(discipline, developmentIntent);
        } catch (ServiceException | ExecutiveException e) {
            throw new DesignException(e);
        }
    }

    @Override
    public Fidelity<Initialization> getInitializerFi() {
        return initializerFi;
    }

    @Override
    public Fidelity<Finalization> getFinalizerFi() {
        return finalizerFi;
    }

    @Override
    public void setName(String name) {
        key = name;
    }
}