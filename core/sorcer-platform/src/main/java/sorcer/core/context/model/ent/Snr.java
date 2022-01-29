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
package sorcer.core.context.model.ent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.context.ServiceContext;
import sorcer.core.invoker.Activator;
import sorcer.eo.operator;
import sorcer.service.*;
import sorcer.service.modeling.func;


import java.rmi.RemoteException;
import java.util.Iterator;

/**
 * In service-based modeling, an artificial neuron entry (for short a snr) is a special kind of
 * function, used in a service model {@link EntryModel} to refer to one of the
 * pieces of data provided as input to other neurons.
 * 
 * @author Mike Sobolewski
 */
@SuppressWarnings({"unchecked", "rawtypes" })
public class Snr extends Function<Double> implements Invocation<Double>,
		Setter, Scopable, Comparable<Double>, func<Double> {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(Snr.class.getName());

	protected double bias;

	public Snr(String name) {
		super(name);
        impl = new Activator(name);
		type = Type.NEURON;
	}

       public Snr(String name, double value) {
        this(name);
        out = value;
    }

    public Snr(String name, operator.Args args) {
        this(name);
		((Activator)impl).setArgs(args.argSet());
    }

    public Snr(String name, Context<Float> weights, operator.Args args) {
        this(name, args);
		((Activator)impl).setWeights(weights);
    }

    public Snr(String name, double value, Context<Function> signals) {
        this(name);
        impl = value;
		((Activator)impl).setInvokeContext(signals);
    }

	public Snr(String name, Context<Value> signals, Context<Float> weights, operator.Args args) {
		this(name, args);
		((Activator)impl).setInvokeContext(signals);

	}

	public Snr(String name, Context<Value> signals, Context<Float> weights) {
		this(name);
		((Activator)impl).setInvokeContext(signals);

	}

	public Snr(String name, double value, Context<Value> signals, Context<Float> weights) {
        this(name);
		impl = value;
		((Activator)impl).setInvokeContext(signals);

    }

	public Snr(String name, Context<Float> weights, Arg... args) {
		this(name, new operator.Args(args));
		((Activator)impl).setWeights(weights);
	}

	public Snr(String name, ServiceFidelity fidelities) {
		this(name);
		this.multiFi= fidelities;
	}

    /* (non-Javadoc)
	 * @see sorcer.service.Evaluation#substitute(sorcer.co.tuple.Parameter[])
	 */
	@Override
	public void substitute(Arg... parameters) throws SetterException {
		if (parameters == null)
			return;
		for (Arg p : parameters) {
			try {
				if (p instanceof Snr) {
					if (key.equals(((Snr) p).key)) {
						if (((Snr) p).getScope() != null)
							scope.append(((Snr) p).getScope());

					}
				} else if (p instanceof Fidelity && multiFi != null) {
					try {
						multiFi.selectSelect(p.getName());
					} catch (ConfigurationException e) {
						throw new SetterException(e);
					}
				} else if (p instanceof Context) {
					if (scope == null)
						scope = (Context) p;
					else
						scope.append((Context) p);
				}
			} catch (ContextException e) {
				e.printStackTrace();
				throw new SetterException(e);
			}
		}
	}

	private boolean isFidelityValid(Object fidelity) throws EvaluationException {
		if (fidelity == null || fidelity == Context.none)
			return false;
		if (fidelity instanceof Function) {
			Object obj = null;
			obj = ((Function)fidelity).asis();
			if (obj == null || obj == Context.none) return false;
		}
		 return true;
	}

	public Context getScope() {
		return scope;
	}

	public void setScope(Context scope) {
		if (scope != null) {
            this.scope = scope;
            if (impl != null) {
				((Activator)impl).setInvokeContext(scope);
            }
        }
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Double o) {
		if (o == null)
			throw new NullPointerException();
		if (o instanceof Double)
			return out.compareTo(o);
		else
			return -1;
	}

	/* (non-Javadoc)
	 * @see sorcer.vfe.Variability#getFiType()
	 */
	@Override
	public Type getType() {
		return type;
	}

	@Override
	public Class getValueType() {
		return impl.getClass();
	}

	/* (non-Javadoc)
	 * @see sorcer.vfe.Variability#getArgs()
	 */
	@Override
	public ArgSet getArgs() {
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.vfe.Variability#getArg(java.lang.String)
	 */
	@Override
	public Double getArg(String varName) throws ArgException {
		try {
			return (Double) scope.getValue(varName);
		} catch (ContextException | RemoteException e) {
			throw new ArgException(e);
		}
	}

    @Override
    public Double evaluate(Arg... args) throws EvaluationException {
	    if (((Activator)impl).getArgs().size() > 0) {
            out = ((Activator)impl).activate(args);
        }
        return out;
    }

	@Override
	public boolean isValueCurrent() {
		return false;
	}

	/* (non-Javadoc)
	 * @see sorcer.vfe.Variability#valueChanged(java.lang.Object)
	 */
	@Override
	public void valueChanged(Object obj) throws EvaluationException {
	}

	/* (non-Javadoc)
	 * @see sorcer.vfe.Variability#valueChanged()
	 */
	@Override
	public void valueChanged() throws EvaluationException {		
	}

	/* (non-Javadoc)
	 * @see sorcer.vfe.Persister#isPersistable()
	 */
	@Override
	public boolean isPersistent() {
		return isPersistent;
	}

	public void setPersistent(boolean state) {
		isPersistent = state;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Invocation#invoke(sorcer.service.Context, sorcer.service.Arg[])
	 */
	@Override
    public Double invoke(Context context, Arg... args) throws InvocationException {
        try {
            if (context != null) {
                if (((Activator)impl).getInvokeContext() == null)
					((Activator)impl).setInvokeContext(context);
                else {
					((Activator)impl).getInvokeContext().append(context);
                }
            }
            if (multiFi != null) {
				impl = multiFi.getSelect();
            } else if (((Activator)impl).getArgs().size() == 0) {
                return out;
            }
            ((Activator)impl).setInvokeContext(scope);
            out = ((Activator)impl).activate(args);
            return out;
        } catch (Exception e) {
            throw new InvocationException(e);
        }
    }

	/* (non-Javadoc)
	 * @see sorcer.core.context.model.Variability#addArgs(ArgSet set)
	 */
	@Override
	public void addArgs(ArgSet set) throws EvaluationException {
		Iterator<Arg> i = set.iterator();
		while (i.hasNext()) {
			Snr procEntry = (Snr)i.next();
			try {
				((Activator)impl).getInvokeContext().putValue(procEntry.getName(), procEntry.asis());
			} catch (Exception e) {
				throw new EvaluationException(e);
			} 
		}
		
	}

	@Override
	public int hashCode() {
		int hash = key.length() + 1;
		return hash * 31 + key.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Snr
				&& ((Snr) object).key.equals(key))
			return true;
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Scopable#setScope(java.lang.Object)
	 */
	public void setScope(Object scope) {
		this.scope = (Context)scope;

	}

	@Override
	public Double execute(Arg... args) throws MogramException {
		Context cxt = (Context) Arg.selectDomain(args);
		if (cxt != null) {
			scope = cxt;
			return evaluate(args);
		} else {
			return evaluate(args);
		}
	}

    @Override
	public Double getPerturbedValue(String varName) throws ConfigurationException {
        return (Double)(((ServiceContext)((Activator)impl).getInvokeContext()).get(varName)) + bias;
    }

    @Override
    public double getPerturbation() {
        return bias;
    }
}
