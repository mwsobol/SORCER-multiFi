package sorcer.core.context.model;

import sorcer.service.Context;
import sorcer.service.ContextException;

public interface OptimizerState {
        public Context getOptiDesignContext() throws ContextException;

        public Context getConstraintContext() throws ContextException;

        public Context getObjectiveContext() throws ContextException;
}
