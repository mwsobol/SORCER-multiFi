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

package sorcer.core.context.model.ent;

import sorcer.core.signature.LocalSignature;
import sorcer.service.*;
import sorcer.service.modeling.Finalization;
import sorcer.service.modeling.Functionality;

/**
 * Created by Mike Sobolewski on 12/19/2020.
 */
public class FinalizerEntry extends Entry<Finalization> implements Finalization {

    private static final long serialVersionUID = 1L;

    private Signature signature;

    public FinalizerEntry(String name, Finalization finalizer)  {
        this.key = name;
        this.impl = finalizer;
        this.type = Functionality.Type.FNL;
    }

    public FinalizerEntry(String name, Signature signature) {
        this.key = name;
        this.signature = signature;
        this.type = Functionality.Type.FNL;
    }

    public FinalizerEntry(String name, Finalization finalizer, Context context) {
        this.key = name;
        scope = context;
        this.impl = finalizer;
        this.type = Functionality.Type.FNL;
    }

    public Finalization getFinalizer() {
        return (Finalization) impl;
    }

    public Signature getSignature() {
        return signature;
    }

    @Override
    public void finalize(Context context, Arg... args) throws EvaluationException {
        try {
            if (impl != null && impl instanceof Finalization) {
                ((Finalization) impl).finalize(context, args);
            } else if (signature != null) {
                impl = ((LocalSignature)signature).initInstance();
                ((Finalization)impl).finalize(context, args);
            } else if (impl == null) {
                throw new InvocationException("No Finalizer available!");
            }
        } catch (ContextException | SignatureException e) {
            throw new EvaluationException(e);
        }
    }
}
