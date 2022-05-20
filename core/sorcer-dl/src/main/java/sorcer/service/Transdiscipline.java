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
package sorcer.service;

import sorcer.service.modeling.*;

import java.rmi.RemoteException;
import java.util.Map;

public interface Transdiscipline extends Discipline, disc, cxtn {

    public Map<String, Context>  getChildrenContexts();

    public Contextion getChild(String name);

    public Fidelity<Finalization> getFinalizerFi();

    public Fidelity<Analysis> getAnalyzerFi();

    public Fidelity<Exploration> getExplorerFi();

    public Context analyze(Context modelContext, Arg... args)
        throws ContextException, RemoteException;

    public Context explore(Context context, Arg... args)
        throws ContextException, RemoteException;

    /**
     * Returns the context of parent evaluation of this contextion.
     * The current context can be the existing one with no need
     * to evaluate it if is still valid.
     *
     * @return the current execute of this evaluation
     * @throws MogramException
     * @throws RemoteException
     */
    public Context superevaluate(Context context, Arg... args) throws ServiceException, RemoteException;

}