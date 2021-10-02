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

package sorcer.service;

import sorcer.service.modeling.ExecutiveException;
import sorcer.service.modeling.Exploration;
import sorcer.service.modeling.Finalization;
import sorcer.service.modeling.Initialization;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An top-level common interface for all design services in SORCER.
 * Design services are frontend services created by multidisciplinary
 * designers. Standalone design services that are multidisciplinary
 * services comprise design intents that refer to target disciplines
 * (mostly transdisciplines), and development controllers (developers).
 *
 * @author Mike Sobolewski, 05/23/2021
 */
public interface Design extends Request, Contextion, Remote {

    public Context getContext() throws ContextException;

    public Contextion getDiscipline() throws RemoteException;;

    public Fi getDevelopmentFi() throws RemoteException;

    public Context design(Discipline discipline, Context context) throws DesignException, RemoteException;

    public Fidelity<Initialization> getInitializerFi();

    public Fidelity<Finalization> getFinalizerFi();
}
