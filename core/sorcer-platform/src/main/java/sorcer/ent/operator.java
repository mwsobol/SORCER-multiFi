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
package sorcer.ent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.Operator;
import sorcer.co.tuple.*;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.core.context.model.ent.*;
import sorcer.core.context.model.ent.Pcr;
import sorcer.core.context.model.req.Req;
import sorcer.core.invoker.*;
import sorcer.core.plexus.FidelityManager;
import sorcer.core.plexus.MorphFidelity;
import sorcer.core.plexus.MorphMogram;
import sorcer.service.*;
import sorcer.service.ContextDomain;
import sorcer.service.modeling.Model;
import sorcer.eo.operator.Args;
import sorcer.service.modeling.SupportComponent;
import sorcer.service.modeling.Functionality;
import sorcer.util.Checkpoint;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static sorcer.eo.operator.context;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class operator extends Operator {

	private static final Logger logger = LoggerFactory.getLogger(operator.class.getName());

	public static Snr snr(String path, double signal) {
		return new Snr(path, signal);
	}

    public static Snr snr(String path, Args signals) {
        return new Snr(path, signals);
    }

    public static Snr snr(String path, Context<Float> weights, Args signals) {
        return new Snr(path, weights, signals);
    }

    public static Snr snr(String path, Context<Float> weights, double signal, Args signals) {
        return new Snr(path, weights, signals);
    }

    public static Snr snr(String path, ServiceFidelity fidelities) {
		return new Snr(path, fidelities);
	}

    public static Entry th(String path, double threshold) {
        Entry e = new Entry(path, threshold);
	    e.setType(Functionality.Type.THRESHOLD);
        return e;
    }

    public static Entry bias(String path, double bias) {
        Entry e = new Entry(path, bias);
        e.setType(Functionality.Type.BIAS);
        return e;
    }

    public static <T> Pcr<T> pcr(String path, T argument) throws EvaluationException, RemoteException {
		return new Pcr(path, argument);
	}

	public static Pcr dbEnt(String path, Object argument) throws EvaluationException, RemoteException {
		Pcr p = new Pcr(path, argument);
		p.setPersistent(true);
		p.evaluate();
		return p;
	}

	public static Pcr pcr(Identifiable identifiable, Context context) throws EvaluationException, RemoteException {
		Pcr p = new Pcr(identifiable.getName(), identifiable);
		if (identifiable instanceof Scopable) {
			((Scopable) identifiable).setScope(context);
		}
		p.setType(Functionality.Type.PCR);
		return p;
	}

	public static Pcr pcr(Contextion argument, String name, String path) {
		Pcr p = new Pcr(argument, name, path);
		return p;
	}

	public static Pcr pcr(String path, Object argument, Object object) throws ContextException, RemoteException {
		Pcr p = null;
		if (object instanceof Context) {
			p = new Pcr(path, argument);
			p.setScope(object);
		} else if (object instanceof Entry) {
			p = new Pcr(path, argument);
			p.setScope(context((Entry)object));
		} else if (object instanceof Args) {
			p = new Pcr(path, argument);
			p.setScope(context((Args)object));
		} else if (object instanceof Service) {
			p = new Pcr(path, argument, object);
		}
		return p;
	}

	public static ServiceInvoker func(ServiceInvoker invoker) {
		invoker.setFunctional(true);
		return invoker;
	}

	public static Req req(ServiceFidelity fidelity) {
		Req service = new Req(fidelity.getName(), fidelity);
		return service;
	}

	public static Req req(String name, ServiceFidelity fidelity) {
		Req service = new Req(name, fidelity);
		return service;
	}

	public static Req req(String name, MorphFidelity fidelity) {
		fidelity.setPath(name);
		fidelity.getFidelity().setPath(name);
		fidelity.getFidelity().setName(name);
		Req service = new Req(name, fidelity);
		return service;
	}

	public static Req req(String name, Identifiable item) {
		return req(name,  item,  null);
	}

	public static Req req(Identifiable item, Context context) {
		return req(null,  item,  context);
	}

	public static Req ent(Signature sig) {
		return req(sig);
	}

	public static Signatory call(Signature signature) {
		return new Signatory(signature.getName(), signature);
	}
	public static Signatory call(Signature signature, Context context) {
		return new Signatory(signature.getName(), signature, context);
	}

	public static Req req(String name, Identifiable item, Context context, Arg... args) {
		String srvName = item.getName();
		Req req = null;
		if (name != null) {
            srvName = name;
        }
		if (item instanceof Signature) {
			req = new Req(srvName,
					new Signatory(item.getName(), (Signature) item, context));
		} else if (item instanceof Mogram) {
			req = new Req(srvName,
					new MogramEntry(item.getName(), (Mogram) item));
		} else {
			req = new Req(srvName, item);
		}
		try {
			req.substitute(args);
		} catch (SetterException e) {
			e.printStackTrace();
		}
		return req;
	}

	public static Req req(Identifiable item) {
		return req(null, item);
	}

	public static Req req(String name, String path, Model model) {
		return new Req(path, model, name);
	}

	public static Req req(String name, String path, Model model, Functionality.Type type) {
		return new Req(path, model, name, type);
	}

	public static Req aka(String name, String path) {
		return new Req(name, null, path);
	}
	public static Req alias(String name, String path) {
		return new Req(path, null, name);
	}

	public static Pcr dPar(Identifiable identifiable, Context context) throws EvaluationException, RemoteException {
		Pcr p = new Pcr(identifiable.getName(), identifiable);
		p.setPersistent(true);
		p.setScope(context);
		return p;
	}

	public static Pcr dbEnt(String path, Object argument, Context context) throws EvaluationException, RemoteException {
		Pcr p = new Pcr(path, argument);
		p.setPersistent(true);
		p.setScope(context);
		return p;
	}

	public static Pcr pipe(Contextion in, String name, String path, Service out) throws ContextException {
		Pcr p = new Pcr(name, path, out);
		add(p, in);
		return p;
	}

	public static Pcr storeUrl(Pcr callEntry, URL url) {
		callEntry.setDbURL(url);
		return callEntry;
	}

	public static Pcr pcr(EntryModel pm, String name) throws ContextException, RemoteException {
		Pcr parameter = new Pcr(name, pm.asis(name));
		parameter.setScope(pm);
		return parameter;
	}

	public static Invocation invoker(Context mappable, String path)
			throws ContextException {
		Object obj = mappable.asis(path);
		while (obj instanceof Contextion || obj instanceof Pcr) {
			try {
				obj = ((Evaluation) obj).asis();
			} catch (RemoteException e) {
				throw new ContextException(e);
			}
		}
		if (obj instanceof Invocation)
			return (Invocation) obj;
		else if (obj != null) {
			if (obj instanceof Double)
				return new DoubleIncrementor(path, null, (Double) obj);
			if (obj instanceof Integer)
				return new IntegerIncrementor(path, null, (Integer) obj);
		}
		throw new NoneException("No such invoker at: " + path + " in: " + mappable.getName());
	}

	public static void clearPars(Object invoker) throws EvaluationException {
		if (invoker instanceof ServiceInvoker)
			((ServiceInvoker)invoker).clearPars();
	}

	public static EntryModel append(EntryModel parContext, Arg... objects)
			throws RemoteException, ContextException {
		parContext.append(objects);
		return parContext;
	}

//	public static Proc put(EntryModel entModel, String name, Object getValue) throws ContextException, RemoteException {
//		entModel.putValue(name, getValue);
//		entModel.setContextChanged(true);
//		return pcr(entModel, name);
//	}
//
//	public static EntryModel put(EntryModel entModel, Entry... entries) throws ContextException {
//		for (Entry e : entries) {
//			entModel.putValue(e.getName(), e.getImpl());
//		}
//		entModel.setContextChanged(true);
//		return entModel;
//	}

	public static Pcr add(Pcr callEntry, Object to)
			throws ContextException {
		if (to instanceof Routine) {
			((Subroutine)to).addPersister(callEntry);
			return callEntry;
		}
		return callEntry;
	}

	public static Pcr connect(Object to, Pcr callEntry)
			throws ContextException {
		return add(callEntry, to);
	}

	public static Pcr pcr(Object object) throws EvaluationException, RemoteException {
		if (object instanceof String)
			return new Pcr((String)object);
		else if (object instanceof Identifiable)
			return new Pcr(((Identifiable) object).getName(), object);
		return null;
	}

	public static Pcr pcr() {
		GroovyInvoker gi = new GroovyInvoker();
		return new Pcr(gi.getName(), gi);
	}

    public static Pipeline pl(String name, Context context, Opservice... pservices) {
        Pipeline pl =  new Pipeline(pservices);
        pl.setInvokeContext(context);
        pl.setName(name);
        return pl;
    }

    public static Pipeline pl(Context context, Opservice... pservices) {
        Pipeline pl =  new Pipeline(pservices);
        pl.setInvokeContext(context);
        return pl;
    }

    public static Pipeline pl(String name, Opservice... pservices) {
        Pipeline pl =  new Pipeline(pservices);
        pl.setName(name);
        return pl;
    }

    public static Pipeline pl(Opservice... pservices) {
        return new Pipeline(pservices);
    }

	public static Pipeline n2(String name, Context data, Opservice... pservices) {
		return new Pipeline(name, data, pservices);
	}

	public static <T> Evaluator<T>  mfEval(Evaluator<T>... evaluators) {
		return (Evaluator<T>) new MultiFiEvaluator(evaluators);
	}

    public static Invoker appendScope(Context context) {
		Appender inv = new Appender();
		inv.setScope(context);
		inv.setType(Appender.ContextType.SCOPE);
		inv.setNew(false);
        return inv;
    }

	public static Invoker newScope(Context context) {
		Appender inv = new Appender();
		inv.setScope(context);
		inv.setType(Appender.ContextType.SCOPE);
		inv.setNew(true);
		return inv;
	}

	public static Invoker appendInput(Context context) {
		Appender inv = new Appender();
		inv.setDataContext(context);
		inv.setType(Appender.ContextType.INPUT);
		inv.setNew(false);
		return inv;
	}

	public static Invoker newInput(Context context) {
		Appender inv = new Appender();
		inv.setDataContext(context);
		inv.setType(Appender.ContextType.INPUT);
		inv.setNew(true);
		return inv;
	}

    public static Pcr mfpcr(Evaluation... evaluators) {
		MultiFiEvaluator mfEval =  new MultiFiEvaluator(evaluators);
		// set default fidelity to the first evaluation
		return new Pcr(((Identifiable)evaluators[0]).getName(), mfEval);
	}

    public static Pcr pcr(String expression, Arg... parameters) {
        return pcr(null, expression, null, parameters);
    }

	public static Pcr pcr(String expression, Context context, Arg... parameters) {
		return pcr(null, expression, context, parameters);
	}

	public static Pcr pcr(String path, String expression, Context context, Arg... parameters) {
		GroovyInvoker gi = new GroovyInvoker(expression, parameters);
		if (context != null) {
            gi.setInvokeContext(context);
        }
		String name = path;
		if (path == null) {
			name = gi.getName();
		}
		return new Pcr(name, gi);
	}

	public static Pcr pcr(Invocation invoker) {
		return new Pcr(((ServiceInvoker)invoker).getName(), invoker);
	}

	public static Pcr pcr(String path, Evaluator invoker) {
		return new Pcr(path, invoker);
	}

	public static Object invoke(Evaluator invoker, Arg... parameters)
			throws ContextException, RemoteException {
		return ((Invocation)invoker).invoke(null, parameters);
	}

	public static Object invoke(Invocation invoker, Context context, Arg... parameters)
			throws ContextException, RemoteException {
		return invoker.invoke(context, parameters);
	}

	public static Evaluator invalid(Evaluator evaluator) {
		evaluator.setValid(false);
		return evaluator;
	}

	public static MultiFiSlot invalid(MultiFiSlot slot) {
		slot.setValid(false);
		return slot;
	}

    public static Object activate(Model model, String path, Arg... args) throws InvocationException {
        try {
			Snr ane = (Snr) ((ServiceMogram)model).get(path);
            if (ane.getMultiFi() != null) {
                List<Fi> fiList = Arg.selectFidelities(args);
                ((FidelityManager) ((ServiceMogram)model).getFidelityManager()).reconfigure(fiList);
                return invoke((EntryModel) model, path, args);

            } else {
                return invoke((EntryModel) model, path, args);
            }
        } catch (RemoteException | ContextException | ConfigurationException e) {
            throw new InvocationException(e);
        }
    }

	public static Object invoke(EntryModel procModel, String parname, Arg... parameters)
			throws RemoteException, InvocationException {
		try {
			Object obj;
			if (parameters.length > 0 && parameters[0] instanceof Agent) {
				obj = parameters[0];
			} else {
				obj = procModel.get(parname);
			}

			Context scope = null;
			// assume that the first argument is always context if provided
			if (parameters.length > 0 && parameters[0] instanceof Context) {
				scope = (Context) parameters[0];
			}
			if (obj instanceof Agent) {
				return ((Agent)obj).evaluate(parameters);
			} else if (obj instanceof Pcr
					&& (( Pcr ) obj).asis() instanceof Invocation) {
				Invocation invoker = (Invocation) (( Pcr ) obj).asis();
				//return invoker.invoke(entModel, parameters);
				if (scope != null)
					return invoker.invoke(scope, parameters);
				else
					return invoker.invoke(procModel, parameters);
			} else if (obj instanceof Invocation) {
				Object out;
				if (scope != null)
					out = ((Invocation) obj).invoke(scope, parameters);
				else
					out = ((Invocation) obj).invoke(null, parameters);
				return out;
			} else {
				throw new InvocationException("No invoker for: " + parname);
			}
		} catch (ContextException e) {
			throw new InvocationException(e);
		}
	}

	public static ArgSet args(ServiceInvoker invoker) {
		return invoker.getArgs();
	}

	public static Arg[] args(EntryModel pm, String... parnames)
			throws ContextException {
		ArgSet ps = new ArgSet();
		for (String name : parnames) {
			ps.add(pm.getCall(name));
		}
		return ps.toArray();
	}

	public static <T> Evaluator<T> invoker(Evaluator evaluator, ArgSet pars) {
		return new ServiceInvoker(evaluator,pars);
	}

	public static <T> Evaluator<T> invoker(ValueCallable<T> lambda, Args args) throws InvocationException {
		return new ServiceInvoker(null, lambda, null, args.argSet());
	}

	public static <T> Evaluator<T> invoker(ValueCallable<T> lambda, Context scope, Args args) throws InvocationException {
		try {
			return new ServiceInvoker(null, lambda, scope, args.argSet());
		} catch (Exception e) {
			throw new InvocationException("Failed to create invoker!", e);
		}
	}

	public static <T> Evaluator<T> invoker(String name, ValueCallable<T> lambda) throws InvocationException {
		return new ServiceInvoker(name, lambda, null, null);
	}

	public static <T> Evaluator<T> invoker(String name, ValueCallable<T> lambda, Args args) throws InvocationException {
		return new ServiceInvoker(name, lambda, args.argSet());
	}

	public static <T> Evaluator invoker(String name, ValueCallable<T> lambda, Context scope, Args args) throws InvocationException {
		return new ServiceInvoker(name, lambda, scope, args.argSet());
	}

	public static ServiceInvoker invoker(String name, String expression, Args args) {
		return new GroovyInvoker(name, expression, args.argSet());
	}

	public static <T> Evaluator<T> invoker(String name, String expression, Context scope, Args args) throws ContextException {
		GroovyInvoker<T> invoker = new GroovyInvoker(name, expression, args.argSet());
		invoker.setInvokeContext(scope);
		return invoker;
	}

	public static Evaluator expr(String expression, Context scope,  Args args) throws ContextException {
		return invoker(expression, scope, args);
	}

	public static ServiceInvoker invoker(String expression, Context scope, Args args) throws ContextException {
		GroovyInvoker invoker = new GroovyInvoker(expression, args.argSet());
		invoker.setInvokeContext(scope);
		return invoker;
	}

	public static ServiceInvoker expr(String expression) {
		return new GroovyInvoker(expression);
	}

	public static ServiceInvoker expr(String name, String expression) {
		return new GroovyInvoker(name, expression);
	}

	public static ServiceInvoker expr(String expression, Args args) {
		return 	invoker(expression, args);
		}

	public static ServiceInvoker gvy(String expression, Args args) {
		return new GroovyInvoker(expression, args.args());
	}
	public static ServiceInvoker invoker(String expression, Args args) {
		return new GroovyInvoker(expression, args.args());
	}

	public static ServiceInvoker invoker(String expression, Arg... args) {
		return new GroovyInvoker(expression, args);
	}

	public static ServiceInvoker invoker(String name, String expression, Arg... args) {
		GroovyInvoker gi = new GroovyInvoker(expression, args);
		gi.setName(name);
		return gi;
	}

    public static SysCall sysCall(String name, Context context) throws ContextException {
        return new SysCall(name, context);
    }

	public static ServiceInvoker print(String path) {
		return new GroovyInvoker("_print_", new Path(path));
	}

	public static ServiceInvoker invoker(String expression) {
		return new GroovyInvoker(expression);
	}

	public static ServiceInvoker invoker(Routine exertion) {
        return new RoutineInvoker(exertion);
    }

    public static ServiceInvoker invoker(Args args) {
        return new CmdInvoker(args.getNameArray());
    }
    public static IncrementInvoker inc(String path) {
		return new IntegerIncrementor(path, 1);
	}

	public static IncrementInvoker inc(String path, int increment) {
		return new IntegerIncrementor(path, increment);
	}

	public static IncrementInvoker inc(Invocation invoker, int increment) {
		if (invoker instanceof IntegerIncrementor) {
			((IntegerIncrementor) invoker).setIncrement(increment);
			return (IntegerIncrementor) invoker;
		} else {
			return new IntegerIncrementor(invoker, increment);
		}
	}

	public static IncrementInvoker inc(Invocation<Integer> invoker) {
		return new IntegerIncrementor(invoker, 1);
	}

	public static IncrementInvoker dinc(String path) {
		return new DoubleIncrementor(path, 1.0);
	}

	public static IncrementInvoker inc(String path, double increment) {
		return new DoubleIncrementor(path, increment);
	}


	public static IncrementInvoker inc(Invocation invoker, double increment) {
		if (invoker instanceof IntegerIncrementor) {
			((DoubleIncrementor) invoker).setIncrement(increment);
			return (DoubleIncrementor) invoker;
		} else {
			return new DoubleIncrementor(invoker, increment);
		}
	}

	public static IncrementInvoker dinc(Invocation<Double> invoker) {
		return new DoubleIncrementor(invoker, 1.0);
	}

	public static sorcer.service.Incrementor reset(sorcer.service.Incrementor incrementor) {
		incrementor.reset();
		return incrementor;
	}

	public static <T> T next(Incrementor<T> incrementor) {
		return incrementor.next();
	}

	public static <T> T next(EntryModel model, String name) throws ContextException {
		Incrementor<T> inceremntor = (Incrementor<T>)invoker(model, name);
		return inceremntor.next();
	}

	public static MethodInvoker methodInvoker(String selector, Object methodObject, Args... args) {
		return methodInvoker(selector, methodObject, null, args);
	}

	public static MethodInvoker methodInvoker(String selector, Object methodObject,
                                              ContextDomain context, Args... args) {
		MethodInvoker mi = new MethodInvoker(selector, methodObject, selector, args);
		ContextDomain cxt = context;
		if (context == null) {
			cxt = new ServiceContext();
		}
		mi.setContext(cxt);
		mi.setParameterTypes(new Class[]{ Context.class });
		return mi;
	}

	public static RoutineInvoker exertInvoker(String name, Routine exertion, String path, Pcr... callEntries) {
		return new RoutineInvoker(name, exertion, path, callEntries);
	}

	public static RoutineInvoker exertInvoker(Routine exertion, String path, Pcr... callEntries) {
		return new RoutineInvoker(exertion, path, callEntries);
	}

	public static RoutineInvoker exertInvoker(Routine exertion, Pcr... callEntries) {
		return new RoutineInvoker(exertion, callEntries);
	}

	public static CmdInvoker cmdInvoker(String name, String cmd, Pcr... callEntries) {
		return new CmdInvoker(name, cmd, callEntries);
	}

	public static RunnableInvoker runnableInvoker(String name, Runnable runnable, Pcr... callEntries) {
		return new RunnableInvoker(name, runnable, callEntries);
	}

	public static CallableInvoker callableInvoker(String name, Callable callable, Pcr... callEntries) {
		return new CallableInvoker(name, callable, callEntries);
	}

	public static <T> OptInvoker<T> opt(T value) {
		return new OptInvoker(value);
	}

	public static OptInvoker opt(Condition condition, ServiceInvoker target) {
		return new OptInvoker(null, condition, target);
	}

	public static OptInvoker opt(String name, ServiceInvoker target) {
		return new OptInvoker(name, target);
	}

	public static OptInvoker opt(String name, Condition condition, ServiceInvoker target) {
		return new OptInvoker(name, condition, target);
	}

	public static AltInvoker alt(Evaluator...  invokers) {
		return new AltInvoker(null, invokers);
	}

	public static AltInvoker alt(String name, Evaluator...  invokers) {
		return new AltInvoker(name, invokers);
	}

	public static LoopInvoker loop(Condition condition, Evaluator target) {
		return new LoopInvoker(null, condition, (Invocation) target);
	}

	public static LoopInvoker loop(Condition condition, Invocation target, Context context) throws ContextException {
		LoopInvoker invoker = new LoopInvoker(null, condition, target);
		invoker.setInvokeContext(context);
		return invoker;
	}

	public static LoopInvoker loop(String name, Condition condition, Invocation target) {
		return new LoopInvoker(name, condition, target);
	}

	public static LoopInvoker loop(String name, Condition condition, Pcr target)
			throws EvaluationException, RemoteException {
		return new LoopInvoker(name, condition, (ServiceInvoker) target.asis());
	}

	public static OptInvoker get(AltInvoker invoker, int index) {
		return invoker.getInvoker(index);
	}

	public static Agent agent(String name, String classNme, URL agentJar)
			throws EvaluationException, RemoteException {
		return new Agent(name, classNme, agentJar);
	}

	public static ExecPath invoker(String name, ServiceInvoker invoker) {
		return new ExecPath(name, invoker);
	}

	public static ContextDomain scope(Pcr callEntry) {
		return callEntry.getScope();
	}

	public static Context invokeScope(Pcr callEntry) throws EvaluationException,
			RemoteException {
		Object obj = callEntry.asis();
		if (obj instanceof ServiceInvoker)
			return ((ServiceInvoker) obj).getInvokeContext();
		else
			return null;
	}

	public static Function pcr(Model model, String path) throws ContextException {
		try {
			return new Function(path, model.asis(path));
		} catch (RemoteException e) {
			throw new ContextException(e);
		}
	}

	public static <T extends Service> Req ent(String name, MorphFidelity fidelity) {
        fidelity.setPath(name);
        fidelity.getFidelity().setPath(name);
		fidelity.getFidelity().setName(name);
		for (Object item : fidelity.getSelects()) {
			if (item instanceof Fidelity) {
				((Fidelity) item).setPath(name);
				if (((Fidelity)item).getName() == null) {
					((Fidelity)item).setName(name);
				}
				for (Object obj : ((Fidelity) item).getSelects()) {
					if (obj instanceof Fi && ((Fidelity) obj).getName() == null) {
						((Fidelity) obj).setPath(name);
						((Fidelity) obj).setName(((Identifiable) obj).getName());
					}
				}
			}
		}
        return req(name, fidelity);
    }

	public static Req ent(String name, ServiceFidelity fidelity) {
        return req(name, fidelity);
    }

	public static Req ent(ServiceFidelity fidelity) {
        return req(fidelity);
    }

	public static FreeEntry ent(String path) {
		return new FreeEntry(path);
	}

	public static Entry ent(Path path, Object value, Arg... args) {
		Entry entry = ent(path.getName(), value, args);
		entry.annotation(path.info.toString());
		return entry;
	}

	public static Entry cached(Entry ent) {
		ent.setCached(true);
		return ent;
	}

	public static <T> Ref<T> ref(SupportComponent component) {
		Ref cr = new Ref();
		cr.setOut(component);
		return cr;
	}

	public static <T> Ref<T> ref(String path, Arg... args) {
		Ref cr = new Ref(path, args);
		return cr;
	}

	public static Entry ent(Context context, String name) {
		return new Entry(name, ((ServiceContext)context).get(name));
	}

	public static Entry ent(Identifiable object) {
		return new Entry(object.getName(), object);
	}

    public static Entry ent(Fidelity<Path> multiFipath, Object value, Arg... args) {
	    Entry mpe = ent(multiFipath.getSelect().getName(), value, args);
	    mpe.setMultiFiPath(multiFipath);
		multiFipath.setName(multiFipath.getSelect().path);
		multiFipath.setPath(mpe.getName());
		return mpe;
    }

    public static Entry ent(String path, Object value, Arg... args) {
		Entry entry = null;
		if (value instanceof List) {
			if  (((List)value).get(0) instanceof Path) {
				entry = new ExecDependency(path, (List) value);
			} else {
				entry = new Value(path, value);
			}
		} else if (value instanceof  Number || value instanceof  String || value instanceof  Date
				|| value instanceof Map || value.getClass().isArray()) {
			return new Value(path, value);
		} else if (value instanceof Context && args != null && args.length > 0) {
			return new Snr(path, (Context)value, new Args(args));
		} else if (value instanceof Signature) {
			Mogram mog = Arg.selectMogram(args);
			Context cxt = null;
			if (mog instanceof Context) {
				cxt = (Context)mog;
			}
			if (cxt != null) {
				entry = req(path, (Identifiable) value, cxt, args);
			} else {
				entry = req(path, (Identifiable) value, null, args);
			}
			entry.setType(Functionality.Type.SRV);
		} else if (value instanceof Fidelity) {
			if (((Fi)value).getFiType() == Fi.Type.VAL) {
				entry = new Value(path, value);
			} else if (((Fi)value).getFiType() == Fi.Type.PROC) {
				entry = new Pcr(path, value);
			} else if (((Fi)value).getFiType() == Fi.Type.ENTRY) {
                ((Fidelity)value).setName(path);
				entry = new Entry(path, value);
			} else if (((Fi)value).getFiType() == Fi.Type.SRV) {
				entry = new Req(path, value);
			}
		} else if (value instanceof MorphMogram) {
			try {
				(( MorphMogram )value).setUnifiedName(path);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			entry = new Req(path, value);
		} else if (value instanceof ServiceMogram) {
			entry = new MogramEntry(path, (Mogram) value);
			entry.setType(Functionality.Type.MOGRAM);
		} else if (value instanceof Tuple) {
			entry = new Value(path, value);
			entry.setType(Functionality.Type.VAL);
		}else if (value instanceof Service) {
			entry = new Pcr(path, value);
			entry.setType(Functionality.Type.PCR);
		} else {
			entry = new Entry(path, value);
		}

		Context cxt = null;
		Checkpoint ckpt = null;
		for (Arg arg : args) {
			if (arg instanceof Context) {
				cxt = (Context) Arg.selectDomain(args);
			} else if (arg instanceof Checkpoint)
			ckpt = (Checkpoint) Arg.selectCheckpoint(args);
		}
		try {
			// special cases of procedural attachment
			if (entry instanceof Pcr) {
				if (cxt != null) {
					if (value instanceof ServiceInvoker) {
						((ServiceInvoker)value).setInvokeContext(cxt);
					} else {
						entry.setScope(cxt);
					}
				} else if (args.length == 1 && args[0] instanceof Function) {
					entry.setScope(context(args[0]));
				} else if (args.length == 1 && args[0] instanceof Service) {
					entry = new Pcr(path, value, args[0]);
				}
			}
		} catch (ContextException e) {
			e.printStackTrace();
		}
		if (ckpt != null) {
			ckpt.setName(path);
			entry.setCheckpoint(ckpt);
		}
		entry.setValid(true);
		return entry;
	}

	public static Req srv(Signature sig) {
		return req(sig);
	}

	public static Slot<Fi, Service> slot(Fi selectFi, Service service) throws ConfigurationException {
		Slot<Fi, Service> assoc = new Slot(selectFi, service);
		if (service instanceof Fidelity) {
			Fidelity fi = (Fidelity)service;
			if (!fi.isValid()) {
				String msg = "Misconfigured entry fidelity: " + service + " for: " + selectFi;
				logger.warn(msg);
			}

			if (fi.getFiType().equals(Fi.Type.GRADIENT)) {
				// if no contextReturn set use its key - no multifidelities
				if (selectFi.getPath().equals("")) {
					selectFi.setPath(selectFi.getName());
				}
				// use a select gradient key if declared
				if (selectFi.getSelect() == null) {
					if (fi.getSelect() != null) {
						selectFi.setSelect(fi.getSelect());
					} else {
						selectFi.setSelect(selectFi.getName());
						fi.setSelect(selectFi.getName());
					}
				}
			}

			fi.setName(selectFi.getName());
			fi.setPath(selectFi.getPath());
			((Fidelity)selectFi).setType(fi.getFiType());
		}

		return assoc;
	}

	public static Function pcr(String path) {
		return new Function(path, null);
	}

	public static <T> TagEntry<T> ent(String path, T value, String association) {
		return new TagEntry(path, value, association);
	}

	public static Arg[] ents(String... entries)
			throws ContextException {
		ArgSet as = new ArgSet();
		for (String name : entries) {
			as.add(new Function(name, Context.none));
		}
		return as.toArray();
	}

	public static Arg[] ents(Function... entries)
			throws ContextException {
		ArgSet as = new ArgSet();
		for (Function e : entries) {
			as.add(e);
		}
		return as.toArray();
	}

	public static Function inout(Function entry) {
		entry.setType(Functionality.Type.INOUT);
		return entry;
	}

	public static InputValue inoutVal(String path) {
		return new InputValue(path, null, 0);
	}

	public static <T> InoutValue<T> inoutVal(String path, T value) {
		return new InoutValue(path, value, 0);
	}

	public static <T> InoutValue<T> inoutVal(String path, T value, int index) {
		return new InoutValue(path, value, index);
	}

	public static <T> InoutValue<T> inoutVal(String path, T value, String annotation) {
		InoutValue<T> ie = inoutVal(path, value);
		ie.annotation(annotation);
		return ie;
	}


    public static Req srv(String path, Args args) {
        Req req = new Req(path, path);
        req.setType(Functionality.Type.LAMBDA);
        return req;
    }


    public static Req srv(String path, Service service, Args args) {
		Req req = new Req(path, path, service, args.getNameArray());
		req.setType(Functionality.Type.LAMBDA);
		return req;
	}

	public static Req srv(String path, Service service, String name, Args args) {
		Req req = new Req(name, path, service,  args.getNameArray());
		req.setType(Functionality.Type.LAMBDA);
		return req;
	}

	public static Req srv(String name, String path, Client client) {
		Req req = new Req(name, path, client);
		req.setType(Functionality.Type.LAMBDA);
		return req;
	}

	public static <T> Req srv(String path, Callable<T> call) {
		Req req = new Req(path, call);
		req.setType(Functionality.Type.LAMBDA);
		return req;
	}

	public static <T> Req srv(String path, ValueCallable<T> call) {
		Req req = new Req(path, call);
		req.setType(Functionality.Type.LAMBDA);
		return req;
	}

	public static <T> Req srv(String path, ValueCallable<T> call, Args args) {
		Req req = new Req(path, call, args.getNameArray());
		req.setType(Functionality.Type.LAMBDA);
		return req;
	}

	public static <T> Req srv(String path, ValueCallable<T> lambda, Context context, Args args)
			throws InvocationException {
		Req req = new Req(path, invoker(lambda, context, args));
		req.setType(Functionality.Type.LAMBDA);
		return req;
	}

    public static <T> Req srv(Fidelity<Path> multiFipath, EntryCollable call) {
		Req req = new Req(multiFipath.getSelect().getName(), call);
        req.setMultiFiPath(multiFipath);
		multiFipath.setName(multiFipath.getSelect().path);
		multiFipath.setPath(req.getName());
		req.setType(Functionality.Type.LAMBDA);
        req.setMultiFiPath(multiFipath);
        return req;
    }

	public static <T> Req srv(String path, EntryCollable call) {
		Req req = new Req(path, call);
		req.setType(Functionality.Type.LAMBDA);
		return req;
	}

	public static <T> Req srv(String path, ValueCallable<T> call, Context.Return returnPath) {
		Req req = new Req(path, call, returnPath);
		req.setType(Functionality.Type.LAMBDA);
		return req;
	}
}
