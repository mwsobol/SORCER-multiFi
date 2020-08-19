package sorcer.core.plexus;

import sorcer.service.*;

import java.util.List;

public class ContextFidelityManager extends FidelityManager<Context> {


    public ContextFidelityManager(String name) {
        super(name);
    }

    public ContextFidelityManager(Mogram mogram) {
        this(mogram.getName());
        this.mogram = mogram;
    }

    @Override
    public void morph(String... fiNames) throws EvaluationException {
        Projection prj = null;
        if (fiNames == null || fiNames.length == 0) {
            prj = ((ServiceMogram) mogram).getProjection();
        } else {
            for (String fiName : fiNames) {
                prj = (Projection) fidelities.get(fiName);
                if (prj != null && prj.fiType.equals((Fi.Type.CXT_PRJ))) {
                    Projection inPrj = prj.getInPathProjection();
                    Projection outPrj = prj.getOutPathProjection();
                    Fidelity cxtFi = prj.getContextFidelity();
                    if (inPrj != null) {
                        ((ServiceMogram) mogram).setInPathProjection(inPrj);
                    }
                    if (mogram != null) {
                        ((ServiceMogram) mogram).setInPathProjection(outPrj);
                    }
                    if (cxtFi != null) {
                        try {
                            ((ServiceMogram) mogram).getDataContext().selectFidelity(cxtFi.getName());
                        } catch (ConfigurationException | ContextException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

}
