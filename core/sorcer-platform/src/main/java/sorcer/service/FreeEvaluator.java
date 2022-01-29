/*
 * Copyright 2019 the original author or authors.
 * Copyright 2019 SorcerSoft.org.
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

import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.ent.Entry;

import java.rmi.RemoteException;

/**
 * A free entry is an instance of Entry that has a name only to be bound at runtime.
 *
 * @see Entry
 *
 * @author Mike Sobolewski
 */
public class FreeEvaluator implements FreeService, Identifiable, Evaluation, Service {

    private String name;

    private Evaluator evaluator;

    private Context scope;

    public FreeEvaluator (String name) {
        this.name = name;
    }

    @Override
    public Object evaluate(Arg... args) throws EvaluationException, RemoteException {
        if (evaluator != null)  {
            return evaluator.evaluate(args);
        } else {
            return null;
        }
    }

    @Override
    public Object asis() throws EvaluationException, RemoteException {
        return evaluator;
    }

    @Override
    public Context.Return getContextReturn() {
        return scope.getContextReturn();
    }

    @Override
    public void setContextReturn(Context.Return contextReturn) {
        scope.setContextReturn(contextReturn);
    }

    @Override
    public void setNegative(boolean negative) {

    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Context getScope() {
        return scope;
    }

    @Override
    public void setScope(Context scope) {
        this.scope = scope;
    }

    @Override
    public void substitute(Arg... entries) throws SetterException, RemoteException {
        ((ServiceContext)scope).substitute(entries);
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public Object getId() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object execute(Arg... args) throws ServiceException, RemoteException {
        if (evaluator != null)  {
            return evaluator.evaluate(args);
        } else {
            return null;
        }
    }

    @Override
    public void bind(Object object) {
        evaluator = (Evaluator) object;
    }
}
