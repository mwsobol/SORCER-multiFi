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

package sorcer.service;


import net.jini.core.transaction.Transaction;
import sorcer.service.modeling.cxtn;

import java.rmi.RemoteException;
import java.util.List;

/**
 * An instance of the Contextion type represents a functional mapping with a domain
 * and a codomain of service contexts of {@link sorcer.service.Context} type.
 *
 * @author Mike Sobolewski
 */
public interface Contextion extends Request, Scopable {

    /**
     * Returns the context of evaluation of this contextion.
	 * The current context can be the existing one with no need
	 * to evaluate it if is still valid.
     *
     * @return the current execute of this evaluation
     * @throws MogramException
     * @throws RemoteException
     */
    Context evaluate(Context context, Arg... args) throws ServiceException, RemoteException;

	/**
	 * Generic execution of mogram by cooperated services.
	 *
	 * @return a resulting Contextion
	 * @throws ServiceException
	 *             if a mogram error occurs
	 * @throws RemoteException
	 *             if remote call causes an error
	 */
	public <T extends Contextion> T exert(Arg... args) throws ServiceException, RemoteException;

	/**
	 * Generic federated execution called exertion by federated services.
	 *
	 * @param txn
	 *            The transaction (if any) under which to exert.
	 * @return a resulting exertion
	 * @throws MogramException
	 *             if a mogram error occurs
	 * @throws RemoteException
	 *             if remote call causes an error
	 */
	<T extends Contextion> T exert(Transaction txn, Arg... args) throws ServiceException, RemoteException;

	public String getDomainName() throws RemoteException;

	/**
	 * Returns the data context of this contextion.
	 *
	 * @return the data context
	 * @throws ContextException
	*/
	Context getContext() throws ContextException, RemoteException;

	Context getOutput(Arg... args) throws ContextException, RemoteException;

	/**
	 * Sets the data context of this contextion.
	 *
	 * @throws ContextException
	 */
	void setContext(Context input) throws ContextException, RemoteException;

	/**
	 * Appends an argument context to the data context of this contextion.
	 *
	 * @throws ContextException
	 */
	Context appendContext(Context context)
		throws ContextException, RemoteException;

	Context getDomainData()
		throws ContextException, RemoteException;

	/**
	 * Returns a subcontext specified by paths of contextTemplate.
	 * @throws ContextException
	 * @throws RemoteException
	 */
	Context getContext(Context contextTemplate)
		throws ContextException, RemoteException;

	/**
	 * Appends an argument context to this context for a given path.
	 * @param context a context to be appended
	 * @param path an offset path of the argument context
	 * @return an appended context
	 * @throws ContextException
	 * @throws RemoteException
	 */
	Context appendContext(Context context, String path)
		throws ContextException, RemoteException;

	/**
	 * Returns a subcontext at a given path.
	 * @param path a path in this context
	 * @return a subcontext of this context at <code>path</code>
	 * @throws ContextException
	 * @throws RemoteException
	 */
	Context getContext(String path) throws ContextException, RemoteException;

	/**
	 * Returns true if this contextion is executable in Collaboration
	 */
	public boolean isExec() throws RemoteException;

	/**
	 * Returns a Context.Return that specifies a returned context
	 * of this contextion evaluation.
	 *
	 * @return Context.Return to the return execute
	 */
	Context.Return getContextReturn() throws RemoteException;

	ServiceStrategy getDomainStrategy() throws RemoteException;

	Projection getInPathProjection() throws RemoteException;

	Projection getOutPathProjection() throws RemoteException;

	List<Contextion> getContextions(List<Contextion> contextionList) throws RemoteException;

	FidelityManagement getFidelityManager() throws RemoteException;

	void selectFidelity(Fi fi) throws ConfigurationException, RemoteException;

}
