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

package sorcer.core.context.model.ent.cntrl;

import sorcer.core.context.model.ent.Entry;
import sorcer.core.signature.LocalSignature;
import sorcer.service.*;
import sorcer.service.modeling.ExecutiveException;
import sorcer.service.modeling.Functionality;

import java.rmi.RemoteException;

/**
 * Created by Mike Sobolewski on 03/12/20221.
 */
public class Hypervisor extends Entry<Analysis> implements Driver, Hypervision {

    private static final long serialVersionUID = 1L;

    private Contextion contextion;

    private Signature signature;

    public Hypervisor(String name, Analysis mda)  {
        this.key = name;
        this.impl = mda;
        this.type = Functionality.Type.MDA;
    }

    public Hypervisor(String name, Signature signature) {
        this.key = name;
        this.signature = signature;
        this.type = Functionality.Type.MDA;
    }

    public Hypervisor(String name, Analysis mda, Context context) {
        this.key = name;
        scope = context;
        this.impl = mda;
        this.type = Functionality.Type.MDA;
    }

    public Analysis getAnalyzer() {
        return (Analysis) impl;
    }

    public Contextion getContextion() {
        return contextion;
    }

    public void setContextion(Contextion contextion) {
        this.contextion = contextion;
    }

    public Signature getSignature() {
        return signature;
    }

    @Override
    public Context hypervise(Context context, Arg... args) throws ServiceException, ExecutiveException {
        Context out = null;
        try {
            if (impl != null && impl instanceof Analysis) {
                if (contextion == null || context == contextion) {
                    out = ((Hypervision) impl).hypervise(context, args);
                } else {
                    out =  ((Hypervision) impl).hypervise(contextion.evaluate(context));
                }
            } else if (signature != null) {
                impl = ((LocalSignature)signature).initInstance();
                out = ((Hypervision)impl).hypervise(context, args);
            } else if (impl == null) {
                throw new InvocationException("No MDA analysis available!");
            }
        } catch (MogramException | SignatureException | RemoteException e) {
            throw new EvaluationException(e);
        }
        return out;
    }
}
