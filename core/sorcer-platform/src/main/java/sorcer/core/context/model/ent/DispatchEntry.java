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

import sorcer.core.service.Collaboration;
import sorcer.core.signature.LocalSignature;
import sorcer.service.*;
import sorcer.service.modeling.Functionality;

import java.rmi.RemoteException;

/**
 * Created by Mike Sobolewski on 01/05/20.
 */
public class DispatchEntry extends Entry<Dispatch> implements Dispatch {

    private static final long serialVersionUID = 1L;

    private Contextion contextion;

    private Signature signature;

    public DispatchEntry(String name, Dispatch dispatcher)  {
        this.key = name;
        this.impl = dispatcher;
        this.type = Functionality.Type.DISPATCH;
    }

    public DispatchEntry(String name, Signature signature) {
        this.key = name;
        this.signature = signature;
        this.type = Functionality.Type.DISPATCH;
    }

    public DispatchEntry(String name, Dispatch dispatcher, Context context) {
        this.key = name;
        scope = context;
        this.impl = dispatcher;
        this.type = Functionality.Type.DISPATCH;
    }

    public Dispatch getDispatcher() {
        return (Dispatch) impl;
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
    public Context dispatch(Context context, Arg... args) throws DispatchException, RemoteException {
        Context out = ((Collaboration) contextion).getOutput();
        try {
            if (impl != null && impl instanceof Analysis) {
                if (contextion == null) {
                    out = ((Dispatch) impl).dispatch(context, args);
                } else {
                    out = ((Dispatch) impl).dispatch(context, args);
                }
            } else if (signature != null) {
                impl = ((LocalSignature) signature).initInstance();
                out = ((Dispatch) impl).dispatch(context);
            } else if (impl == null) {
                throw new InvocationException("No dispatcher available!");
            }
        } catch (ContextException | SignatureException | DispatchException | RemoteException e) {
            throw new DispatchException(e);
        }
        return out;
    }
}
