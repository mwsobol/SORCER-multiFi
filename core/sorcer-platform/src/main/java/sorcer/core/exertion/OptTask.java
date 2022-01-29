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
import java.util.List;

/**
 * The option Routine. There is a single target exertion that executes if the
 * condition is true (like if... then).
 * 
 * @author Mike Sobolewski
 */
@SuppressWarnings("rawtypes")
public class OptTask extends ConditionalTask {

	private static final long serialVersionUID = 172930501527871L;
	
	protected boolean isActive = false;
	
	public OptTask(String name) {
		super(name);
	}
		
	public OptTask(String name, Mogram mogram) {
		super(name);
		this.condition = new Condition(true);
		this.target = mogram;
	}
	public OptTask(Condition condition, Mogram mogram) {
		super();
		this.condition = condition;
		this.target = mogram;
	}
	
	public OptTask(String name, Condition condition, Mogram mogram) {
		super(name);
		this.condition = condition;
		this.target = mogram;
	}

	public Task doTask(Transaction txn, Arg... args) throws EvaluationException {
		try {

			if (condition.isTrue()) {
				isActive = true;
				// pass the scope to taget
				target.getContext().setScope(dataContext.getScope());
				target = target.exert(txn, args);
//				if (target.getScope() != null) {
//					((Context) target.getScope()).append(dataContext);
//				} else {
//					target.setScope(dataContext);
//				}
				dataContext = (ServiceContext) ((ServiceMogram)target).getDataContext();
				if (target instanceof Routine) {
					target.getContext().setRoutine(null);
					controlContext.append(((Routine)target).getControlContext());
				}
				dataContext.putValue(Condition.CONDITION_VALUE, true);
				dataContext.putValue(Condition.CONDITION_TARGET, target.getName());

				dataContext.setRoutine(null);
				return this;
			} else {
				dataContext.putValue(Condition.CONDITION_VALUE, false);
				dataContext.putValue(Condition.CONDITION_TARGET, target.getName());
				return this;
			}
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
	}
		
	public boolean isActive() {
		return isActive;
	}

	public boolean isConditional() {
		return true;
	}

	public void reset(int state) {
		((Subroutine)target).reset(state);
		this.setStatus(state);
	}

	/* (non-Javadoc)
	 * @see sorcer.service.modeling.Conditional#getConditions()
	 */
	@Override
	public List<Conditional> getConditions() {
		List<Conditional> cs = new ArrayList<Conditional>();
		if (condition != null)
			cs.add(condition);
		return cs;
	}
	
	public List<Contextion> getMograms(List<Contextion> exs) {
		exs.add(target);
		exs.add(this);
		return exs;
	}
	
	@Override
	public List<ThrowableTrace> getExceptions(List<ThrowableTrace> exceptions) throws RemoteException {
		exceptions.addAll(((ServiceMogram)target).getExceptions());
		exceptions.addAll(this.getExceptions());
		return exceptions;
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.ConditionalExertion#getTargets()
	 */
	@Override
	public List<Contextion> getTargets() {
		List<Contextion> tl = new ArrayList<Contextion>();
		tl.add(target);
		return tl;
	}
}
