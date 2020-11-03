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

import sorcer.core.signature.LocalSignature;
import sorcer.core.service.Collaboration;
import sorcer.service.*;
import sorcer.service.modeling.Exploration;
import sorcer.service.modeling.ExploreException;
import sorcer.service.modeling.Functionality;

import java.rmi.RemoteException;
import java.util.Map;

import static sorcer.mo.operator.mdaFi;
import static sorcer.so.operator.exec;
import static sorcer.so.operator.response;

/**
 * Created by Mike Sobolewski on 01/05/20.
 */
public class EntryExplorer extends Entry<Exploration> implements Exploration {

    private static final long serialVersionUID = 1L;

    private Contextion contextion;

    private Dispatch disptacher;

    private Signature signature;

    public EntryExplorer(String name, Exploration explorer)  {
        this.key = name;
        this.impl = explorer;
        this.type = Functionality.Type.EXPLORER;
    }

    public EntryExplorer(String name, Signature signature) {
        this.key = name;
        this.signature = signature;
        this.type = Functionality.Type.EXPLORER;
    }

    public EntryExplorer(String name, Exploration mda, Context context) {
        this.key = name;
        scope = context;
        this.impl = mda;
        this.type = Functionality.Type.EXPLORER;
    }

    public Contextion getContextion() {
        return contextion;
    }

    public void setContextion(Contextion contextion) {
        this.contextion = contextion;
    }

    public Dispatch getDisptacher() {
        return disptacher;
    }

    public void setDisptacher(Dispatch disptacher) {
        this.disptacher = disptacher;
    }

    public Exploration getExploration() {
        return out;
    }

    public Signature getSignature() {
        return signature;
    }

    @Override
    public Context explore(Context context) throws ContextException, ExploreException, RemoteException {
        Map<String, Context> domainOutputs = ((Collaboration) contextion).getOutputs();
        // use output for explorer after collaboration
        Context output = null;
        try {
            if (disptacher != null) {
                disptacher.dispatch(context);
            }
            if (contextion instanceof Collaboration) {
                ((Collaboration) contextion).analyze(context);
            } else {
                throw new ContextException("analysis failed for: " + contextion);
            }
            output = ((Collaboration) contextion).getOutput();
            output.putValue(Functionality.Type.COLLABORATION.toString(), contextion.getName());
            if (impl != null && impl instanceof Exploration) {
                output = ((Exploration) impl).explore(output);
            } else if (signature != null) {
                impl = ((LocalSignature)signature).initInstance();
                output = ((Exploration)impl).explore(output);
            } else if (impl == null) {
                throw new InvocationException("No explorer available!");
            }
        } catch (SignatureException | DispatchException | ServiceException e) {
            throw new ContextException(e);
        }
        return output;
    }

}
