/*
 * Copyright 2013 the original author or authors.
 * Copyright 2013 SorcerSoft.org.
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

package sorcer.core.context.model.ent;

import sorcer.core.service.Governance;
import sorcer.core.signature.LocalSignature;
import sorcer.service.*;
import sorcer.service.modeling.ExecutiveException;
import sorcer.service.modeling.Functionality;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * Created by Mike Sobolewski on 02/03/21.
 */
public class Rule extends Entry<Hypervision> implements Controller, Hypervision {

    private static final long serialVersionUID = 1L;

    private Map<String, Supervision> supervisors;

    private Contextion contextion;

    private Signature signature;

    public Rule(String name, Hypervision executive)  {
        this.key = name;
        this.impl = executive;
        this.type = Functionality.Type.EXECUTION;
    }

    public Rule(String name, Signature signature) {
        this.key = name;
        this.signature = signature;
        this.type = Functionality.Type.EXECUTION;
    }

    public Rule(String name, Hypervision executive, Context context) {
        this.key = name;
        scope = context;
        this.impl = executive;
        this.type = Functionality.Type.EXECUTION;
    }

    public Contextion getContextion() {
        return contextion;
    }

    public void setContextion(Contextion contextion) {
        this.contextion = contextion;
    }


    public Map<String, Supervision> getSupervisors() {
        return supervisors;
    }

    public void setSupervisors(Map<String, Supervision> supervisors) {
        this.supervisors = supervisors;
    }

    public Hypervision getExecutive() {
        return (Hypervision) impl;
    }

    public void setExecutive(Hypervision executive) {
        impl = executive;
    }

    public Signature getSignature() {
        return signature;
    }

    @Override
    public Context hypervise(Context context, Arg... args) throws ExecutiveException, ServiceException {
        Context out = ((Governance) contextion).getOutput();
        try {
            if (impl != null && impl instanceof Hypervision) {
                if (supervisors == null) {
                    out = ((Hypervision) impl).hypervise(context, args);
                }
            } else if (signature != null) {
                impl = ((LocalSignature) signature).initInstance();
                out = ((Hypervision) impl).hypervise(context);
            } else if (impl == null) {
                throw new InvocationException("No rule available!");
            }
        } catch (ContextException | SignatureException | RemoteException e) {
            throw new ExecutiveException(e);
        }
        return out;
    }
}
