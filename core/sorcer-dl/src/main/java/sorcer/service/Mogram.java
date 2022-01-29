/*
 * Copyright 2015 the original author or authors.
 * Copyright 2015 SorcerSoft.org.
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

import net.jini.core.transaction.Transaction;
import net.jini.id.Uuid;
import sorcer.core.context.ThrowableTrace;

import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Date;
import java.util.List;

/**
 * A top-level interface for mograms - models of the {@link sorcer.service.modeling.Model} type
 * and routines of the {@link sorcer.service.Routine} type, or both.
 *
 * @author Mike Sobolewski
 */
public interface Mogram extends Contextion, Exertion, Discipline, Arg {

    /**
     * Returns the list of traces of thrown exceptions from this mogram.
     * @return ThrowableTrace list
     */
    public List<ThrowableTrace> getExceptions() throws RemoteException;;

    /**
     * Returns the list of traces left by collborating services.
     * @return ThrowableTrace list
     */
    public List<String> getTrace() throws RemoteException;

    /**
     * Returns the list of all traces of thrown exceptions with exceptions of
     * component mograms.
     *
     * @return ThrowableTrace list
     */
    public List<ThrowableTrace> getAllExceptions() throws RemoteException;

    /**
     * Returns <code>true</code> if this exertion should be monitored for its
     * execution, otherwise <code>false</code>.
     *
     * @return <code>true</code> if this exertion requires its execution to be
     *         monitored.
     */
    public boolean isMonitorable() throws RemoteException;

    /**
     * Return date when exertion was created
     * @return
     */
    public Date getCreationDate() throws RemoteException;

    /**
     * Reconfigure this mogram with given fudelities.
     *
     * @param fidelities
     */
    public void reconfigure(Fidelity... fidelities) throws ContextException, RemoteException, ConfigurationException;

    /**
     * Reconfigure this mmogramodel with given names of metafidelities.
     *
     * @param metaFiNames
     */
    public void project(String... metaFiNames) throws ContextException, RemoteException, ConfigurationException;

    /**
     * Update this mogram with given setup context entries.
     *
     * @param contextEntries
     */
    public void update(Setup... contextEntries) throws ContextException, RemoteException;

    /**
     * Returns true if this exertion is semantically valid.
     */
    public boolean isValid() throws ContextException, RemoteException;

    /**
	 * Returns true if this exertion is a branching or looping exertion.
	 */
	public boolean isConditional() throws RemoteException;

	/**
	 * Returns true if this exertion is composed of other exertions.
	 */
	public boolean isCompound() throws RemoteException;

}
