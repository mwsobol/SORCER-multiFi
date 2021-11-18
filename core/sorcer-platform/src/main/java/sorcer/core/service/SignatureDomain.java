/*
 * Copyright 2020 the original author or authors.
 * Copyright 2020 SorcerSoft.org.
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
import net.jini.id.Uuid;
import sorcer.core.context.ThrowableTrace;
import sorcer.core.signature.LocalSignature;
import sorcer.service.*;
import sorcer.service.modeling.Functionality;

import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Date;
import java.util.List;

/**
 * @author Mike Sobolewski
 */
public class SignatureDomain implements Domain {

    String name;
    private Signature signature;
    private Domain domain;
    protected Contextion parent;
    protected boolean isExec = true;

    public SignatureDomain() {

    }

    public SignatureDomain(String name) {
        this.name = name;
    }

    public SignatureDomain(String name, Signature signature) {
        this.name = name;
        this.signature = signature;
    }

    public SignatureDomain(Signature signature) {
        this.signature = signature;
        this.name = signature.getName();
    }

    public Domain getDomain() throws SignatureException {
        if (domain == null) {
            domain = (Domain) ((LocalSignature) signature).initInstance();
            // domain = (Domain) sorcer.co.operator.instance(signature);
            if (name != null) {
                domain.setName(name);
            } else {
                domain.setName(domain.getName());
            }
        }
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    @Override
    public Object asis(String path) throws ContextException, RemoteException {
        return domain.asis((path));
    }

    @Override
    public Context getInConnector(Arg... args) throws ContextException, RemoteException {
        return domain.getInConnector(args);
    }

    @Override
    public Context getOutConnector(Arg... args) throws ContextException, RemoteException {
        return domain.getOutConnector(args);
    }

    @Override
    public Object getValue(String path, Arg... args) throws ContextException, RemoteException {
        return domain.getValue(path, args);
    }

    @Override
    public void setParent(Contextion parent) throws RemoteException {
        if (domain == null) {
            this.parent = parent;
        } else {
            domain.setParent(parent);
        }
    }

    @Override
    public Uuid getId() {
        return ((ServiceMogram)domain).getId();
    }

    @Override
    public String getName() {
        if (name != null) {
            return name;
        } else if (domain != null) {
            name = domain.getName();
        } else if (signature != null) {
            name = signature.getName();
        }
        return name;
    }

    public void setId(Uuid id) {
        ((ServiceMogram)domain).setId(id);
    }

    @Override
    public <T extends Contextion> T exert(Arg... args) throws ServiceException {
        try {
            return domain.exert(args);
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    public int getIndex() {
        return ((ServiceMogram)domain).getIndex();
    }

    public void setIndex(int i) {
        ((ServiceMogram)domain).setIndex(i);
    }

    public Contextion getParent() throws RemoteException {
        return ((ServiceMogram)domain).getParent();
    }

    public void setParentId(Uuid parentId) {
        ((ServiceMogram)domain).setParentId(parentId);
    }

    public Signature getProcessSignature() throws RemoteException {
        return ((ServiceMogram)domain).getProcessSignature();
    }

    public Mogram deploy(List<Signature> builders) throws ServiceException, ConfigurationException {
        return ((ServiceMogram)domain).deploy(builders);
    }

    public int getStatus() {
        return ((ServiceMogram)domain).getStatus();
    }

    public void setStatus(int value) {
        ((ServiceMogram)domain).setStatus(value);
    }

    public Object get(String key) {
        return ((ServiceMogram)domain).get(key);
    }

    public Mogram clearScope() throws MogramException {
        return ((ServiceMogram)domain).clearScope();
    }

    public Mogram clear() throws MogramException {
        return ((ServiceMogram)domain).clear();
    }

    public void reportException(Throwable t) {
        ((ServiceMogram)domain).reportException(t);
    }

    public List<ThrowableTrace> getExceptions() throws RemoteException {
        return domain.getExceptions();
    }

    public void reportException(String message, Throwable t) {
        ((ServiceMogram)domain).reportException(message, t);
    }

    public void reportException(String message, Throwable t, ProviderInfo info) {
        ((ServiceMogram)domain).reportException(message, t, info);
    }

    public void reportException(String message, Throwable t, Exerter provider) {
        ((ServiceMogram)domain).reportException(message, t, provider);
    }

    public void reportException(String message, Throwable t, Exerter provider, ProviderInfo info) {
        ((ServiceMogram)domain).reportException(message, t, provider, info);
    }

    public List<String> getTrace() throws RemoteException {
        return domain.getTrace();
    }

    public void appendTrace(String info) throws RemoteException {
        ((ServiceMogram)domain).appendTrace(info);
    }

    @Override
    public List<ThrowableTrace> getAllExceptions() throws RemoteException {
        return domain.getAllExceptions();
    }

    public Fi selectFidelity(String selection) throws ConfigurationException {
        return ((ServiceMogram)domain).selectFidelity(selection);
    }

    public Fidelity getSelectedFidelity() {
        return ((ServiceMogram)domain).getSelectedFidelity();
    }

    public FidelityManagement getFidelityManager() {
        return ((ServiceMogram)domain).getFidelityManager();
    }

    public FidelityManagement getRemoteFidelityManager() throws RemoteException {
        return ((ServiceMogram)domain).getRemoteFidelityManager();
    }

    @Override
    public boolean isMonitorable() throws RemoteException {
        return domain.isMonitorable();
    }

    public Uuid getParentId() {
        return ((ServiceMogram)domain).getParentId();
    }

    @Override
    public Date getCreationDate() throws RemoteException {
        return domain.getCreationDate();
    }

    public Date getGoodUntilDate() {
        return ((ServiceMogram)domain).getGoodUntilDate();
    }

    public void setGoodUntilDate(Date date) {
        ((ServiceMogram)domain).setGoodUntilDate(date);
    }

    public String getDomainId() {
        return ((ServiceMogram)domain).getDomainId();
    }

    public void setDomainId(String id) {
        ((ServiceMogram)domain).setDomainId(id);
    }

    public String getSubdomainId() {
        return ((ServiceMogram)domain).getSubdomainId();
    }

    public void setSubdomainId(String id) {
        ((ServiceMogram)domain).setSubdomainName(id);
    }

    @Override
    public String getDomainName() throws RemoteException {
        if (domain != null) {
            return domain.getDomainName();
        } else {
            return name;
        }
    }

    public void setDomainName(String name) {
        ((ServiceMogram)domain).setSubdomainName(name);
    }

    public String getSubdomainName() {
        return ((ServiceMogram)domain).getSubdomainName();
    }

    public Object getEvaluatedValue(String path) throws ContextException {
        return ((ServiceMogram)domain).getEvaluatedValue(path);
    }

    public boolean isEvaluated() {
        return ((ServiceMogram)domain).isEvaluated();
    }

    public void setSubdomainName(String name) {
        ((ServiceMogram)domain).setSubdomainName(name);
    }

    public Principal getPrincipal() {
        return ((ServiceMogram)domain).getPrincipal();
    }

    public Date getLastUpdateDate() {
        return ((ServiceMogram)domain).getLastUpdateDate();
    }

    public void setLastUpdateDate(Date date) {
        ((ServiceMogram)domain).setLastUpdateDate(date);
    }

    public void setDescription(String description) {
        ((ServiceMogram)domain).setDescription(description);
    }

    public String getDescription() {
        return ((ServiceMogram)domain).getDescription();
    }

    public String getOwnerId() {
        return ((ServiceMogram)domain).getOwnerId();
    }

    public String getSubjectId() {
        return ((ServiceMogram)domain).getSubjectId();
    }

    public void setProjectName(String projectName) {
        ((ServiceMogram)domain).setProjectName(projectName);
    }

    public String getProjectName() {
        return ((ServiceMogram)domain).getProjectName();
    }

    public boolean isValid() {
        return ((ServiceMogram)domain).isValid();
    }

    public void setValid(boolean state) {
        ((ServiceMogram)domain).setValid(state);
    }

    public Context getDataContext() throws ContextException {
        return ((ServiceMogram)domain).getDataContext();
    }

    @Override
    public void reconfigure(Fidelity... fidelities) throws ContextException, RemoteException, ConfigurationException {
        domain.reconfigure(fidelities);
    }

    @Override
    public void project(String... metaFiNames) throws ContextException, RemoteException, ConfigurationException {
        domain.project(metaFiNames);
    }

    @Override
    public void update(Setup... contextEntries) throws ContextException, RemoteException {
        domain.update(contextEntries);
    }

//    public String getProjectionFi(String projectionName) throws ContextException, RemoteException {
//        return ((ServiceMogram)domain).getProjectionFi(projectionName);
//    }

    public boolean isExportControlled() {
        return ((ServiceMogram)domain).isExportControlled();
    }

    public List<Contextion> getMograms(List<Contextion> allMograms) {
        return ((ServiceMogram)domain).getMograms(allMograms);
    }

    public List<Contextion> getMograms() {
        return ((ServiceMogram)domain).getMograms();
    }

    public List<Contextion> getContextions() {
        return ((ServiceMogram)domain).getContextions();
    }

    public List<Contextion> getAllMograms() throws RemoteException {
        return ((ServiceMogram)domain).getAllMograms();
    }

    public List<Contextion> getAllContextions() throws RemoteException {
        return ((ServiceMogram)domain).getAllContextions();
    }

    public Signature getBuilder(Arg... args) throws ServiceException, RemoteException {
        return ((ServiceMogram)domain).getBuilder(args);
    }

    public void applyFidelity(String name) throws RemoteException {
        ((ServiceMogram)domain).applyFidelity(name);
    }

    public void setBuilder(Signature builder) throws ServiceException, RemoteException {
        ((ServiceMogram)domain).setBuilder(builder);
    }

    @Override
    public boolean isConditional() throws RemoteException {
        return domain.isConditional();
    }

    @Override
    public boolean isCompound() throws RemoteException {
        return domain.isCompound();
    }

    @Override
    public boolean isExec() {
        return isExec;
    }

    public void setExec(boolean exec) {
        this.isExec = exec;
    }

    @Override
    public void execDependencies(String path, Arg... args) throws ContextException, RemoteException {
        domain.execDependencies(path, args);
    }

    @Override
    public boolean isChanged() throws ContextException, RemoteException {
        return domain.isChanged();
    }

    @Override
    public Context evaluate(Context context, Arg... args) throws ServiceException {
        try {
            return domain.evaluate(context, args);
        } catch (RemoteException e) {
            throw new MogramException(e);
        }
    }

    @Override
    public <T extends Contextion> T exert(Transaction txn, Arg... args) throws ServiceException {
        try {
            return domain.exert(txn, args);
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public Context getContext() throws ContextException, RemoteException {
        return domain.getContext();
    }

    @Override
    public Context getOutput(Arg... args) throws ContextException, RemoteException {
        return domain.getOutput(args);
    }

    @Override
    public void setContext(Context input) throws ContextException, RemoteException {
        domain.setContext(input);
    }

    @Override
    public Context appendContext(Context context) throws ContextException {
        try {
            return domain.appendContext(context);
        } catch (RemoteException e) {
            throw new ContextException(e);
        }
    }

    @Override
    public Context getDomainData() throws ContextException {
        try {
            return domain.getDomainData();
        } catch (RemoteException e) {
            throw new ContextException(e);
        }
    }

    @Override
    public Context getContext(Context contextTemplate) throws ContextException {
        try {
            return domain.getContext(contextTemplate);
        } catch (RemoteException e) {
            throw new ContextException(e);
        }
    }

    @Override
    public Context appendContext(Context context, String path) throws ContextException {
        try {
            return domain.appendContext(context, path);
        } catch (RemoteException e) {
            throw new ContextException(e);
        }
    }

    @Override
    public Context getContext(String path) throws ContextException, RemoteException {
        return domain.getContext();
    }

    @Override
    public Context.Return getContextReturn() throws RemoteException {
        return domain.getContextReturn();
    }

    @Override
    public ServiceStrategy getDomainStrategy() throws RemoteException {
        return domain.getDomainStrategy();
    }

    @Override
    public Projection getInPathProjection() throws RemoteException {
        return domain.getInPathProjection();
    }

    @Override
    public Projection getOutPathProjection() throws RemoteException {
        return domain.getOutPathProjection();
    }

    @Override
    public List<Contextion> getContextions(List<Contextion> contextionList) throws RemoteException {
        return domain.getContextions(contextionList);
    }

    @Override
    public void selectFidelity(Fi fi) throws ConfigurationException, RemoteException {
        domain.selectFidelity(fi);
    }

    @Override
    public void addDependers(Evaluation... dependers) {
        domain.addDependers(dependers);
    }

    @Override
    public List<Evaluation> getDependers() {
        return domain.getDependers();
    }

    @Override
    public Functionality.Type getDependencyType() {
        return domain.getDependencyType();
    }

    @Override
    public void setName(String name) {
        domain.setName(name);
    }

    @Override
    public Fi getMultiFi() {
        return domain.getMultiFi();
    }

    @Override
    public Morpher getMorpher() {
        return domain.getMorpher();
    }

    @Override
    public Context getScope() {
        return domain.getScope();
    }

    @Override
    public void setScope(Context scope) {
        domain.setScope(scope);
    }

    @Override
    public Object execute(Arg... args) throws ServiceException, RemoteException {
        return domain.execute(args);
    }

    @Override
    public <T extends Contextion> T exert(T exertion, Transaction txn, Arg... args) throws ServiceException, RemoteException {
        return null;
    }

    @Override
    public void substitute(Arg... entries) throws SetterException, RemoteException {
        domain.substitute(entries);
    }

    @Override
    public List<Signature> getAllSignatures() throws RemoteException {
        return null;
    }
}
