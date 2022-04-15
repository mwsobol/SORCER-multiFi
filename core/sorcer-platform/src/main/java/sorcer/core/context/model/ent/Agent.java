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

package sorcer.core.context.model.ent;

import sorcer.core.invoker.MethodInvoker;
import sorcer.service.Arg;
import sorcer.service.Context;
import sorcer.service.EvaluationException;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;

/**
 * @author Mike Sobolewski
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Agent<T> extends Pcr<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private final URL[] agentURLs;
	
	private String className;
	
	private MethodInvoker invoker;
	
	transient private URLClassLoader agentLoader;

	public Agent(String name, URL... agentURLs) {
		super(name);
		this.agentURLs = agentURLs;
	}
	
	public Agent(String name, String className, URL... agentURLs) {
		super(name);
		this.className = className;
		this.agentURLs = agentURLs;
	}

	public T process(Arg... entries) throws EvaluationException {
		if (invoker != null) {
			return (T) invoker.evaluate(getPars(entries));
		}

		if (className == null)
			className = getClassName(entries);

		ClassLoader cl = getClass().getClassLoader();
		try {
			agentLoader = URLClassLoader.newInstance(agentURLs, cl);
			final Thread currentThread = Thread.currentThread();
			final ClassLoader parentLoader = (ClassLoader) AccessController
					.doPrivileged((PrivilegedAction<?>) currentThread::getContextClassLoader);

			try {
				AccessController.doPrivileged((PrivilegedAction<?>) () -> {
					currentThread.setContextClassLoader(agentLoader);
					return (null);
				});
				Class<?> clazz;
				try {
					clazz = agentLoader.loadClass(className);
				} catch (ClassNotFoundException ex) {
					ex.printStackTrace();
					throw ex;
				}
				Constructor<?> constructor = clazz.getConstructor(Context.class);
				
				Object obj = constructor.newInstance(scope);
				invoker = new MethodInvoker(name, obj, name, entries);
				if (scope instanceof EntryModel)
					invoker.setInvokeContext(scope);
				invoker.setContext(scope);
			} finally {
				AccessController.doPrivileged((PrivilegedAction<?>) () -> {
					currentThread.setContextClassLoader(parentLoader);
					return (null);
				});
			}
		} catch (Throwable e) {
			throw new IllegalArgumentException(
					"Unable to instantiate pcr agent :"
							+ e.getClass().getName() + ": "
							+ e.getLocalizedMessage());
		}
		out = (T)invoker.evaluate(entries);
		invoker.setValid(true);
		return out;
	}
	
	@Override
	public T evaluate(Arg... args) throws EvaluationException {
		if (out != null && invoker != null && invoker.isValid())
			return out;
		else
			return process(args);
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#getAsis()
	 */
	@Override
	public T asis() {
		return (T) invoker;
	}
	
	private Pcr[] getPars(Arg... entries) {
		Pcr[] pa = new Pcr[entries.length];
		if (entries.length > 0) {
			for (int i = 0; i < entries.length; i++)
				pa[i] = ( Pcr ) entries[i];
		}
		return pa;
	}
	
	private String getClassName(Arg... entries) {
		for (Arg p : entries) {
			if (p instanceof Entry && p.getName().equals("class"))
				return (String)((Entry)p).getImpl();
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "agent [" + name + ":" + Arrays.toString(agentURLs) + "]";
	}
}
