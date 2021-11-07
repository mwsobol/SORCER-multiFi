/*
 * Copyright 2018 the original author or authors.
 * Copyright 2018 SorcerSoft.org.
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

package sorcer.service;

import net.jini.core.transaction.Transaction;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import sorcer.core.context.ModelStrategy;
import sorcer.core.context.ServiceContext;
import sorcer.core.signature.LocalSignature;
import sorcer.service.modeling.Functionality;
import sorcer.service.modeling.Getter;
import sorcer.service.modeling.Model;

import java.rmi.RemoteException;
import java.util.*;

/**
 *  Implements a service discipline as out-multiFi-dispatch
 */
public class ServiceNode extends MultiFiSlot<String, Object> implements Node, Getter<Object> {

    protected Uuid disciplineId;

    protected String  name;

    // the input of this discipline
    protected Context input;

    // the output of this discipline
    protected Context output;

    protected Context inConnector;

    protected Context outConnector;

    // the executed contextion
    protected Service out;

    // the executed dispatcher
    protected Routine outDispatcher;

    protected Task precondition;

    protected Task postcondition;

    protected Signature builder;

    protected Morpher morpher;

    protected Contextion parent;

    // dependency management for this disciline
    protected List<Evaluation> dependers = new ArrayList<>();

    // default instance new Return(Context.RETURN);
    protected Context.Return contextReturn;

    protected Projection inPathProjection;

    protected Projection outPathProjection;

    protected ServiceStrategy serviceStrategy;

    protected boolean isExec = true;

    public ServiceNode() {
        disciplineId = UuidFactory.generate();
        serviceStrategy = new ModelStrategy(this);
        multiFi = new ServiceFidelity();
    }

    public ServiceNode(String name) {
        this.name = name;
        disciplineId = UuidFactory.generate();
        serviceStrategy = new ModelStrategy(this);
        multiFi = new ServiceFidelity();
        ((ServiceFidelity)multiFi).setName(name);
    }

    public ServiceNode(String name, NodeFi[] fidelities) {
        this(name);
        multiFi.getSelects().addAll(Arrays.asList(fidelities));
    }

    @Override
    public Service getContextion() {
        return (( NodeFi )multiFi.getSelect()).getContextion();
    }

    @Override
    public Context getInput() {
        return input;
    }

    public Service getOutContextion() {
        return out;
    }

    public Context getContextionContext() throws ContextException, RemoteException {
        return ((Contextion)out).getContext();
    }

    @Override
    public Routine getDispatcher() {
        return (( NodeFi )multiFi.getSelect()).getDispatcher();
    }

    public Context setInput(Context input) throws ContextException {
        this.input = input;
        return input;
    }

    @Override
    public Context getOutput(Arg... args) throws ContextException {
        if (outDispatcher == null) {
            try {
                execute(args);
            } catch (ServiceException e) {
                throw new ContextException(e);
            }
        }
        Context out = null;
        if (outConnector != null) {
            if (outDispatcher instanceof Context) {
                out = ((ServiceContext) outDispatcher).updateContextWith(outConnector);
            } else if (outDispatcher instanceof Mogram) {
                if (outConnector != null)
                    out = ((ServiceContext) outDispatcher.getContext()).updateContextWith(outConnector);
            }
        } else {
            if (outDispatcher instanceof Context) {
                out = (Context) outDispatcher;
            } else if (outDispatcher instanceof Mogram) {
                out = outDispatcher.getContext();
            }
        }
        if (output == null) {
            output = out;
        } else if (out != null) {
            output.append(out);
        }

        return output;
    }

    @Override
    public void setContext(Context input) throws ContextException {
        this.input = input;
    }

    @Override
    public Context appendContext(Context context) throws ContextException, RemoteException {
        return input.appendContext(context);
    }

    public Routine getOutDispatcher() {
        return outDispatcher;
    }

    @Override
    public Signature getBuilder() {
        return builder;
    }

    @Override
    public Context getInConnector() {
        return inConnector;
    }

    @Override
    public void setInConnector(Context inConnector) {
        this.inConnector = inConnector;
    }

    @Override
    public Context getOutConnector() {
        return outConnector;
    }

    @Override
    public void setOutConnector(Context inConnector) {
        this.outConnector = inConnector;
    }

    public void setBuilder(Signature builder) {
        this.builder = builder;
    }

    @Override
    public Object execute(Arg... args) throws ServiceException {
        try {
            if (out != null) {
                clear();
            }
            List<Fi> fis = Arg.selectFidelities(args);
            if (fis.size() > 0) {
                try {
                    selectFidelity(fis.get(0));
                } catch (ConfigurationException e) {
                    throw new ServiceException(e);
                }
            }
            Routine xrt = getDispatcher();
            Context cxt = (( NodeFi )multiFi.getSelect()).getContext();
            if (cxt != null && xrt != null) {
                xrt.setContext(cxt);
            }
            if (input != null) {
                if (inConnector != null) {
                    xrt.setContext(((ServiceContext) input).updateContextWith(inConnector));
                } else {
                    xrt.setContext(input);
                }
            }
            out = this.getContextion();
            if (out != null && xrt != null) {
                xrt.dispatch(out);
            }
            // realize contextion if not dispatched
            if (out instanceof LocalSignature && cxt != null) {
                try {
                    out = (Contextion) ((LocalSignature)out).initInstance();
                    if (out instanceof Model) {
                    xrt.setContext(cxt);
                        if (xrt.getProcessSignature() instanceof LocalSignature) {
                            ((LocalSignature) xrt.getProcessSignature()).setTarget(out);
                        }
                    }
                } catch (SignatureException e) {
                    throw new ConfigurationException(e);
                }
            }
            if (xrt != null) {
                outDispatcher = xrt.exert();
            } else if (out != null) {
                output = ((Contextion)out).evaluate(cxt, args);
            }

            return getOutput();
        } catch (DispatchException | ConfigurationException | RemoteException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void selectFidelity(Fi fi) throws ConfigurationException {
        multiFi.selectSelect(fi.getName());
    }

    @Override
    public Context.Return getContextReturn() {
        return contextReturn;
    }

    @Override
    public ServiceStrategy getDomainStrategy() {
        return serviceStrategy;
    }

    public void setContextReturn() {
        this.contextReturn = new Context.Return();
    }

    public void setContextReturn(String returnPath) {
        this.contextReturn = new Context.Return(returnPath);
    }

    public void setContextReturn(Context.Return contextReturn) {
        this.contextReturn = contextReturn;
    }

    public void setContextReturn(String path, Signature.Direction direction) {
        contextReturn = new Context.Return(path, direction);
    }

    public Task getPrecondition() {
        return precondition;
    }

    public void setPrecondition(Task precondition) {
        this.precondition = precondition;
    }

    public Task getPostcondition() {
        return postcondition;
    }

    public void setPostcondition(Task postcondition) {
        this.postcondition = postcondition;
    }

    @Override
    public Service getValue(Arg... args) throws ContextException {
        return out;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Morpher getMorpher() {
        return morpher;
    }

    public void setMorpher(Morpher morpher) {
        this.morpher = morpher;
    }

    public Context<Object> getContext(String dscName) throws ContextException {
        try {
            return (( NodeFi ) multiFi.selectSelect(dscName)).getContext();
        } catch (ConfigurationException e) {
            throw new ContextException(e);
        }
    }

    public Context<Object> getContext() {
        return (( NodeFi ) multiFi.getSelect()).getContext();
    }

    @Override
    public Context evaluate(Context context, Arg... args) throws EvaluationException, RemoteException {
        try {
            if (input != null) {
                ((ServiceContext)input).substitute(context);
            }
            Object out = execute(args);
            if (out instanceof Context) {
                return (Context) out;
            } else {
                return ((ServiceNode) out).getOutput();
            }
        } catch (ServiceException e) {
            throw new EvaluationException(e);
        }
    }

    @Override
    public <T extends Contextion> T exert(Arg... args) throws ServiceException, RemoteException {
        return (T) execute(args);
    }

    @Override
    public String getDomainName() {
        return name;
    }

    @Override
    public Contextion exert(Transaction txn, Arg... args) throws ContextException, RemoteException {
        return evaluate(input, args);
    }

    @Override
    public Context getDomainData() throws ContextException, RemoteException {
        return input;
    }

    @Override
    public Context getContext(Context contextTemplate) throws RemoteException, ContextException {
        return null;
    }

    @Override
    public Context appendContext(Context context, String path) throws ContextException, RemoteException {
        return input.appendContext(context, path);
    }

    @Override
    public Object getId() {
        return disciplineId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void clear() throws MogramException {
        ((ServiceMogram)outDispatcher).clear();
    }

    public Contextion getParent() {
        return parent;
    }

    public void setParent(Contextion parent) {
        this.parent = parent;
    }

    public Node addDepender(Evaluation depender) {
        if (this.dependers == null)
            this.dependers = new ArrayList<Evaluation>();
        dependers.add(depender);
        return this;
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

    @Override
    public List<Contextion> getContextions(List<Contextion> contextionList) {
        return null;
    }

    public void setOutPathProjection(Projection outPathProjection) {
        this.outPathProjection = outPathProjection;
    }

    @Override
    public void addDependers(Evaluation... dependers) {
        if (this.dependers == null)
            this.dependers = new ArrayList<Evaluation>();
        for (Evaluation depender : dependers)
            this.dependers.add(depender);
    }

    public Functionality.Type getDependencyType() {
        return Functionality.Type.REGION;
    }

    @Override
    public List<Evaluation> getDependers() {
        return dependers;
    }

    @Override
    public boolean isExec() {
        return isExec;
    }

    public void setExec(boolean exec) {
        isExec = exec;
    }

    @Override
    public List<Signature> getAllSignatures() throws RemoteException {
        return null;
    }
}
