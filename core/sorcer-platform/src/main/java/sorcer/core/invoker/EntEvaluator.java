package sorcer.core.invoker;

import sorcer.core.context.model.ent.Entry;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.service.Arg;
import sorcer.service.ContextException;
import sorcer.service.Evaluator;

import java.util.Set;

abstract public class EntEvaluator implements Evaluator {

    public Set<Entry> getIndependentEnts(Set<Entry> independentVars)
        throws ContextException {
        EntEvaluator e;
        Entry iv = null;
        for (Arg v : getArgs()) {
            if (!(v instanceof Entry)) {
                iv = (( EntryModel )getScope()).getEntry(v.getName());
            } else {
                iv = (Entry) v;
            }
            e = ( EntEvaluator ) iv.getEvaluator();
            if (e.isInependent()) {
                independentVars.add(iv);
            } else {
                e.getIndependentEnts(independentVars);
            }
        }
        return independentVars;
    }

    public boolean isInependent() {
        return false;
    }
}
