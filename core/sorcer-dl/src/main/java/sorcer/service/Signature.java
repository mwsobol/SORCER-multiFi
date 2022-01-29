/*
 * Copyright 2009 the original author or authors.
 * Copyright 2009 SorcerSoft.org.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.provider.ProviderName;
import sorcer.service.modeling.EvaluationComponent;
import sorcer.service.modeling.SupportComponent;
import sorcer.service.modeling.Functionality;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A service <code>Signature</code> is an indirect behavioral feature of
 * {@link Routine}s that declares a service that can be performed by instances
 * of {@link Service}s. It contains a service fiType and a selector of operation
 * of that service fiType (interface). Its implicit parameter and return execute is
 * a service {@link Context}. Thus, the explicit signature of service-oriented
 * operations is defined by the same {@link Context} fiType for any exertion
 * parameter and return execute . A signature may include a collection of optional
 * attributes describing a preferred {@link Service} with a given service fiType.
 * Also a signature can carry own implementation when its fiType is implemented
 * with the provided codebase.
 * <p>
 * In other words, a service signature is a specification of a service that can
 * be requested dynamically at the boundary of a service provider. Operations
 * include modifying a service {@link Context} or disclosing information about
 * the service context.
 *
 * @author Mike Sobolewski
 */
@SuppressWarnings("rawtypes")
public interface Signature extends Opservice, Exertion, Comparable, Dependency, Identifiable,
		Scopable, Arg, EvaluationComponent, SupportComponent, Serializable {

	/**
	 * Returns a name of this signature.
	 *
	 * @return name of signature
	 */
	String getName();

	/**
	 * Returns an operation name of this signature.
	 *
	 * @return name of operation
	 */
	String getSelector();

	/**
	 * Returns a fragment of operation of this signature. It's the part
	 * preceeding # in its selector.
	 *
	 * @return fragment of operation
	 */
	String getPrefix();

	/**
	 * Returns a service provider name.
	 *
	 * @return name of service provider
	 */
	ProviderName getProviderName();

	/**
	 * Returns a service provider.
	 *
	 * @return name of service provider
	 */
	Object getProvider() throws SignatureException;

	/**
	 * Returns a provider of <code>Variability</code> fiType.
	 *
	 * @return Variability of this service provider
	 */
	Functionality<?> getVariability();

	void setProviderName(ProviderName providerName);

	/**
	 * Returns a service fiType name of this signature.
	 *
	 * @return name of service interface
	 */
	Class<?> getServiceType();

	/**
	 * Assigns a service type of this signature.
	 *
	 * @param serviceType
	 *            service serviceType
	 */
	void setServiceType(Class<?> serviceType);

	/**
	 * Returns a service multitype of this signature.
	 *
	 * @return service multitype
	 */
	Multitype getMultitype() throws SignatureException;

	/**
	 * Assigns a service multi of this signature.
	 *
	 * @param multitype
	 *            service multitype
	 */
	void setMultitype(Multitype multitype);

	/**
	 * Returns an array of service types of this signature
	 * to be matched by its service proxy.
	 *
	 * @return array of service types matched by service proxy
	 */
	Class<?>[] getMatchTypes();

	/**
	 * Assigns a request return of this signature with a given return path.
	 *
	 * @param contextReturn
	 * 			a context return
	 */
	void setContextReturn(Context.Return contextReturn);

	void setContextReturn(String path);

	void setContextReturn();

	/**
	 * Assigns a request return to the return execute with a return path and directional attribute.
	 *
	 * @param path
	 *            the return path of the request return
	 * @param direction
	 *            the request return directional attribute
	 */
	void setContextReturn(String path, Direction direction);

	/**
	 * Returns a Context.Return to the return execute by this signature.
	 *
	 * @return Context.Return to the return execute
	 */
	Context.Return getContextReturn();

	/**
	 * Returns a signature Type of this signature.
	 *
	 * @return a Type of this signature
	 */
	Type getExecType();

	/**
	 * Returns a inConnector specifying output paths for existing
	 * paths in returned context for this signature.
	 *
	 * @return a context mapping output paths to existing returnPath
	 */
	Context getInConnector();

	/**
	 * Assigns a signature <code>fiType</code> for this service signature.
	 *
	 * @param type
	 *            a signature fiType
	 */
	Signature setType(Signature.Type type);

	/**
	 * Returns a codebase for the code implementing this signature. The codebase
	 * is a space separated string (list) of URls.
	 *
	 * @return a codebase for the code implementing this signature
	 */
	String getCodebase();

	/**
	 * Assigns a codbase to <code>urls</code> for the code implementing this
	 * signature. The codebase is a space separated string (list) of URls.
	 *
	 * @param urls
	 *            a list of space separated URLS
	 */
	void setCodebase(String urls);

	/**
	 *  Close and connectivity to the bound service provider.
	 * @throws RemoteException
	 * @throws IOException
	 */
	void close() throws RemoteException, IOException;

	/**
	 * Returns a deployment for provisioning a referenced service provider;
	 */
	Deployment getDeployment();

	/**
	 * Returns an access types to a provider, synchronous (PUSH) or asynchronous (PULL);
	 */
	Strategy.Access getAccessType();
	
	/**
	 * There are four types of {@link Signature} operations that can be
	 * associated with signatures: <code>PRE</code> (preprocess),
	 * <code>PRO</code> (process/service) , <code>POST</code> (postprocess), and
	 * <code>APD_DATA</code> (append data) and code>APD_CONTROL</code> (append
	 * control strategy). Only one <code>PRO</code> signature can be associated
	 * with the target process. The <code>PRO</code> signature defines an executing
	 * provider to be bounded at runtime. The <code>APD_DATA</code>
	 * signatures are invoked first to getValue specified contexts from
	 * {@link sorcer.service.Context}s that are appended to the task's current
	 * context.
	 */
	enum Type implements Arg {
		PRO, PRE, POST, SRV, APD_DATA, APD_CONTROL, BUILDER, CONTROLLER, DISPATCH;

		/* (non-Javadoc)
         * @see sorcer.service.Arg#getName()
         */
		@Override
		public String getName() {
			return toString();
		}

		public Object execute(Arg... args) {
			return this;
		}
	}

	/**
	 * Used to indicate if signature is active.
	 */
	enum Operating implements Arg {
		YES, NO, TRUE, FALSE;

		/* (non-Javadoc)
		 * @see sorcer.service.Arg#getName()
		 */
		@Override
		public String getName() {
			return toString();
		}

		public Object execute(Arg... args) {
			return this;
		}
	}

	/*enum Kind implements Arg {
		CONTEXT, DESIGN, TASKER, JOBBER, SPACER, DISPATCHER, OPTIMIZER, EXPLORER, SOLVER, DRIVER, MODEL, DISCIPLINE, MODEL_MANAGER;*/
	enum Kind implements Arg {
		CONTEXT, CONTEXTION, DESIGN, TASKER, JOBBER, SPACER, DISPATCHER, OPTIMIZER,
		EXPLORER, SOLVER, DRIVER, MORPHER, MODEL, DISCIPLINE, MODEL_MANAGER, PROVIDER;

		/* (non-Javadoc)
		 * @see sorcer.service.Arg#getName()
		 */
		@Override
		public String getName() {
			return toString();
		}

		public Object execute(Arg... args) {
			return this;
		}
	}

	enum Direction implements Arg {
		IN, OUT, INOUT, FROM, TO;

		/* (non-Javadoc)
		 * @see sorcer.service.Arg#getName()
		 */
		@Override
		public String getName() {
			return toString();
		}

		static public Direction fromString(String direction) {
			if (direction == null) {
				return null;
			} else if (direction.equals(""+IN)) {
				return IN;
			} else if (direction.equals(""+OUT)) {
				return OUT;
			} else if (direction.equals(""+INOUT)) {
				return INOUT;
			} else {
				return null;
			}
		}

		public Object execute(Arg... args) {
			return this;
		}
	}

	String SELF = "_self_";
	String SELF_VALUE = "_self_value_";
	Type SRV = Type.PRO;
	Type PRE = Type.PRE;
	Type POST = Type.POST;
	Type APD = Type.APD_DATA;

	class Read extends Paths {
		static long serialVersionUID = 1L;

		public Read() {
			super();
		}

		public Read(Path[] paths) {
			for (Path path : paths) {
				add(path) ;
			}
		}
		public Read(String[] names) {
			for (String name : names) {
				add(new Path(name)) ;
			}
		}
	}

	class Write extends Paths {
		static long serialVersionUID = 1L;

		public Write() {
			super();
		}

		public Write(Path[] paths) {
			for (Path path : paths) {
				add(path) ;
			}
		}
		public Write(String[] names) {
			for (String name : names) {
				add(new Path(name)) ;
			}
		}
	}

	class State extends Paths {
		static long serialVersionUID = 1L;

		public State() {
			super();
		}

		public State(Path[] paths) {
			for (Path path : paths) {
				add(path) ;
			}
		}
		public State(String[] names) {
			for (String name : names) {
				add(new Path(name)) ;
			}
		}
	}


	class Append extends Paths {
		static long serialVersionUID = 1L;

		public Append() {
			super();
		}

		public Append(Path[] paths) {
			for (Path path : paths) {
				add(path) ;
			}
		}
		public Append(String[] names) {
			for (String name : names) {
				add(new Path(name)) ;
			}
		}
	}

	class SessionPaths extends ArrayList<Paths> implements Arg {
		static long serialVersionUID = 1L;

		public SessionPaths() {
			super();
		}

		public SessionPaths(Paths[] lists) {
			for (Paths al : lists) {
				add(al);
			}
		}

		public Paths getPaths(Class<?> clazz) {
			for(Paths al : this) {
				if (clazz.isInstance(al)) {
					return al;
				}
			}
			return null;
		}

		@Override
		public String getName() {
			return toString();
		}

		public Object execute(Arg... args) {
			return this;
		}
	}

	class Operation implements Serializable, Arg {
		static long serialVersionUID = 1L;

		public String selector;

		public String path;

		public Strategy.Access accessType = Strategy.Access.PUSH;

		public Strategy.Flow flowType = Strategy.Flow.SEQ;

		public Strategy.Monitor toMonitor = Strategy.Monitor.NO;

		private List matchTokens;

		public Strategy.Wait toWait = Strategy.Wait.YES;

		public Strategy.FidelityManagement toManageFi = Strategy.FidelityManagement.NO;

		public Strategy.Shell isShellRemote = Strategy.Shell.LOCAL;

		public boolean isProvisionable = false;

		@Override
		public String getName() {
			return selector;
		}


		public List getMatchTokens() {
			return matchTokens;
		}

		public void setMatchTokens(List matchTokens) {
			this.matchTokens = matchTokens;
		}

		public Object execute(Arg... args) {
			return this;
		}
	}

	class Multitype implements Serializable, Arg {
		static long serialVersionUID = 1L;
		private static final Logger logger = LoggerFactory.getLogger(Multitype.class);

		public String typeName;

		// default prvType
		public Class providerType;

		// service types implemented by the service provider
		public Class[] matchTypes;

		public Multitype() {
			// do nothing
		}

		public Multitype(String className) {
			typeName = className;
		}

		public Multitype(Class classType) {
			providerType = classType;
		}

		@Override
		public String getName() {
			if (typeName != null) {
				return typeName;
			} else {
				return providerType.toString();
			}
		}

		public Class<?> getProviderType() {
			return getProviderType(null);
		}

		public Class<?> getProviderType(ClassLoader loader) {
			if (providerType != null) {
				return providerType;
			} else if (typeName != null) {
				try {
					if (loader == null)
						providerType = Class.forName(typeName);
					else
						providerType = Class.forName(typeName, true, loader);
				} catch (ClassNotFoundException e) {
					logger.warn("Unable to load " + typeName, e);
				}
			}
			return providerType;
		}

		@Override
		public String toString() {
			return (providerType != null ? providerType.getSimpleName() : "null")
					+ (matchTypes != null ? ":" + Arrays.toString(matchTypes) : "");
		}

		public Object execute(Arg... args) {
			return this;
		}
	}

}
