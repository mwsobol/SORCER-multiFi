package sorcer.service;

public class NodeFidelity extends Fidelity<Fidelity> {

    private Fidelity cxtnFi;

    private Fidelity cxtFi;

    public NodeFidelity(String name, Fidelity... fidelities) {
        this(fidelities);
        fiName = name;
    }

    public NodeFidelity(Fidelity... fidelities) {
        for (Fidelity fi : fidelities) {
            assignFi(fi);
        }
    }

    public Fidelity getContextFi() {
        return cxtFi;
    }

    public void setContextFi(Fidelity contextMultiFi) {
        this.cxtFi = contextMultiFi;
    }

    public Fidelity getContextionFi() {
        return cxtnFi;
    }

    public void setContextionFi(Fidelity govFi) {
        this.cxtnFi = govFi;
    }

    public Fidelity getDispatcherFi() {
        return select;
    }

    public void setDispatcherFi(Fidelity dsptFi) {
        this.select = dsptFi;
    }

    private void assignFi(Fidelity fi) {
        if (fi.getFiType().equals(Type.DISPATCHER)) {
            this.select = fi;
        } else if (fi.getFiType().equals(Fi.Type.CONTEXTION)) {
            this.cxtnFi = fi;
        } else if (fi.getFiType().equals(Type.CONTEXT)) {
            this.cxtFi = fi;
        }
    }

}
