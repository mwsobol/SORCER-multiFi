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

import sorcer.core.context.Connector;
import sorcer.core.signature.LocalSignature;
import sorcer.service.*;
import sorcer.service.modeling.Functionality;

import java.rmi.RemoteException;

/**
 * Created by Mike Sobolewski on 09/30/20.
 */
public class Morpheus extends Entry<Morpher> implements Controller, Morpher {

    private static final long serialVersionUID = 1L;

    private Signature signature;

    private Morpher.Dir direction = Morpher.Dir.OUT;

    public Morpheus(String name, Morpher morpher)  {
        this.key = name;
        this.impl = morpher;
        this.type = Functionality.Type.MRPH;
    }

    public Morpheus(String name, Morpher morpher,  Morpher.Dir direction)  {
        this(name, morpher);
        this.direction = direction;
    }

    public Morpheus(String name, Signature signature) {
        this.key = name;
        this.signature = signature;
        this.type = Functionality.Type.MRPH;
    }

    public Morpheus(String name, Morpher mda, Context context) {
        this.key = name;
        scope = context;
        this.impl = mda;
        this.type = Functionality.Type.MRPH;
    }

    public Morpher getMorpher() {
        return (Morpher) impl;
    }

    @Override
    public void morph(FidelityManagement manager, Fi<Service> mFi, Object value) throws ServiceException {
        try {
            if (impl != null && impl instanceof Morpher) {
                ((Morpher) impl).morph(manager, mFi, value);
            } else if (signature != null) {
                impl = ((LocalSignature)signature).initInstance();
                ((Morpher)impl).morph(manager, mFi, value);
            } else if (impl == null) {
                throw new InvocationException("No morpher available!: " + key);
            }
        } catch (SignatureException | ConfigurationException | RemoteException e) {
            throw new ServiceException(e);
        }
    }

    public Morpher.Dir getDirection() {
        return direction;
    }
}
