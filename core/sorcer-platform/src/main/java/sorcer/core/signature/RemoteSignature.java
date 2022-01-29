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

package sorcer.core.signature;

import net.jini.core.entry.Entry;
import net.jini.core.transaction.Transaction;
import net.jini.lookup.entry.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.exertion.NetTask;
import sorcer.core.provider.*;
import sorcer.service.*;
import sorcer.service.modeling.sig;
import sorcer.util.MavenUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import static sorcer.eo.operator.*;

/**
 * Represents a handle to remote service provider.
 *
 * Created by Mike Sobolewski
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class RemoteSignature extends LocalSignature implements sig {

	private static final long serialVersionUID = 1L;

	/**
	 * URL for a mobile agent: an inserted custom method executed by service
	 * providers
	 */
	private String agentCodebase;

	/** codebase for downloaded classes */
	private String codebase = System.getProperty("java.rmi.server.codebase");

	private String agentClass;

	private String portalURL;

	private boolean isUnicast = false;

	// provider bound to this signature
	transient private Exerter provider;

	protected List<Entry> attributes;

    protected String version;
    private static final Logger logger = LoggerFactory.getLogger(LocalSignature.class);

	public RemoteSignature() {
		providerName = new ProviderName();
	}

	public RemoteSignature(ServiceSignature signature) {
		this.name = signature.name;
		this.operation = signature.operation;
		this.providerName =  signature.providerName;
		this.multitype = signature.multitype;
		this.deployment = signature.deployment;
		this.contextReturn = signature.contextReturn;
	}

	public RemoteSignature(Class<?> serviceType) {
		this("none", serviceType, ANY);
	}
	
	public RemoteSignature(String selector, Class<?> serviceType) {
		this(selector, serviceType, ANY);
	}


	public RemoteSignature(String selector, Class<?> serviceType, ProviderName providerName) {
		this(selector, serviceType);
		this.providerName =  providerName;
		execType = Type.PRO;
	}


	public RemoteSignature(String selector, Class<?> serviceType,
						   String providerName) {
		this(selector, serviceType, providerName, (Type) null);
	}

	public RemoteSignature(String selector, Class<?> serviceType,
						   Type methodType) {
		this(selector, serviceType);
		this.execType = methodType;
	}

	public RemoteSignature(String selector, Class<?> serviceType,
						   List<Entry> attributes, Type methodType) {
		this(selector, serviceType);
		this.execType = methodType;
		this.attributes = attributes;
	}

	public RemoteSignature(String selector, Class<?> serviceType,
						   String providerName, Type methodType) {

		this(selector, serviceType, providerName, methodType, null);
	}

	public RemoteSignature(String selector, Class<?> serviceType,
						   String providerName, Type methodType, Version version) {
		this.version = version!=null ? version.getName() : null;
		this.multitype.providerType = serviceType;
        if (serviceType != null && version == null)
            this.version = MavenUtil.findVersion(serviceType);
		if (providerName == null || providerName.length() == 0)
			this.providerName = new ProviderName(ANY);
		else
			this.providerName = new ProviderName(providerName);
		if (methodType == null) 
			execType = Type.PRO;
		else
			execType = methodType;
		
		setSelector(selector);
	}

    /**
    String version of constructor - required i.e. when running from Scilab
    */
    public RemoteSignature(String selector, String strServiceType) {
        try {
            Class<?> serviceType = Class.forName(strServiceType);
            this.multitype.providerType = serviceType;
            this.version = MavenUtil.findVersion(serviceType);
            setSelector(selector);
        } catch (ClassNotFoundException e) {
            logger.error("Problem creating RemoteSignature: " + e.getMessage());
        }
    }

    public RemoteSignature(String selector, Class<?> serviceType, String version,
						   String providerName, Type methodType) {
        this(selector, serviceType, providerName, methodType);
        if (version!=null) this.version = version;
    }

    public RemoteSignature(String selector, Class<?> serviceType, String version,
						   String providerName) {
        this(selector, serviceType, version, providerName, null);
    }


    public void setExertion(Routine exertion) {
        this.exertion = exertion;
	}

	public Routine getExertion() {
		return exertion;
	}

	public void setAttributes(List<net.jini.core.entry.Entry> attributes) {
		this.attributes = attributes;
	}

	public List<Entry> getAttributes() {
		if (attributes == null || attributes.size() == 0) {
			Entry[] atts = new Entry[] { new Name(providerName.getName()) };
			return Arrays.asList(atts);
		}
		return attributes;
	}

	public void addAttribute(Entry attribute) {
		attributes.add(attribute);
	}

	public void addAllAttributes(List<Entry> attributes) {
		this.attributes.addAll(attributes);
	}

    public Exerter getService() throws SignatureException {
        if (provider == null) return provider;
        try {
            // ping provider to see if alive
            provider.getProviderName();
        } catch (RemoteException e) {
            // provider is dead; getValue new one
            //e.printStackTrace();
            provider = null;
            provider = (Exerter)Accessor.get().getService(this);
        }
        return provider;
    }

    public Exerter getProvider() {
        return provider;
    }

    public void setProvider(Service provider) {
        this.provider = (Exerter)provider;
    }

	public String action() {
		String pn = (providerName == null) ? ANY : providerName.getName();
		return multitype + ", " + operation.selector + ", " + pn;
	}

	public ProviderName getProviderName() {
		return providerName;
	}

	public void setProviderName(String name) {
		providerName.setName(name);
	}

	public void setOwnerId(String oid) {
		ownerID = oid;
	}

	public String getOwnerID() {
		return ownerID;
	}

	public String getAgentCodebase() {
		return agentCodebase;
	}

	public void setAgentCodebase(String codebase) {
		agentCodebase = codebase;
	}

	public String getAgentClass() {
		return agentClass;
	}

	public void setAgentClass(String className) {
		agentClass = className;
	}

	public String getPortalURL() {
		return portalURL;
	}

	public void setPortalURL(String url) {
		portalURL = url;
	}

	public boolean equals(ServiceSignature method) {
		return method != null && toString().equals(method.toString());
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public String getCodebase() {
		return codebase;
	}

	public void setCodebase(String codebase) {
		this.codebase = codebase;
	}

	@Override
	public void close() throws IOException {
		if (provider instanceof Closing)
			((Closing)provider).close();
	}

	public Routine invokeMethod(Routine ex) throws RoutineException {
		// If customized method provided by Mobile Agent
		Method m = getSubstituteMethod(new Class[] { Mogram.class });
		try {
			if (m != null)
				return (Routine) m.invoke(this, new Object[] { ex });

			if (((ServiceExerter) provider).isValidMethod(operation.selector)) {
				return ((ServiceExerter) provider).getDelegate()
						.invokeMethod(operation.selector, ex);
			} else {
				RoutineException eme = new RoutineException(
						"Not supported method: " + multitype + "#" + operation.selector
								+ " by: "
								+  provider.getProviderName());
				((ServiceExerter) provider).notifyException(ex, "unsupported method",
						eme);
				throw eme;
			}
		} catch (Exception e) {
			throw new RoutineException(e);
		}
	}

	public Context invokeMethod(Context context) throws RoutineException {
		// If customized method provided by Mobile Agent
		Method m = getSubstituteMethod(new Class[] { Context.class });
		try {
			if (m != null)
				return ((Context) m.invoke(this, new Object[] { context }));

			if (((ServiceExerter) provider).isValidMethod(operation.selector)) {
				return ((ServiceExerter) provider)
						.getDelegate().invokeMethod(operation.selector, context);
			} else {
				RoutineException eme = new RoutineException(
						"Not supported method: " + multitype + "#" + operation.selector
								+ " by: "
								+ provider.getProviderName());
				((ServiceExerter) provider).notifyException(context.getMogram(),
						"unsupported method", eme);
				throw eme;
			}
		} catch (Exception e) {
			throw new RoutineException(e);
		}
	}

	@Override
	public Context exert(Contextion mogram) throws MogramException {
		return exert(mogram, null);
	}

	@Override
	public Context exert(Contextion mogram, Transaction txn, Arg... args) throws MogramException {
		try {
			Exerter prv;
			if (this.isShellRemote()) {
				prv = (Exerter) Accessor.get().getService(sig(RemoteServiceShell.class));
				return prv.exert(mogram, txn).getContext();
			}
			Context cxt, out;
			NetTask task = null;
			if (mogram instanceof NetTask) {
				task = (NetTask)mogram;
			}
			if (mogram instanceof Context) {
				cxt = (Context) mogram;
			} else {
				cxt = Arg.selectContext(args);
			}
			if (task != null) {
				if (cxt != null) {
					task.setDataContext(cxt);
				}
				out = task.exert(txn).getContext();
			} else {
				out = new NetTask(this, cxt).exert(txn).getContext();
			}

			return out;
		} catch (Exception e) {
			throw new ContextException(e);
		}
	}

	public boolean isUnicast() {
		return isUnicast;
	}

	public void setUnicast(boolean isUnicast) {
		this.isUnicast = isUnicast;
	}

    public String getVersion() {
        return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String toString() {

		return this.getClass().getSimpleName() + ":" + providerName + ":"
				+ multitype + "." + operation.selector
				+ (prefix != null ? "#" + prefix : "")
				+ (contextReturn != null ? "; result: " + contextReturn : "")
				+ ("; provisionable: " + operation.isProvisionable)
				+ ((deployment != null && deployment.getConfig() != null)
					? "; config: " + deployment.getConfig() : "");
	}

	@Override
	public Object execute(Arg... args) throws ServiceException, RemoteException {
		Routine mog = Arg.selectRoutine(args);
		Context cxt = (Context) Arg.selectDomain(args);
		if (cxt == null && contextReturn != null) {
			cxt = contextReturn.getDataContext();
		}
		Mogram result = null;
		try {
			if (mog != null && cxt == null) {
				if (multitype.providerType == RemoteServiceShell.class) {
					Exertion prv = (Exertion) Accessor.get().getService(sig(RemoteServiceShell.class));
					result = prv.exert(mog, null);
				} else {
					if (mog.getProcessSignature() != null
							&& ((ServiceSignature) mog.getProcessSignature()).isShellRemote()) {
						Exertion prv = (Exertion) Accessor.get().getService(sig(RemoteServiceShell.class));
						result = prv.exert(mog, null);
					} else {
						result = (exert(mog));
					}
				}
			} else if (cxt != null) {
				Context out;
				Context.Return rp = contextReturn;
				if (rp == null) {
					rp = cxt.getContextReturn();
				}
				if (rp != null && rp.returnPath != null) {
					cxt.setContextReturn(rp);
					out = exert(task(this, cxt));
					return out.getValue(rp.returnPath);
				}
				out = exert(task(this, cxt));
				return out;
			}
		} catch (SignatureException | ServiceException ex) {
			throw new MogramException(ex);
		}
		return context(result);
	}
}
