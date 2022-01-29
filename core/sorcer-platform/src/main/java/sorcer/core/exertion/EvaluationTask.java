/*
 * Copyright 2012 the original author or authors.
 * Copyright 2012 SorcerSoft.org.
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
import net.jini.core.transaction.TransactionException;
import sorcer.core.context.model.ent.Function;
import sorcer.core.context.model.ent.Prc;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.core.context.model.req.Srv;
import sorcer.core.signature.EvaluationSignature;
import sorcer.service.*;
import sorcer.service.modeling.Functionality;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * The SORCER evaluation task extending the basic task implementation
 * {@link Task}.
 * 
 * @author Mike Sobolewski
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class EvaluationTask extends Task {

	static final long serialVersionUID = -3710507950682812041L;

	public EvaluationTask(String name) {
		super(name);
	}

	public EvaluationTask(Evaluation evaluator) throws ContextException, RemoteException {
		super(((Identifiable)evaluator).getName());
		EvaluationSignature es = new EvaluationSignature(evaluator);
		if (evaluator instanceof FreeEvaluator) {
			controlContext.getFreeServices().put(((FreeEvaluator) evaluator).getName(), (Service) evaluator);
		}
		addSignature(es);
		es.setEvaluator(evaluator);
		dataContext.setRoutine(this);
		if (es.getEvaluator() instanceof Prc) {
			if (dataContext.getScope() == null)
				dataContext.setScope(new EntryModel(key));
		}
		if (evaluator instanceof Srv) {
			if (dataContext.getContextReturn() == null)
				dataContext.setContextReturn(Signature.SELF_VALUE);
		}
	}

	public EvaluationTask(EvaluationSignature signature) {
		this(null, signature, null);
	}

	public EvaluationTask(String name, EvaluationSignature signature) {
		super(name);
		addSignature(signature);
	}

	public EvaluationTask(EvaluationSignature signature, Context context) {
		this(null, signature, context);
	}

	public EvaluationTask(String name, EvaluationSignature signature,
			Context context) {
		super(name);
		addSignature(signature);
		if (context != null) {
			if (signature.getEvaluator() instanceof Prc) {
				((Prc) signature.getEvaluator()).setScope(context);
			}
			setContext(context);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.service.Task#doTask(net.jini.core.transaction.Transaction)
	 */
	@Override
	public Task doTask(Transaction txn, Arg... args) throws EvaluationException {
		dataContext.getDomainStrategy().setCurrentSelector(getProcessSignature().getSelector());
		dataContext.setCurrentPrefix(getProcessSignature().getPrefix());

		if (((ServiceFidelity)multiFi.getSelect()).getSelects().size() > 1) {
			try {
				return super.doBatchTask(txn);
			} catch (Exception e) {
				e.printStackTrace();
				throw new EvaluationException(e);
			}
		}
		try {
			Evaluation evaluator = ((EvaluationSignature) getProcessSignature())
					.getEvaluator();
			if (evaluator instanceof Function)
				((Function)evaluator).setValid(false);

			if (evaluator instanceof Evaluator) {
				ArgSet vs = ((Evaluator) evaluator).getArgs();
				Object pars = dataContext.getArgs();
				Object val;
				if (args != null && pars instanceof Map) {
					for (Arg v : vs) {
						val = ((Map<String, Object>) pars).get(v.getName());
						if (val != null && (val instanceof Setter)) {
							((Setter) v).setValue(val);
						}
					}
				} else {
					if (vs != null)
						for (Arg v : vs) {
							val = dataContext.getValueEndsWith(v.getName());
							if (val != null && v instanceof Setter) {
								((Setter) v).setValue(val);
							}
						}
				}
			}
//			else {
//				if (evaluator instanceof Prc && dataContext.getScope() != null)
//					((Prc) evaluator).getScope().append(dataContext.getScope());
//			}

			Object result = null;
			// used bound evaluator form FreeEvaluator
			if (evaluator instanceof FreeEvaluator) {
				evaluator = ((FreeEvaluator)evaluator).getEvaluator();
			}
			if (evaluator instanceof Srv) {
				result = handleSrvEntry((Srv)evaluator, args);
			} else if (evaluator instanceof Invocation) {
				result = ((Invocation)evaluator).invoke(dataContext, args);
			} else {
				result = evaluator.evaluate(args);
			}

			if (getProcessSignature().getContextReturn() != null)
				dataContext.setContextReturn(getProcessSignature().getContextReturn());
			dataContext.setReturnValue(result);
			if (evaluator instanceof Scopable && ((Scopable)evaluator).getScope() != null) {
				(((Scopable)evaluator).getScope()).putValue(dataContext.getContextReturn().returnPath, result);
			}
			if (evaluator instanceof Srv && dataContext.getScope() != null)
				dataContext.getScope().putValue(((Identifiable)evaluator).getName(), result);
		} catch (Exception e) {
			e.printStackTrace();
			dataContext.reportException(e);
		}
		dataContext.appendTrace("task" + getName() + " by: " + getEvaluation());
		return this;
	}

	private Object handleSrvEntry(Srv evaluator, Arg... args) throws ServiceException, RemoteException, TransactionException {
		Object out = null;
		Object val = null;

		if (evaluator instanceof Srv) {
			if (isChanged())
				evaluator.setValid(false);
			val = evaluator.asis();
		}

		if (val instanceof ValueCallable && evaluator.getType() == Functionality.Type.LAMBDA) {
			Context.Return rp = evaluator.getReturnPath();
			if (rp != null && rp.inPaths != null) {
				Context cxt = getScope().getDirectionalSubcontext(rp.inPaths);
				out = ((ValueCallable)val).call(cxt);
			} else {
				out = ((ValueCallable) val).call(getScope());
			}
			if (rp != null && rp.returnPath != null)
				putValue((evaluator).getReturnPath().returnPath, out);
		}
		return out;
	}

	public Evaluation getEvaluation() {
		EvaluationSignature es = (EvaluationSignature) getProcessSignature();
		return es.getEvaluator();
	}

}
