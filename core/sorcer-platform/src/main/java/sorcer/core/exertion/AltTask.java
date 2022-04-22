/*
 *
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
package sorcer.core.exertion;

import net.jini.core.transaction.Transaction;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.ThrowableTrace;
import sorcer.service.*;
import sorcer.service.modeling.Conditional;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The alternative Routine that executes sequentially a collection of optional
 * domains. It executes the first optExertion in the collection such that its
 * condition is true.
 * 
 * @author Mike Sobolewski
 */
@SuppressWarnings("unchecked")
public class AltTask extends ConditionalTask {

	private static final long serialVersionUID = 4012356285896459828L;
	
	protected List<OptTask> optExertions;

	public AltTask(OptTask... optExertions) {
		super();
		this.optExertions = Arrays.asList(optExertions);
	}
	
	public AltTask(String name, OptTask... optExertions) {
		super(name);
		this.optExertions = Arrays.asList(optExertions);
	}

	public AltTask(String name, List<OptTask> optExertions) {
		super(name);
		this.optExertions = optExertions;
	}

	@Override
	public Task doTask(Transaction txn, Arg... args) throws EvaluationException {
		OptTask opt = null;
		try {
			for (int i = 0; i < optExertions.size(); i++) {
				opt = optExertions.get(i);
				if (opt.getCondition().isTrue()) {
					opt.isActive = true;
					Context cxt = opt.getCondition().getConditionalContext();
					if (cxt != null) {
						Condition.clenupContextScripts(cxt);
						((ServiceMogram)opt.getTarget()).getDataContext().updateEntries(cxt);
					}
					// pass te scope to the option task
//					opt.setContextScope(cxt);
					opt.setContextScope(dataContext);
					Mogram mog = (Mogram)opt.getTarget();
					if (mog instanceof Routine) {
                        ((Subroutine)mog).setContextScope(cxt);
                    } else {
                        mog.setScope(cxt);
                    }
					Routine out = mog.exert(txn, args);
					opt.setTarget(out);
					dataContext = (ServiceContext) out.getContext();
					controlContext.append(out.getControlContext());
					dataContext.putValue(Condition.CONDITION_VALUE, true);
					dataContext.putValue(Condition.CONDITION_TARGET, opt.getName());
					return this;
				}
			}
			dataContext.putValue(Condition.CONDITION_VALUE, false);
			dataContext.putValue(Condition.CONDITION_TARGET, opt.getName());
			dataContext.setRoutine(null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new EvaluationException(e);
		}
		return this;
	}
	
	public OptTask getActiveOptExertion() {
		OptTask active = null;
		for (OptTask oe : optExertions) {
			if (oe.isActive())
				return oe;
		}
		return active;
	}
		
	public List<OptTask> getOptExertions() {
		return optExertions;
	}

	public void setOptExertions(List<OptTask> optExertions) {
		this.optExertions = optExertions;
	}

	public OptTask getOptExertion(int index) {
		return optExertions.get(index);
	}
	
	public boolean isConditional() {
		return true;
	}
	
	public void reset(int state) {
			for (Subroutine e : optExertions)
				e.reset(state);
		
		this.setStatus(state);
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.modeling.Conditional#getConditions()
	 */
	@Override
	public List<Conditional> getConditions() {
		List<Conditional> cs = new ArrayList<Conditional>(optExertions.size());
		for (OptTask oe : optExertions)
			cs.add(oe.getCondition());
		return cs;
	}

	@Override
	public List<ThrowableTrace> getExceptions(List<ThrowableTrace> exceptions) throws RemoteException {
		for (Routine ext : optExertions) {
			exceptions.addAll(((Subroutine)ext).getExceptions(exceptions));
		}
		exceptions.addAll(this.getExceptions());
		return exceptions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.service.Routine#getMograms()
	 */
	@Override
	public List<Contextion> getMograms() {
		ArrayList<Contextion> list = new ArrayList<Contextion>(1);
		list.addAll(optExertions);
		return list;
	}
	
	public List<Contextion> getMograms(List<Contextion> exs) {
		for (Routine e : optExertions) {
			((ServiceMogram)e).getMograms(exs);
		}
		exs.add(this);
		return exs;
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.ConditionalExertion#getTargets()
	 */
	@Override
	public List<Contextion> getTargets() {
		List<Contextion> tl = new ArrayList<Contextion>(optExertions.size());
		for (OptTask oe : optExertions)
			tl.add(oe.getTarget());
		return tl;
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Routine#isCompound()
	 */
	@Override
	public boolean isCompound() {
		return false;
	}

}
