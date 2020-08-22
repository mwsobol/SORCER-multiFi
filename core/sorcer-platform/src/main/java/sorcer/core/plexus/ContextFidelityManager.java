package sorcer.core.plexus;

import sorcer.core.context.ServiceContext;
import sorcer.service.*;

import java.rmi.RemoteException;
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
    public void reconfigure(Fidelity... fidelities) throws  ConfigurationException {
        if (fidelities == null || fidelities.length == 0) {
            return;
        }

        for (Fidelity fi : fidelities) {
            if (fi.getFiType().equals((Fi.Type.CONTEXT))) {
                try {
                    ((ServiceMogram) mogram).getDataContext().selectFidelity(fi.getName());
                } catch (ContextException e) {
                    throw new ConfigurationException(e);
                }
            } else if (fi.getFiType().equals((Fi.Type.PROJECTION))) {
                if (isTraced) {
                    fiTrace.add(fi);
                }
            }
        }
    }


    @Override
    public void morph(String... fiNames) throws EvaluationException {
        Projection prj = null;
        if (fiNames == null || fiNames.length == 0) {
            try {
                Morpher cxtMorpher = mogram.getContext().getMorpher();
                if (cxtMorpher != null) {
                    cxtMorpher.morph(this, mogram.getContext().getMultiFi(), mogram);
                }
                return;
            } catch (RemoteException | ServiceException | ConfigurationException e) {
                throw new EvaluationException(e);
            }
        }

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
                    ((ServiceMogram) mogram).setOutPathProjection(outPrj);
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
