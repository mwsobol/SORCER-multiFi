package sorcer.core.plexus;

import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.req.RequestModel;
import sorcer.service.*;
import sorcer.service.modeling.Model;

import java.rmi.RemoteException;
import java.util.List;

public class ContextFidelityManager extends FidelityManager {

    private Context dataContext;

    public ContextFidelityManager(String name) {
        super(name);
    }

    public ContextFidelityManager(Mogram mogram) {
        this(mogram.getName());
        this.mogram = mogram;
    }

    @Override
    public void reconfigure(Fi... fidelities) throws  ConfigurationException {
        if (fidelities == null || fidelities.length == 0) {
            return;
        }

        for (Fi fi : fidelities) {
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
                Morpher cxtMorpher = null;
                if (mogram instanceof Model) {
                    cxtMorpher = dataContext.getMorpher();
                } else {
                    cxtMorpher = mogram.getContext().getMorpher();
                }
                if (cxtMorpher != null) {
                    Fi fi = null;
                    if (mogram instanceof Model) {
                        fi = ((ServiceContext)mogram).getContextProjection();
                    } else {
                        fi = mogram.getContext().getMultiFi();
                    }
                    // based on input output contexts reconfigure context fidelity and morph multiPaths
                    cxtMorpher.morph(this, fi, mogram);
                }
                return;
            } catch (RemoteException | ServiceException | ConfigurationException e) {
                throw new EvaluationException(e);
            }
        }

        // handle multifidelities and context projections by name
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
                        if (mogram instanceof RequestModel) {
                            ((ServiceMogram)dataContext).selectFidelity(cxtFi.getName());
                            ((ServiceContext) mogram).append((Context) dataContext.getMultiFi().getSelect());
                        } else {
                            ((ServiceMogram) mogram).getDataContext().selectFidelity(cxtFi.getName());
                        }
                    } catch (ConfigurationException | ContextException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Projection getContextProjection(String contextName) {
        if (fidelities != null) {
            for (Fidelity prj : fidelities.values()) {
                if (((Projection) prj).getContextFidelity().getName().equals(contextName)) {
                    return (Projection) prj;
                }
            }
        }
        return null;
    }

    public Context getDataContext() {
        return dataContext;
    }

    public void setDataContext(Context dataContext) {
        this.dataContext = dataContext;
    }


}
