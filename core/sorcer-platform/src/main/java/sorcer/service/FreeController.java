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

import sorcer.core.signature.LocalSignature;

/**
 * A free controller is an instance of Controller that has a name only to be bound at runtime.
 *
 * @see Controlling
 *
 * @author Mike Sobolewski
 */
public class FreeController implements Controlling,  FreeService {

    private String name;
    private Controlling controller;
    private boolean isValid = false;
    private MultiFiSlot taraget;
    private Signature builder;

    public FreeController(String name) {
        isValid = false;
        this.name = name;
    }

    @Override
    public void bind(Object object) throws ConfigurationException {
        if (object instanceof Controlling) {
            controller = ( Controlling ) object;
        } else if (object instanceof LocalSignature) {
            try {
                controller = ( Controlling ) ((LocalSignature) object).build();
                builder = (Signature) object;
                name = builder.getName();
            } catch (SignatureException e) {
                throw new ConfigurationException(e);
            }
        }
        if (taraget != null && controller instanceof Identifiable) {
            taraget.putController(((Identifiable)controller).getName(), controller);
        }
    }

    public Controlling getController() {
        if (isValid) {
            return controller;
        } else {
            controller = taraget.getController(name);
            isValid = true;
        }
        return controller;
    }

    public Signature getBuilder() {
        return builder;
    }

    public void setBuilder(Signature builder) {
        this.builder = builder;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean iValid) {
        this.isValid = iValid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
