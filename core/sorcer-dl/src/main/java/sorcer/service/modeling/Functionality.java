/*
 * Copyright 2009 the original author or authors.
 * Copyright 2009 SorcerSoft.org.
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

package sorcer.service.modeling;

import sorcer.service.*;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * A variable has id, name and execute. Its execute can be evaluated by a related
 * Evaluator.
 *
 * @author Mike Sobolewski
 */
public interface Functionality<T> extends Identifiable, ent<T> {

	/**
	 * Types allow for grouping in the functional model so all variables of a
	 * particular fiType can be retrieved  as a functional collection, e.g.,
	 * DESIGN, RESPONSE, DERIVATIVE or GRADIENT. Selected types can be used to
	 * define the nature of the variable (to the clients) by grouping them in in
	 * a list of types called kinds - addKind(Type). FUNDAMENTAL - if scalar has
	 * meaning to the client
	 */
	public enum Type implements Arg { SIGNATURE,
		INPUT, CONSTANT, PARAMETER, DOMAIN_CONSTANT, INVARIANT, OUTPUT, SHARED, INOUT, RESPONSE, DESIGN, INITIAL_DESIGN, PARAMETRIC,
		LINKED, CONSTRAINT, OBJECTIVE, DERIVATIVE, GRADIENT, RANDOM, BOUNDED, FUNDAMENTAL, RAW, DELEGATION, COMPOSITION,
		MULTIVAL, PRED, DOMAIN_PRED, DEP, DOMAIN_DEP, FILTER, PERSISTER, EVALUATOR, EVALUATION, PRODUCT, WATCHABLE, ENT, PROC, DUAL,
		NEURON, VAR, SRV, LAMBDA, VAL, SUPERVAL, THRESHOLD, BIAS, FUNCTION, DATA, CONTEXT, MFI_CONTEXT, ARRAY, LIST, PIPELINE, ROUTINE, MODEL,
		DOMAIN, NODE, REGION, DISCIPLINE, EXERTION, MOGRAM, SELF, CONDITION, FIDELITY, LOCATOR, ARG, PATH, CONFIG, PROXY, DISPATCH,
		OBJECTIVE_GRADIENT, CONSTRAINT_GRADIENT, COUPLED, MADO, INTENT, TRANS, CONTEXTION, COLLABORATION, SEARCH, GOVERNANCE,
		SUPERVISOR, DEVELOPER, EXECUTION, STRATEGY, EXEC, SCOPE,
        // in Model Pattern
        // All-at-Once (AAO),
		// Simultaneous Analysis and Design (SAND),
		// Individual Discipline Feasible (IDF),
		// Multidiscipline Feasible (MDF)
		// Response, Analysis, Explore
		 AAO, SAND, IDF, MDF, MDA, MRPH, RESP, FNL, ANAL, EXPL, SNAP, NONE,

		// Finite Difference Functionality
		VGD,	// var GradientDifferentiator
		MXVGD,	// var MatrixGradientDifferentiator
		CBDD,	// var double CombinedDifferentiator
		CBMD,	// var matrix CombinedDifferentiator
		GGD, 	// global gradient differentiator for coupled domains
		FDG,   	// finite difference gradient
		MPFD,   // model parallel finite difference gradient
		VPFD;  	// var parallel finite difference

		@Override
		public String getName() {
			return toString();
		}

		@Override
		public Object execute(Arg... args) throws ServiceException, RemoteException {
			return this;
		}
	}

	public enum MathType {
		CONTINUOUS, // ordered, element math defined as usual, continuous design
		// variable
		DISCRETE, // unordered, no element math defined, discrete design
		// variable
		DISCRETE_WITH_ORDER, // ordered, no element math defined, discrete
		// design variable
		DISCRETE_WITH_MATH, // ordered, element math defined, discrete design
		// variable
		DISCRETE_NO_ORDER, // non-ordered, element math undefined, discrete
		// design variable
		PROBLEM_PARAMETER, // ordered, element math defined as usual, continuous
		// problem parameter
		REAL, // ordered, element math defined as usual, continuous, element
		// class is Double, design variable
		LINEAR, // Linear function of the dependent variables

		QUADRATIC // Quadratic function of dependent variables
	}

	public T getValue(Arg... args) throws EvaluationException;

	public Type getType();

	public Class<?> getValueType();

	public ArgSet getArgs();

	public void addArgs(ArgSet set) throws EvaluationException;

	public Object getArg(String varName) throws ContextException;

	public boolean isValueCurrent();

	public void valueChanged(Object obj) throws EvaluationException;

	void valueChanged() throws EvaluationException;
}
