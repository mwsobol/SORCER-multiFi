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

import sorcer.service.modeling.cxtn;
import sorcer.service.modeling.mog;

import java.rmi.RemoteException;

/**
 * Common functionality of Discipline types.
 *
 * Created by Mike Sobolewski on 11/26/2019.
 */
public interface Domain extends Mogram, Discipline, Dependency, mog, cxtn {

	public String getDomainName();

    /**
     * Returns an execute of the domain at the returnPath as is
     * (no evaluation or invocation on this object).
     *
     * @param path
     *            the variable name
     * @return this model execute at the returnPath
     * @throws ModelException
     */
    public Object asis(String path) throws ContextException;

	/**
	 * Returns an input connector as a map of input paths of this domain mapped to output paths of the sender.
	 * An input connector specifies a map of an input context of this model.
	 *
	 * @param args optional configuration arguments
	 * @return
	 * @throws ContextException
	 * @throws RemoteException
	 */
	public Context getInConnector(Arg... args) throws ContextException, RemoteException;

	/**
	 * Returns a output connector as a map of output paths of tis domain mapped to input paths of the receiver.
	 * An output connector specifies a map of an output context of this domain.
	 *
	 * @param args optional configuration arguments
	 * @return
	 * @throws ContextException
	 * @throws RemoteException
	 */
	public Context getOutConnector(Arg... args) throws ContextException, RemoteException;

	/**
	 * Returns a execute of the object at the returnPath od this domain
	 * (evaluation or invocation on this object if needed).
	 *
	 * @param path
	 *            the variable name
	 * @return this domain execute at the returnPath
	 * @throws ContextException, RemoteException
	 */
	public Object getValue(String path, Arg... args) throws ContextException, RemoteException;

	public void setParent(Contextion parent);

	public Contextion getParent();

	public void execDependencies(String path, Arg... args) throws ContextException;

	public boolean isChanged();
}
