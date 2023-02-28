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

package sorcer.core.service;

import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import sorcer.service.*;
import sorcer.service.modeling.Exploration;
import sorcer.service.modeling.Finalization;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * @author Mike Sobolewski
 */
 public class DesignProject extends MultiFiSlot implements Project {

    protected Uuid id = UuidFactory.generate();

    private Design design;

    public DesignProject(Design design) {
        this.design = design;
    }

    @Override
    public Object getId() {
        return id;
    }

    public Design getDesign() {
        return design;
    }

    public void setDesign(Transdesign design) {
        this.design = design;
    }


    @Override
    public Map<String, Design> getChildren() {
        return ((Transdesign)design).getChildren();
    }

    @Override
    public Design getChild(String name) {
        return ((Transdesign)design).getChild(name);
    }

    @Override
    public Fidelity<Finalization> getFinalizerFi() {
        return null;
    }

    @Override
    public Fidelity<Analysis> getAnalyzerFi() {
        return null;
    }

    @Override
    public Fidelity<Exploration> getExplorerFi() {
        return null;
    }

    @Override
    public Fidelity<Exploration> getManagerFi() {
        return null;
    }

    @Override
    public Context analyze(Context intent, Arg... args) throws ContextException, RemoteException {
        return null;
    }

    @Override
    public Context explore(Context intent, Arg... args) throws ContextException, RemoteException {
        return null;
    }

    @Override
    public Context manage(Context intent, Arg... args) throws ContextException, RemoteException {
        return null;
    }

    @Override
    public void setName(String name) {
        key = name;
    }
}
