/*
 * Copyright 2014 the original author or authors.
 * Copyright 2014 SorcerSoft.org.
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

import net.jini.core.entry.Entry;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Created by Mike Sobolewski on 6/14/15.
 */
public interface FidelityManagement extends RemoteEventListener, Serializable {

    Map<String, Fidelity> getFidelities() throws RemoteException;

     Map<String, MetaFi> getMetafidelities() throws RemoteException;

    // for metafidelities
    void morph(String... fiNames) throws EvaluationException, RemoteException;

    // for projections
    void project(String... fiNames) throws EvaluationException, RemoteException;

    void reconfigure(String... fiNames) throws EvaluationException, RemoteException, ConfigurationException;

    void reconfigure(Fi... fidelities) throws EvaluationException, RemoteException, ConfigurationException;

    List<Fi> getDefaultFidelities() throws RemoteException;

    Contextion getMogram() throws RemoteException;

    List<Fi> getFiTrace() throws RemoteException;

    void addTrace(ServiceFidelity fi);

    void publish(Entry entry) throws RemoteException, ContextException;

    EventRegistration register(long eventID, String path,
                                      RemoteEventListener toInform, long leaseLenght)
            throws UnknownEventException, RemoteException;

    void deregister(long eventID) throws UnknownEventException,
            RemoteException;

}
