package sorcer.util;

import sorcer.service.*;

import java.rmi.RemoteException;

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
