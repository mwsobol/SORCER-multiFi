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
package sorcer.service;

import sorcer.core.Dispatcher;
import sorcer.service.modeling.SlotMultiFi;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * @author Mike Sobolewski 03/11/2021
 */
public class NodeFi implements Identifiable, Serializable, Arg {

    private String name;

    protected String path;

    private SlotMultiFi cxtFi;

    private SlotMultiFi cxtnFi;

    private SlotMultiFi dspFi;

    public NodeFi(String name) {
        this.name = name;
    }

    @Override
    public Object getId() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Context getContext() {
        if (cxtFi != null) {
            return (Context) ((Slot)cxtFi.getSelect()).getOut();
        } else {
            return null;
        }
    }

    public void setContext(Context context) {
        this.cxtFi.setSelect(context);
    }

    public Service getContextion() {
        if (cxtnFi != null) {
            return (Service) ((Slot)cxtnFi.getSelect()).getOut();
        } else {
            return null;
        }
    }

    public void setContextion(Contextion contextion) {
        this.cxtnFi.setSelect(contextion);
    }

    public Routine getDispatcher() {
        if (dspFi != null) {
            return (Routine) ((Slot)dspFi.getSelect()).getOut();
        } else {
            return null;
        }
    }

    public void setDispatcher(Dispatcher dispatcher) {
        dspFi.setSelect((Identifiable) dispatcher);
    }

    @Override
    public Object execute(Arg... args) throws ServiceException, RemoteException {
        return null;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) throws ContextException {

    }

    public Fidelity getContextFi() {
        return cxtFi;
    }

    public void setContextFi(SlotMultiFi contextMultiFi) {
        this.cxtFi = contextMultiFi;
    }

    public Fidelity getContextionFi() {
        return cxtnFi;
    }

    public void setContextionFi(SlotMultiFi govFi) {
        this.cxtnFi = govFi;
    }

    public Fidelity getDispatcherFi() {
        return dspFi;
    }

    public void setDispatcherFi(SlotMultiFi dsptFi) {
        this.dspFi = dsptFi;
    }

    private void assignFi(SlotMultiFi fi) {
        if (fi.getFiType().equals(Fi.Type.DISPATCHER)) {
            this.dspFi = fi;
        } else if (fi.getFiType().equals(Fi.Type.CONTEXTION)) {
            this.cxtnFi = fi;
        } else if (fi.getFiType().equals(Fi.Type.CONTEXT)) {
            this.cxtFi = fi;
        }
    }
}
