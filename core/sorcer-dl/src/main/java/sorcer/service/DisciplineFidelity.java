package sorcer.service;

public class DisciplineFidelity extends Fidelity<Fidelity> {

    private Fidelity cxtnFi;

    private Fidelity cxtFi;

    public DisciplineFidelity(String name, Fidelity... fidelities) {
        this(fidelities);
        fiName = name;
    }

    public DisciplineFidelity(Fidelity... fidelities) {
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
