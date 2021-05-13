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

package sorcer.core.context.model.ent;

import sorcer.core.service.Governance;
import sorcer.core.signature.LocalSignature;
import sorcer.service.*;
import sorcer.service.modeling.ExecutiveException;
import sorcer.service.modeling.Functionality;

import java.rmi.RemoteException;

/**
 * Created by Mike Sobolewski on 05/12/21.
 */
public class Developer extends Entry<Development> implements Controller, Development {

    private static final long serialVersionUID = 1L;

    private Contextion contextion;

    private Signature signature;

    public Developer(String name, Development develper)  {
        this.key = name;
        this.impl = develper;
        this.type = Functionality.Type.DEVELOPER;
    }

    public Developer(String name, Signature signature) {
        this.key = name;
        this.signature = signature;
        this.type = Functionality.Type.DEVELOPER;
    }

    public Developer(String name, Development develper, Context context) {
        this.key = name;
        scope = context;
        this.impl = develper;
        this.type = Functionality.Type.DEVELOPER;
    }

    public Contextion getContextion() {
        return contextion;
    }

    public void setContextion(Contextion contextion) {
        this.contextion = contextion;
    }

    public Development getDeveloper() {
        return (Development) impl;
    }

    public void setDeveloper(Development developer) {
        impl = developer;
    }

    public Signature getSignature() {
        return signature;
    }

    @Override
    public Context develop(Context context, Arg... args) throws ServiceException, ExecutiveException, RemoteException {
        Context out = ((Governance) contextion).getOutput();
        try {
            if (impl != null && impl instanceof Development) {
                if (contextion == null) {
                    out = ((Development) impl).develop(context, args);
                } else {
                    out = ((Development) impl).develop(context, args);
                }
            } else if (signature != null) {
                impl = ((LocalSignature) signature).initInstance();
                out = ((Development) impl).develop(context);
            } else if (impl == null) {
                throw new InvocationException("No developer available!");
            }
        } catch (ContextException | SignatureException | ExecutiveException |  RemoteException e) {
            throw new ServiceException(e);
        }
        return out;
    }
}
