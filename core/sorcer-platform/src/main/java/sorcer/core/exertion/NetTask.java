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

package sorcer.core.exertion;

import net.jini.core.transaction.Transaction;
import net.jini.id.Uuid;
import sorcer.core.context.ServiceContext;
import sorcer.core.provider.ProviderName;
import sorcer.core.provider.exerter.ServiceShell;
import sorcer.core.signature.RemoteSignature;
import sorcer.service.*;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

/**
 * The SORCER service task extending the abstract task {@link Task}.
 * 
 * @author Mike Sobolewski
 */
public class NetTask extends ObjectTask implements Invocation<Object> {

	private static final long serialVersionUID = -6741189881780105534L;

	public NetTask() {
		// do nothing
	}

	public NetTask(String name) {			
		super(name);
	}

	public NetTask(Uuid jobId, int jobState) {
		this("net task-" + count++);
		setParentId(jobId);
		status = jobState;
	}

	public NetTask(String name, String description) {
		this(name);
		this.description = description;
	}

	public NetTask(String name, Signature signature)
			throws SignatureException {
		this(name, null, signature, null);
	}

	public NetTask(String name, String description, Signature signature)
			throws SignatureException {
		this(name, description, signature, null);
	}

	public NetTask(String name, Signature signature, Context context)
			throws SignatureException {
		this(name, null, signature, context);
	}
	public NetTask(Signature signature, Context context)
			throws SignatureException {
		this("task-" + count++, null, signature, context);
	}
	
	public NetTask(String name, String description, Signature signature,
			Context context) throws SignatureException {
		this(name, description);
		if (signature instanceof RemoteSignature)
			addSignature(signature);
		else
			throw new SignatureException("Net task requires RemoteSignature: "
					+ signature);
		if (context != null)
			setContext(context);
	}

	public NetTask(String name, Signature[] signatures, Context context)
			throws SignatureException {
		this(name);
		setContext(context);

		for (Signature s : signatures) {
			if (s instanceof RemoteSignature)
				((RemoteSignature) s).setExertion(this);
			else
				throw new SignatureException("Net task requires RemoteSignature: " + s);
		}
		ServiceFidelity sFi = ((ServiceFidelity)multiFi.getSelect());
		sFi.getSelects().addAll(Arrays.asList(signatures));
		sFi.setSelect(signatures[0]);
	}

	public void setService(Service provider) {
		((RemoteSignature) getProcessSignature()).setProvider(provider);
	}

	public Service getService() throws SignatureException {
		return ((RemoteSignature) getProcessSignature()).getService();
	}

	public Task doTask(Transaction txn, Arg... args) throws ServiceException {
		List<String> traceList = dataContext.getTraceList();
		if (delegate != null) {
			if (traceList != null) {
				traceList.add(getClass().getSimpleName()
					+"#doTask>"+">"+getProcessSignature());
			}
			return delegate.doTask(txn, args);
		} else {
			ServiceShell se = new ServiceShell(this);
			try {
				if (traceList != null) {
					traceList.add(getClass().getSimpleName()
						+"#doTask>"+">"+getProcessSignature());
				}
				return se.exert(args);
			} catch (ServiceException e) {
				throw new EvaluationException(e);
			}
		}
	}

	public static NetTask getTemplate() {
		NetTask temp = new NetTask();
		temp.status = INITIAL;
		temp.priority = null;
		temp.index = null;
		temp.multiFi = null;
		return temp;
	}

	public static NetTask getTemplate(String provider) {
		NetTask temp = getTemplate();
		temp.getProcessSignature().setProviderName(new ProviderName(provider));
		return temp;
	}

	@Override
	public Object execute(Arg... args) throws ServiceException {
		return doTask(null, args);
	}
}
