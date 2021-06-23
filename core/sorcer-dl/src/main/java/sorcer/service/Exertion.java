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

import java.rmi.RemoteException;
import net.jini.core.transaction.Transaction;

/**
 * A functionality of executing a mogram (exerting collaboration of service providers)
 * with given parameters by service providers.
 *
 * In particular a SORCER service containers ServiceExerter, ServiceTasker, ServiceShell,
 * and system service beans of type SystemServiceBeans are exerters.
 *
 * @author Mike Sobolewski
 */
public interface Exertion {
	/**
	 * A generic federated execution.
	 *
	 * @param exertion an input mogram
	 * @param txn      The transaction (if any) under which to provide service.
	 * @return a resulting mogram
	 * @throws ServiceException    if an exertion invocation failed for any reason
	 * @throws RemoteException
	 */
	<T extends Contextion> T exert(T exertion, Transaction txn, Arg... args) throws ServiceException, RemoteException;

}
