/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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

import net.jini.admin.Administrable;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import org.slf4j.Logger;
import sorcer.core.analytics.AnalyticsProducer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.Policy;
import java.util.List;
import java.util.Properties;

/**
 * This is an interface that defines how a provider interacts with other code 
 * the through the methods that are exposed. It extends {@link Service},
 * {@link sorcer.service.Monitorable}, and {@link Remote}.
 * @see Service
 * @see Monitorable(806) 744-7223Contex
 * @see Remote
 */
public interface Exerter extends Provider, Exertion, Monitorable, AnalyticsProducer, Administrable, Remote {

	ServiceID getProviderID() throws RemoteException;

	String getProviderName() throws RemoteException;
	
	Entry[] getAttributes() throws RemoteException;

	List<Object> getProperties() throws RemoteException;

	String getProperty(String property) throws RemoteException;

	Properties getJavaSystemProperties() throws RemoteException;

	boolean mutualExclusion() throws RemoteException;
	
	String[] getGroups() throws RemoteException;

	String getInfo() throws RemoteException;

	String getDescription() throws RemoteException;

	boolean isBusy() throws RemoteException;

	/**
	 * Destroy the service, if possible, including its persistent storage.
	 * 
	 * @see Exerter#destroy()
	 */
	void destroy() throws RemoteException;

	/**
	 * Destroy all services in this node (virtual machine) by calling each
	 * destroy().
	 * 
	 * @see Exerter#destroy()
	 */
	void destroyNode() throws RemoteException;
	
	void fireEvent() throws RemoteException;

	Object getProxy() throws RemoteException;

	void updatePolicy(Policy policy) throws RemoteException;

	Logger getContextLogger() throws RemoteException;

	Logger getProviderLogger() throws RemoteException;

	Logger getRemoteLogger() throws RemoteException;
	
}
