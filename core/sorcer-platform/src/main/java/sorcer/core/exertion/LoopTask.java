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
import sorcer.core.context.model.req.RequestModel;
import sorcer.core.invoker.Pipeline;
import sorcer.service.*;
import sorcer.service.modeling.Conditional;
import sorcer.service.modeling.Model;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
//import sorcer.service.Condition;
/**
 * The loop Routine executes its target exertion while its condition is true.
 * Other types of looping types depend on parameters provided as described for
 * each LoopExertion constructor.
 * 
 * @author Mike Sobolewski
 * 
 */
public class LoopTask extends ConditionalTask {

	private static final long serialVersionUID = 8538804142085766935L;
	
	private int min = 0;

	private int max = 0;

	/**
	 * Loop: while(true) { operand }
	 * 
	 * @param name
	 * @param exertion
	 */
	public LoopTask(String name, Routine exertion) {
		super(name);
		condition = new Condition(true);
		target = exertion;
	}

	/**
	 * Iteration: for i = n to m { operand }
	 * 
	 * @param name
	 * @param min
	 * @param max
	 * @param mogram
	 */
	public LoopTask(String name, int min, int max, Mogram mogram) {
		super(name);
		this.min = min;
		this.max = max;
		target = mogram;
	}

	/**
	 * Loop: while (condition) { operand }
	 * 
	 * @param name
	 * @param condition
	 * @param mogram
	 */
	public LoopTask(String name, Condition condition, Contextion mogram) {
		super(name);
		this.condition = condition;
		target = mogram;
	}

	/**
	 * The var loop operation is as follows: loop min times, then while
	 * condition is true, loop (max - min) times. (UML semantics of the loop operator)
	 * 
	 * @param name
	 * @param min
	 * @param max
	 * @param condition
	 * @param invoker
	 */
	public LoopTask(String name, int min, int max, Condition condition,
                    Mogram invoker) {
		super(name);
		this.min = min;
		this.max = max;
		this.condition = condition;
		target = invoker;
	}

	@Override
	public Task doTask(Transaction txn, Arg... args) throws EvaluationException {
		try {
			// use bound mogram
			if (target instanceof FreeMogram) {
				target = ((FreeMogram)target).getMogram();
			} else if (target instanceof FreeContextion) {
				target = ((FreeContextion)target).getContextion();
			}
			// update the scope of target
			if (target.getScope() == null) {
				target.setScope(scope);
			} else {
				target.getScope().append(scope);
			}

			Context.Return rp = null;
			if (target.getContext() != null) {
				rp = target.getContext().getContextReturn();
			}

			if (condition == null) {
				for (int i = 0; i < max - min; i++) {
					target = target.exert(txn, args);
					if (rp != null && rp.returnPath != null) {
						scope.putValue(target.getName(), target.getContext().getReturnValue());
					}
				}
				return this;
			} else if (condition != null && max - min == 0) {
				if (target instanceof Model) {
					Context cxt = condition.getConditionalContext();
					condition.setConditionalContext((Context) target);
					if (cxt != null && cxt.size() > 0) {
						((Context) target).append(cxt);
					}
				} else if (target instanceof Pipeline) {
					Context cxt = condition.getConditionalContext();
					condition.setConditionalContext(target.getContext());
					if (cxt != null && cxt.size() > 0) {
						target.getContext().append(cxt);
					}
				}
				while (condition.isTrue()) {
					if (target instanceof Routine) {
						Signature sig = ((ServiceMogram)target).getProcessSignature();
						if (sig != null && sig.getVariability() != null) {
							target.getContext().append(condition.getConditionalContext());
						}
						target = target.exert(txn, args);
						if (sig != null && sig.getVariability() != null) {
							((Task) target).updateConditionalContext(condition);
						}
					} else if (target instanceof RequestModel) {
						((RequestModel) target).clearOutputs();
						if (condition.getConditionalContext() == target) {
							target = target.exert(txn, args);
						} else {
							Context response = ((RequestModel) target).getResponse(args);
							condition.getConditionalContext().append(response);
						}
					} else {
						target = target.exert(txn, args);
					}
				}
			} else if (condition != null && max - min > 0) {
				// exert min times
				for (int i = 0; i < min; i++) {
					target = target.exert(txn, args);
				}
				for (int i = 0; i < max - min; i++) {
					target = target.exert(txn);
					if (condition.isTrue())
						target = target.exert(txn, args);
					else
						return this;
				}
			}
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
		return this;
	}
	
	public boolean isConditional() {
		return true;
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

	@Override
	public ServiceContext getDataContext() {
		if (target instanceof Context) {
			return (ServiceContext)target;
		} else {
			return dataContext;
		}
	}

	@Override
	public List<ThrowableTrace> getExceptions(List<ThrowableTrace> exceptions) throws RemoteException {
		exceptions.addAll(((Mogram)target).getExceptions());
		exceptions.addAll(this.getExceptions());
		return exceptions;
	}
	
	public List<Contextion> getMograms(List<Contextion> exs) {
		if (target instanceof Contextion) {
			exs.add(target);
			exs.add(this);
		}
		return exs;
	}

	public List<Contextion> getContextions(List<Contextion> exs) {
		if (target instanceof Contextion) {
			exs.add(target);
			exs.add(this);
		}
		return exs;
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
