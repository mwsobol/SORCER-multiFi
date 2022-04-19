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

package sorcer.core.context.model.ent.cntrl;

import sorcer.core.context.model.ent.Entry;
import sorcer.core.service.Governance;
import sorcer.core.signature.LocalSignature;
import sorcer.service.*;
import sorcer.service.modeling.ExecutiveException;
import sorcer.service.modeling.Functionality;

import java.rmi.RemoteException;

/**
 * Created by Mike Sobolewski on 01/05/20.
 */
public class Supervisor extends Entry<Supervision> implements Controlling, Supervision {

    private static final long serialVersionUID = 1L;

    private Contextion contextion;

    private Signature signature;

    public Supervisor(String name, Supervision supervisor)  {
        this.key = name;
        this.impl = supervisor;
        this.type = Functionality.Type.SUPERVISOR;
    }

    public Supervisor(String name, Signature signature) {
        this.key = name;
        this.signature = signature;
        this.type = Functionality.Type.SUPERVISOR;
    }

    public Supervisor(String name, Supervision supervisor, Context context) {
        this.key = name;
        scope = context;
        this.impl = supervisor;
        this.type = Functionality.Type.SUPERVISOR;
    }

    public Contextion getContextion() {
        return contextion;
    }

    public void setContextion(Contextion contextion) {
        this.contextion = contextion;
    }

    public Supervision getSupervisor() {
        return (Supervision) impl;
    }

    public void setSupervisor(Supervision supervisor) {
        impl = supervisor;
    }

    public Signature getSignature() {
        return signature;
    }

    @Override
    public Context supervise(Context context, Arg... args) throws ServiceException, RemoteException {
        Context out = ((Governance) contextion).getOutput();
        try {
            if (impl != null && impl instanceof Supervision) {
                if (contextion == null) {
                    out = ((Supervision) impl).supervise(context, args);
                } else {
                    out = ((Supervision) impl).supervise(context, args);
                }
            } else if (signature != null) {
                impl = ((LocalSignature) signature).initInstance();
                out = ((Supervision) impl).supervise(context);
            } else if (impl == null) {
                throw new InvocationException("No supervisor available!");
            }
        } catch (ContextException | SignatureException | ExecutiveException |  RemoteException e) {
            throw new ServiceException(e);
        }
        return out;
    }
}
