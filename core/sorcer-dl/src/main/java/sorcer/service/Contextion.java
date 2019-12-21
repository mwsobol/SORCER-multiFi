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
     * @throws EvaluationException
     * @throws RemoteException
     */
    public Context evaluate(Context context, Arg... args) throws EvaluationException, RemoteException;

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
	public <T extends Contextion> T exert(Transaction txn, Arg... args) throws ContextException, RemoteException;

	/**
	 * Returns the data context of this contextion.
	 *
	 * @return the data context
	 * @throws ContextException
	*/
	public Context getContext() throws ContextException;

	/**
	 * Sets the data context of this contextion.
	 *
	 * @throws ContextException
	 */
	public void setContext(Context input) throws ContextException;

	/**
	 * Appends an argument context to the data context of this contextion.
	 *
	 * @throws ContextException
	 */
	public Context appendContext(Context context)
		throws ContextException, RemoteException;

	/**
	 * Returns a subcontext specified by paths of contextTemplate.
	 * @throws ContextException
	 * @throws RemoteException
	 */
	public Context getContext(Context contextTemplate)
		throws RemoteException, ContextException;

	/**
	 * Appends an argument context to this context for a given path.
	 * @param context a context to be appended
	 * @param path an offset path of the argument context
	 * @return an appended context
	 * @throws ContextException
	 * @throws RemoteException
	 */
	public Context appendContext(Context context, String path)
		throws ContextException, RemoteException;

	/**
	 * Returns a subcontext at a given path.
	 * @param path a path in this context
	 * @return a subcontext of this context at <code>path</code>
	 * @throws ContextException
	 * @throws RemoteException
	 */
	public Context getContext(String path) throws ContextException,
		RemoteException;

	/**
	 * Returns a Context.Return that specifies a returned context
	 * of this contextion evaluation.
	 *
	 * @return Context.Return to the return execute
	 */
	public Context.Return getContextReturn();

	public MogramStrategy getMogramStrategy();

	public List<Contextion> getContextions(List<Contextion> contextionList);

}
