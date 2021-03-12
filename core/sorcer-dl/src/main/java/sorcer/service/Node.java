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

import sorcer.service.modeling.cxtn;

/**
 *  The interface for a service discipline design pattern as governance-multiFi-dispatcher.
 *  Service governance is the indeterminate multifidelity process of decision-making
 *  and the process by which decisions are actualized in the form of a service federation.
 */
public interface Node extends Discipline, Dependency, cxtn {

    /**
     * Returns a service governance specifying actualization of this discipline
     *
     * @throws ServiceException
     */
    Service getContextion() throws ServiceException;

    /**
     * Returns an executed contextion of this discipline
     *
     * @throws ServiceException
     */
    Service getOutContextion();

    /**
     * Returns a dispatcher to dispatch this discipline
     *
     * @return a dispatcher of this discipline
     * @throws RoutineException
     */
    Dispatch getDispatcher() throws RoutineException;

    /**
     * Returns an executed dispatcherof this discipline
     *
     * @return an executed dispatcher of this discipline
     * @throws RoutineException
     */
    Dispatch getOutDispatcher();

    /**
     * Returns a discipline input context.
     *
     * @return a current input context
     * @throws ContextException
     */
    Context getInput() throws ContextException;

    /**
     * Returns an output context of this discipline.
     *
     * @return a current output context
     * @throws ServiceException
     */
    Context getOutput(Arg... args) throws ContextException;

    /**
     * Returns a builder of this discipline to be used for replication
     * of this discipline when needed.
     * */
    Signature getBuilder();

    Context getInConnector();

    void setInConnector(Context inConnector);

    Context getOutConnector();

    void setOutConnector(Context outConnector);

    Contextion getParent();

}
