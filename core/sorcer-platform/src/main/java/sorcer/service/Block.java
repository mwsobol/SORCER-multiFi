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
import net.jini.core.transaction.TransactionException;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.core.context.model.ent.Entry;
import sorcer.core.exertion.AltTask;
import sorcer.core.exertion.LoopTask;
import sorcer.core.exertion.OptTask;
import sorcer.service.modeling.ExploreException;
import sorcer.util.SorcerUtil;
import sorcer.util.url.sos.SdbUtil;


import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

/**
 * @author Mike Sobolewski
 */
public abstract class Block extends Transroutine {
	
	private URL contextURL;
	
	public Block(String name) {
		super(name);
	}

	public Block(String name, Signature signature) {
		super(name);
		try {
			ServiceFidelity sFi = new ServiceFidelity(signature);
			sFi.setSelect(signature);
			((ServiceFidelity)multiFi).getSelects().add(sFi);// Add the signature
			multiFi.setSelect(sFi);

			setContext(new EntryModel("block context: " + getName()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Block(String name, Signature signature, Context context)
			throws SignatureException {
		this(name, signature);
		if (context != null)
			this.dataContext = (ServiceContext) context;
	}
	
	public abstract Block doBlock(Transaction txn, Arg... args) throws RoutineException,
		SignatureException, RemoteException, TransactionException, MogramException, ServiceException;
	
	/* (non-Javadoc)
	 * @see sorcer.service.Routine#addMogram(sorcer.service.Routine)
	 */
	@Override
	public Contextion addMogram(Contextion mogram) throws RoutineException {
		mograms.add(mogram);
		((ServiceMogram)mogram).setIndex(mograms.indexOf(mogram));
		try {
			controlContext.registerExertion((Mogram)mogram);
		} catch (ContextException | RemoteException e) {
			throw new RoutineException(e);
		}
		((ServiceMogram)mogram).setParentId(getId());
		return this;
	}

	public void setMograms(List<Contextion> mograms) {
		this.mograms = mograms;
	}

	public void setMograms(Mogram[] mograms) throws RoutineException {
		for (Mogram mo :mograms)
			addMogram(mo);
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Routine#execute(java.lang.String, sorcer.service.Arg[])
	 */
	@Override
	public Object getValue(String path, Arg... args) throws ContextException {
		dataContext.getValue(path, args);
		return null;
	}

	@Override
	public ServiceContext getDataContext() throws ContextException  {
		if (contextURL != null) {
			try {
				return (ServiceContext)contextURL.getContent();
			} catch (IOException e) {
				throw new ContextException(e);
			}
		} else {
			return dataContext;
		}
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Routine#getMograms()
	 */
	@Override
	public List<Contextion> getMograms() {
		return mograms;
	}

	@Override
	public List<Contextion> getContextions() {
		List<Contextion> contextions = new ArrayList<>();
		contextions.addAll(mograms);
		return contextions;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Subroutine#linkContext(sorcer.service.Context, java.lang.String)
	 */
	@Override
	public Context linkContext(Context context, String path)
			throws ContextException {
		dataContext.putLink(path + CPS + key, context);
		return context;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Subroutine#linkControlContext(sorcer.service.Context, java.lang.String)
	 */
	@Override
	public Context linkControlContext(Context context, String path)
			throws ContextException {
		controlContext.putLink(path + CPS + key, context);
		return context;
	}

	public boolean isBlock() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Subroutine#isTree(java.util.Set)
	 */
	@Override
	public boolean isTree(Set visited) {
		visited.add(this);
		Iterator i = mograms.iterator();
		while (i.hasNext()) {
			Subroutine e = (Subroutine) i.next();
			if (visited.contains(e) || !e.isTree(visited)) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Subroutine#getMograms(java.util.List)
	 */
	@Override
	public List<Contextion> getMograms(List<Contextion> mogramList) {
		for (Contextion e : mograms) {
			((ServiceMogram)e).getMograms(mogramList);
		}
		mogramList.add(this);
		return mogramList;
	}

	@Override
	public List<Contextion> getContextions(List<Contextion> contextionList) throws RemoteException {
		for (Contextion e : mograms) {
			e.getContextions(contextionList);
		}
		contextionList.add(this);
		return contextionList;
	}

	public URL persistContext() throws ServiceException, SignatureException, RemoteException {
		if (contextURL == null) {
			contextURL = SdbUtil.store(dataContext);
			dataContext = null;
		} else {
			SdbUtil.update(dataContext);
		}
		return contextURL;
	}

	/**
	 * Returns the number of domains in this Block.
	 * 
	 * @return the number of domains in this Block.
	 */
	public int size() {
		return mograms.size();
	}

	public void remove(int index) {
		new RuntimeException().printStackTrace();
		mograms.remove(index);
	}

	/**
	 * Replaces the exertion at the specified position in this list with the
     * specified element.
	 */
	public void setMogramAt(Mogram ex, int i) {
		mograms.set(i, ex);
	}
	
	/**
	 * Returns the exertion at the specified index.
	 */
	public Routine get(int index) {
		return (Routine) mograms.get(index);
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Transroutine#isCompound()
	 */
	@Override
	public boolean isCompound() {
		return true;
	}
	
	public boolean hasChild(String childName) {
		for (Contextion ext : mograms) {
			if (ext.getName().equals(childName))
				return true;
		}
		return false;
	}

	public Contextion getChild(String childName) {
		for (Contextion ext : mograms) {
			if (ext.getName().equals(childName))
				return ext;
		}
		return null;
	}

	public Contextion getComponentMogram(String path) {
		// TODO
		return getChild(path);
	}
	
	public Object putBlockValue(String path, Object value) throws ContextException, RemoteException {
		String[] attributes = SorcerUtil.pathToArray(path);
		// remove the leading attribute of the current exertion
		if (attributes[0].equals(getName())) {
			// updated this context
			if ((attributes.length >= 2) && !hasChild(attributes[1])) {
				dataContext.putValue(path.substring(key.length() + 1), value);
				return value;
			}
			String[] attributes1 = new String[attributes.length - 1];
			System.arraycopy(attributes, 1, attributes1, 0,
					attributes.length - 1);
			attributes = attributes1;
		}
		String last = attributes[0];
		Mogram exti = this;
		for (String attribute : attributes) {
			if (((Subroutine) exti).hasChild(attribute)) {
				exti = (Mogram) ((Transroutine) exti).getChild(attribute);
				if (exti instanceof Task) {
					last = attribute;
					break;
				}
			} else {
				break;
			}
		}
		int index = path.indexOf(last);
		String contextPath = path.substring(index + last.length() + 1);
		exti.getContext().putValue(contextPath, value);
		return value;
	}
	
	public void reset(int state) {
		for(Contextion e : mograms)
			((ServiceMogram)e).reset(state);
		
		this.setStatus(state);
	}

	/**
	 *  TODO
	 * @param path
	 * @return
	 * @throws ContextException
	 */
	@Override
	public Context getComponentContext(String path) throws ContextException {
		return null;
	}

	@Override
	public void substitute(Arg... entries)
			throws SetterException {
		try {
			for (Arg e : entries) {
				if (e instanceof Entry) {
					if (e.getName().indexOf(key) >= 0) {
						putBlockValue(e.getName(), ((Entry) e).getValue());
					} else {
						super.putValue(e.getName(), ((Entry) e).getValue());
					}
				}
			}
			updateConditions();
		} catch (ContextException | RemoteException ex) {
			ex.printStackTrace();
			throw new SetterException(ex);
		}
	}
	
	private void updateConditions() throws ContextException, RemoteException {
		for (Contextion mogram : mograms) {
			if (mogram instanceof Mogram && ((Mogram)mogram).isConditional()) {
				if (mogram instanceof OptTask) {
					((OptTask)mogram).getCondition().getConditionalContext().append(dataContext);
				} else if (mogram instanceof LoopTask && ((LoopTask) mogram).getCondition() != null) {
					((LoopTask) mogram).getCondition().getConditionalContext().append(dataContext);
				} else if (mogram instanceof AltTask) {
					for (OptTask oe : ((AltTask) mogram).getOptExertions()) {
						oe.getCondition().getConditionalContext().append(dataContext);
					}
				}
			}
		}
	}

	public Mogram clearScope() throws ContextException {
		Object[] paths = getDataContext().keySet().toArray();
		for (Object path : paths) {
			dataContext.removePath((String) path);
		}

		Context.Return rp = dataContext.getContextReturn();
		if (rp != null && rp.returnPath != null)
			dataContext.removePath(rp.returnPath);

		List<Contextion> mograms = getAllMograms();
		Context cxt = null;
		for (Contextion mo : mograms) {
			if (mo instanceof Mogram) {
				((ServiceMogram) mo).getDataContext().clearScope();
			}

//			if (mo instanceof Mogram)
//				cxt = mo.getContext();
//			else
//				cxt = (Context) mo;

//			if (!(mo instanceof Block)) {
//				try {
//					cxt.setScope(null);
//				} catch (RemoteException e) {
//					throw new ContextException(e);
//				}
//			}
//			try {
//				if (mo instanceof Routine) {
//					((Routine) mo).clearScope();
//					// set the initial scope from the block
//					mo.setScope((Context) dataContext.getScope());
//				}
//			} catch (RemoteException e) {
//				throw new ContextException(e);
//			}
		}

		// restore initial context
		if (dataContext.getInitContext() != null) {
			dataContext.append(dataContext.getInitContext());
		}

		return this;
	}

	@Override
	public Context analyze(Context context, Arg... args) throws EvaluationException {
		try {
			return exert(context);
		} catch (ServiceException e) {
			throw new EvaluationException(e);
		}
	}

	@Override
	public Context explore(Context context, Arg... args) throws ContextException {
		try {
			return exert(context);
		} catch (ServiceException e) {
			throw new ContextException(e);
		}
	}
}
