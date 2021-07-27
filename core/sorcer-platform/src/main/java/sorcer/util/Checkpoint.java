/*
 * Copyright 2021 the original author or authors.
 * Copyright 2021 SorcerSoft.org.
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
package sorcer.util;

import sorcer.service.*;

import java.rmi.RemoteException;

/**
 * @author Mike Sobolewski
 */
public class Checkpoint extends Condition implements Arg {

    private Integer state = Exec.INITIAL;

    private Integer iteration;

    public Checkpoint() {
        this.state = Exec.INITIAL;
    }

    public Checkpoint(int iteration) {
        this.iteration = iteration;
    }

    public Checkpoint(boolean state) {
        if (state) {
            this.state = Exec.RUNNING;
        } else {
            this.state = Exec.INITIAL;
        }
    }

    public Checkpoint(Context context) {
        conditionalContext = context;
    }

    public Checkpoint(ConditionCallable lambda) {
        this.lambda = lambda;
    }

    public Checkpoint(ConditionCallable closure, String... parameters) {
        this.lambda = closure;
        this.pars = parameters;
    }

    public Checkpoint(Context context, ConditionCallable closure, String... parameters) {
        this.lambda = closure;
        conditionalContext = context;
        this.pars = parameters;
    }

    public boolean isTrue() throws ContextException {
        try {
            if (state == Exec.RUNNING) {
                if (iteration != null && conditionalContext.getValue(Context.CHECKPOINT_ITERATION) != null) {
                    if (iteration.equals(conditionalContext.getValue(Context.CHECKPOINT_ITERATION))) {
                        return true;
                    } else if (lambda != null) {
                        return super.isTrue();
                    }
                }
                return true;
            }
        } catch (RemoteException e) {
            throw new ContextException(e);
        }
        return false;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

}
