/*
 * Copyright 2016 the original author or authors.
 * Copyright 2016 SorcerSoft.org.
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

import java.rmi.RemoteException;

/**
 * Common functionality of Context and Model types.
 *
 * Created by Mike Sobolewski on 7/26/16.
 */
public interface ContextDomain extends Domain {

    /**
	 * Returns the context of all responses of this domain a given configuration.
	 *
	 * @param args optional configuration arguments
	 * @return
	 * @throws ContextException
	 * @throws RemoteException
	 */
	Object getResponse(Arg... args) throws ContextException, RemoteException;

	/**
	 * Returns the input context of this domain.
	 *
	 * @return the input context
	 * @throws ContextException
	 * @throws RemoteException
	 */
	Context getInputs() throws ContextException, RemoteException;

	/**
	 * Returns the input context of this domain with all inputs (in and inout directions).
	 *
	 * @return the input context
	 * @throws ContextException
	 * @throws RemoteException
	 */
	Context getAllInputs() throws ContextException, RemoteException;

	/**
	 * Returns the output context of this domain.
	 *
	 * @return the output context
	 * @throws ContextException
	 * @throws RemoteException
	 */
	Context getOutputs() throws ContextException, RemoteException;

	/**
	 * Returns a execute of the object at the returnPath od this domain
	 * (evaluation or invocation on this object if needed).
	 *
	 * @param objects
	 *            the identifiable objects by type accordingly
	 * @return this domain updated
	 * @throws ContextException
	 */
	ContextDomain add(Identifiable... objects) throws ContextException,
			RemoteException;

	Path getPath(String path) throws ContextException, RemoteException;

	Object getValue(Path path, Arg... args) throws ContextException, RemoteException;
}
