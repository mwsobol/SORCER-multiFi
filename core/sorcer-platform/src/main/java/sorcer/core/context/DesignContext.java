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

package sorcer.core.context;

import sorcer.core.context.model.ent.Developer;
import sorcer.service.*;
import sorcer.service.modeling.Exploration;

/**
 * @author Mike Sobolewski, 05/20/2021
 */
public class DesignContext extends ServiceContext {

    private Fidelity<Development> developerFi;

    private Design design;

    private Signature designSignature;

    private Context intent;

    public DesignContext(String name) {
        super(name);
    }

    @Override
    public Fidelity<Development> getDeveloperFi() {
        return developerFi;
    }

    public void setDeveloperFi(Fidelity<Development> developerFi) {
        this.developerFi = developerFi;
    }
    public Context getIntent() {
        return intent;
    }
    public void setIntent(Context intent) {
        this.intent = intent;
    }
    public Design getDesign() {
        return design;
    }

    public void setDesign(Design design) {
        this.design = design;
    }

    public Signature getDesignSignature() {
        return designSignature;
    }

    public void setDesignSignature(Signature designSignature) {
        this.designSignature = designSignature;
    }
}
