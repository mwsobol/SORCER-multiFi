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
import sorcer.service.modeling.ExecutiveException;
import sorcer.service.modeling.Functionality;

import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Date;
import java.util.List;

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
            domain = (Domain)((LocalSignature) signature).initInstance();
            // domain = (Domain) sorcer.co.operator.instance(signature);
            if (name != null) {
                domain.setDomainName(name);
            } else {
                domain.setDomainName(domain.getName());
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
    public Object asis(String path) throws ContextException {
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
    public void setParent(Contextion parent) {
        if (domain == null) {
            this.parent = parent;
        } else {
            domain.setParent(parent);
        }
    }

    @Override
    public Uuid getId() {
        return domain.getId();
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

    @Override
    public void setId(Uuid id) {
        domain.setId(id);
    }

    @Override
    public <T extends Contextion> T exert(Arg... args) throws ServiceException {
        try {
            return domain.exert(args);
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public int getIndex() {
        return domain.getIndex();
    }

    @Override
    public void setIndex(int i) {
        domain.setIndex(i);
    }

    @Override
    public Contextion getParent() {
        return domain.getParent();
    }

    @Override
    public void setParentId(Uuid parentId) {
        domain.setParentId(parentId);
    }

    @Override
    public Signature getProcessSignature() {
        return domain.getProcessSignature();
    }

    @Override
    public Mogram deploy(List<Signature> builders) throws ServiceException, ConfigurationException {
        return domain.deploy(builders);
    }

    @Override
    public int getStatus() {
        return domain.getStatus();
    }

    @Override
    public void setStatus(int value) {
        domain.setStatus(value);
    }

    @Override
    public Object get(String key) {
        return domain.get(key);
    }

    @Override
    public Mogram clearScope() throws MogramException {
        return domain.clearScope();
    }

    @Override
    public Mogram clear() throws MogramException {
        return domain.clear();
    }

    @Override
    public void reportException(Throwable t) {
        domain.reportException(t);
    }

    @Override
    public List<ThrowableTrace> getExceptions() {
        return domain.getExceptions();
    }

    @Override
    public void reportException(String message, Throwable t) {
        domain.reportException(message,t);
    }

    @Override
    public void reportException(String message, Throwable t, ProviderInfo info) {
        domain.reportException(message, t, info);
    }

    @Override
    public void reportException(String message, Throwable t, Exerter provider) {
        domain.reportException(message, t, provider);
    }

    @Override
    public void reportException(String message, Throwable t, Exerter provider, ProviderInfo info) {
        domain.reportException(message, t, provider, info);
    }

    @Override
    public List<String> getTrace() throws RemoteException {
        return domain.getTrace();
    }

    @Override
    public void appendTrace(String info) throws RemoteException {
        domain.appendTrace(info);
    }

    @Override
    public List<ThrowableTrace> getAllExceptions() throws RemoteException {
        return domain.getAllExceptions();
    }

    @Override
    public Fidelity selectFidelity(String selection) throws ConfigurationException {
        return domain.selectFidelity(selection);
    }

    @Override
    public Fidelity getSelectedFidelity() {
        return domain.getSelectedFidelity();
    }

    @Override
    public FidelityManagement getFidelityManager() {
        return domain.getFidelityManager();
    }

    @Override
    public FidelityManagement getRemoteFidelityManager() throws RemoteException {
        return domain.getRemoteFidelityManager();
    }

    @Override
    public boolean isMonitorable() throws RemoteException {
        return domain.isMonitorable();
    }

    @Override
    public Uuid getParentId() {
        return domain.getParentId();
    }

    @Override
    public Date getCreationDate() {
        return domain.getCreationDate();
    }

    @Override
    public Date getGoodUntilDate() {
        return domain.getGoodUntilDate();
    }

    @Override
    public void setGoodUntilDate(Date date) {
        domain.setGoodUntilDate(date);
    }

    @Override
    public String getDomainId() {
        return domain.getDomainId();
    }

    @Override
    public void setDomainId(String id) {
        domain.setDomainId(id);
    }

    @Override
    public String getSubdomainId() {
        return domain.getSubdomainId();
    }

    @Override
    public void setSubdomainId(String id) {
        domain.setSubdomainName(id);
    }

    @Override
    public String getDomainName() {
        if (domain != null) {
            return domain.getDomainName();
        } else {
            return name;
        }
    }

    @Override
    public void setDomainName(String name) {
        domain.setSubdomainName(name);
    }

    @Override
    public String getSubdomainName() {
        return domain.getSubdomainName();
    }

    @Override
    public Object getEvaluatedValue(String path) throws ContextException {
        return domain.getEvaluatedValue(path);
    }

    @Override
    public boolean isEvaluated() {
        return domain.isEvaluated();
    }

    @Override
    public void setSubdomainName(String name) {
        domain.setSubdomainName(name);
    }

    @Override
    public Principal getPrincipal() {
        return domain.getPrincipal();
    }

    @Override
    public Date getLastUpdateDate() {
        return domain.getLastUpdateDate();
    }

    @Override
    public void setLastUpdateDate(Date date) {
        domain.setLastUpdateDate(date);
    }

    @Override
    public void setDescription(String description) {
        domain.setDescription(description);
    }

    @Override
    public String getDescription() {
        return domain.getDescription();
    }

    @Override
    public String getOwnerId() {
        return domain.getOwnerId();
    }

    @Override
    public String getSubjectId() {
        return domain.getSubjectId();
    }

    @Override
    public void setProjectName(String projectName) {
        domain.setProjectName(projectName);
    }

    @Override
    public String getProjectName() {
        return domain.getProjectName();
    }

    @Override
    public boolean isValid() {
        return domain.isValid();
    }

    @Override
    public void setValid(boolean state) {
        domain.setValid(state);
    }

    @Override
    public Context getDataContext() throws ContextException {
        return domain.getDataContext();
    }

    @Override
    public void reconfigure(Fidelity... fidelities) throws ContextException, RemoteException, ConfigurationException {
        domain.reconfigure(fidelities);
    }

    @Override
    public void morph(String... metaFiNames) throws ContextException, RemoteException, ConfigurationException {
        domain.morph(metaFiNames);
    }

    @Override
    public void update(Setup... contextEntries) throws ContextException, RemoteException {
        domain.update(contextEntries);
    }

    @Override
    public String getProjectionFi(String projectionName) throws ContextException, RemoteException {
        return domain.getProjectionFi(projectionName);
    }

    @Override
    public boolean isExportControlled() {
        return domain.isExportControlled();
    }

    @Override
    public List<Discipline> getMograms(List<Discipline> allMograms) {
        return domain.getMograms(allMograms);
    }

    @Override
    public List<Discipline> getMograms() {
        return domain.getMograms();
    }

    @Override
    public List<Contextion> getContextions() {
        return domain.getContextions();
    }

    @Override
    public List<Discipline> getAllMograms() {
        return domain.getAllMograms();
    }

    @Override
    public List<Contextion> getAllContextions() {
        return domain.getAllContextions();
    }

    @Override
    public Signature getBuilder(Arg... args) throws MogramException {
        return domain.getBuilder(args);
    }

    @Override
    public void applyFidelity(String name) {
        domain.applyFidelity(name);
    }

    @Override
    public void setBuilder(Signature builder) throws MogramException {
        domain.setBuilder(builder);
    }

    @Override
    public boolean isConditional() {
        return domain.isConditional();
    }

    @Override
    public boolean isCompound() {
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
    public void execDependencies(String path, Arg... args) throws ContextException {
        domain.execDependencies(path, args);
    }

    @Override
    public boolean isChanged() {
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
    public <T extends Contextion> T exert(Transaction txn, Arg... args) throws MogramException {
        try {
            return domain.exert(txn, args);
        } catch (RemoteException e) {
            throw new MogramException(e);
        }
    }

    @Override
    public Context getContext() throws ContextException {
        return domain.getContext();
    }

    @Override
    public Context getOutput(Arg... args) throws ContextException {
        return domain.getOutput(args);
    }

    @Override
    public void setContext(Context input) throws ContextException {
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
    public Context getContext(String path) throws ContextException {
        return domain.getContext();
    }

    @Override
    public Context.Return getContextReturn() {
        return domain.getContextReturn();
    }

    @Override
    public ServiceStrategy getDomainStrategy() {
        return domain.getDomainStrategy();
    }

    @Override
    public Projection getInPathProjection() {
        return domain.getInPathProjection();
    }

    @Override
    public Projection getOutPathProjection() {
        return domain.getOutPathProjection();
    }

    @Override
    public List<Contextion> getContextions(List<Contextion> contextionList) {
        return domain.getContextions(contextionList);
    }

    @Override
    public void selectFidelity(Fidelity fi) throws ConfigurationException {
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
    public <T extends Contextion> T exert(T exertion, Transaction txn, Arg... args) throws ServiceException {
        try {
            return domain.exert(exertion, txn, args);
        } catch (RemoteException e) {
            throw new MogramException(e);
        }
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
    public void substitute(Arg... entries) throws SetterException, RemoteException {
        domain.substitute(entries);
    }

    @Override
    public Context develop(Context context, Arg... args) throws ServiceException, ExecutiveException, RemoteException {
        return null;
    }

    @Override
    public Fidelity<Development> getDeveloperFi() {
        return null;
    }
}
