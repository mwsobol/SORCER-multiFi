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

import net.jini.id.Uuid;
import sorcer.core.context.ControlContext;
import sorcer.core.context.ServiceContext;
import sorcer.service.modeling.Exploration;
import sorcer.service.modeling.dmn;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Mike Sobolewski
 */
abstract public class Transroutine extends Subroutine implements Transdomain, dmn {
	/**
	 * Component domains of this job (the Composite Design pattern)
	 */
	protected List<Contextion> mograms = new ArrayList<Contextion>();

	protected Fidelity<Analysis> analyzerFi;

	protected Fidelity<Exploration> explorerFi;

	protected Map<String, Context> childrenContexts;

	public Transroutine() {
		this("transroutine-" + count++);
	}

	public Transroutine(String name) {
		super(name);
		mograms = new ArrayList();
	}

	public boolean isCompound() {
		return true;
	};

	public int size() {
		return   mograms.size();
	};

	public void reset(int state) {
		for(Contextion e : mograms)
			((Subroutine)e).reset(state);

		this.setStatus(state);
		this.setStatus(state);
	}

	/**
	 * Replaces the exertion at the specified position in this list with the
	 * specified element.
	 */
	public void setMogramAt(Mogram ex, int i) {
		mograms.set(i, ex);
	}

	public Routine getMasterExertion() {
		Uuid uuid = null;
		try {
			uuid = (Uuid) controlContext.getValue(ControlContext.MASTER_EXERTION);
		} catch (ContextException ex) {
			ex.printStackTrace();
		}
		if (uuid == null
				&& controlContext.getFlowType().equals(ControlContext.SEQUENTIAL)) {
			return (size() > 0) ? get(size() - 1) : null;
		} else {
			Routine master = null;
			for (int i = 0; i < size(); i++) {
				if (((Subroutine) get(i)).getId().equals(
						uuid)) {
					master = get(i);
					break;
				}
			}
			return master;
		}
	}

	public Contextion removeExertion(Contextion mogram) throws ContextException {
		// int index = ((ExertionImpl)exertion).getIndex();
		mograms.remove(mogram);
		controlContext.deregisterExertion(this, (Mogram)mogram);
		return mogram;
	}

	public void remove(int index) throws ContextException {
		removeExertion(get(index));
	}

	/**
	 * Returns the exertion at the specified index.
	 */
	public Routine get(int index) {
		return (Routine) mograms.get(index);
	}

	public void setMograms(List<Contextion> mograms) {
		this.mograms = mograms;

	}

	public Mogram addExertion(Routine exertion, int priority) throws RoutineException {
		addMogram(exertion);
		controlContext.setPriority(exertion, priority);
		return this;
	}

	@Override
	public Fidelity<Exploration> getExplorerFi() {
		return explorerFi;
	}

	public void setExplorerFi(Fidelity<Exploration> explorerFi) {
		this.explorerFi = explorerFi;
	}

	/**
	 * Returns all component <code>Mograms</code>s of this composite exertion.
	 *
	 * @return all component domains
	 */
	public List<Contextion> getMograms() {
		return mograms;
	}

	public List<Contextion> getAllMograms() {
		List<Contextion> allMograms = new ArrayList<>();
		return getMograms(allMograms);
	}

	public List<Contextion> getAllContextions() throws RemoteException {
		List<Contextion> allContextions = new ArrayList<>();
		return getContextions(allContextions);
	}

	public List<Contextion> getMograms(List<Contextion> mogramList) {
		for (Contextion e : mograms) {
			((ServiceMogram)e).getMograms(mogramList);
		}
		mogramList.add(this);
		return mogramList;
	}

	public List<Contextion> getContextions(List<Contextion> contextionList) throws RemoteException {
		for (Contextion e : mograms) {
			e.getContextions(contextionList);
		}
		contextionList.add(this);
		return contextionList;
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

	public int compareByIndex(Routine e) {
		if (this.getIndex() > ((Transroutine) e).getIndex())
			return 1;
		else if (this.getIndex() < ((Transroutine) e).getIndex())
			return -1;
		else
			return 0;
	}

	@Override
	public Object get(String component) {
		List<Contextion> allMograms = getAllMograms();
		for (Contextion mog : allMograms) {
			if (mog.getName().equals(component)) {
				return mog;
			}
		}
		return null;
	}

	abstract public Contextion getComponentMogram(String path);
	
	abstract public Context getComponentContext(String path) throws ContextException;

	@Override
	public Context evaluate(Context context, Arg... args) throws EvaluationException {
		try {
			((ServiceContext)getContext()).substitute(context);
			Routine out = exert(args);
			return out.getContext();
		} catch (ServiceException e) {
			throw new EvaluationException(e);
		}
	}

	@Override
	public Map<String, Contextion> getChildren() {
		Map<String, Contextion> children = new HashMap<>();
		for (Contextion mog : mograms) {
			children.put(mog.getName(), mog);
		}
		return children;
	}

	@Override
	public Map<String, Context> getChildrenContexts() {
		return childrenContexts;
	}

	public void setChildrenContexts(Map<String, Context> childrenContexts) {
		this.childrenContexts = childrenContexts;
	}

	@Override
	public Fidelity<Analysis> getAnalyzerFi() {
		return analyzerFi;
	}

	public void setAnalyzerFi(Fidelity<Analysis> analyzerFi) {
		this.analyzerFi = analyzerFi;
	}


}
