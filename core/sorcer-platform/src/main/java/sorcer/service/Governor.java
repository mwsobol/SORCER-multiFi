package sorcer.service;

import sorcer.co.tuple.ExecDependency;
import sorcer.core.context.ModelStrategy;
import sorcer.core.context.ServiceContext;
import sorcer.core.service.Governance;
import sorcer.service.modeling.*;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import static sorcer.co.operator.path;

public class Governor implements Service, Supervision {

    protected Governance governance;

    // exec discipline dependencies
    public Governor() {
        // do nothing
    }

    public Governor(Governance governance) {
        this.governance = governance;
    }

    @Override
    public Object execute(Arg... args) throws ServiceException {
        try {
            List<Fidelity> fis = Arg.selectFidelities(args);
            if (fis != null && fis.size() > 0) {
                ((ServiceFidelity)governance.getMultiFi()).selectSelect(fis.get(0).getName());
            }
            Analysis analyzer = null;
            if (governance.getAnalyzerFi() != null) {
                analyzer = governance.getAnalyzerFi().getSelect();
            }
            if (governance.getInput() != null && governance.getInConnector() != null) {
                ((ServiceContext) governance.getInput()).updateContextWith(governance.getInConnector());
            }
            execDependencies(governance.getName(), args);
            if (analyzer != null) {
                governance.getInput().putValue(Functionality.Type.DISCIPLINE.toString(), governance.getName());
                analyzer.analyze(governance, governance.getInput());
            }
            return governance.getOutput();
        } catch (ConfigurationException e) {
            throw new ServiceException(e);
        }
    }

    public void execDependencies(String path, Arg... args) throws ContextException {
        Map<String, List<ExecDependency>> dpm = ((ModelStrategy) governance.getMogramStrategy()).getDependentDomains();
        if (dpm != null && dpm.get(path) != null) {
            List<ExecDependency> del = dpm.get(path);
            if (del != null && del.size() > 0) {
                for (ExecDependency de : del) {
                    List<Path> dpl = (List<Path>) de.getImpl();
                    if (dpl != null && dpl.size() > 0) {
                        for (Path p : dpl) {
                            try {
                                Discipline disc = governance.getDiscipline(p.path);
                                disc.evaluate(governance.getOutput(), args);
                                if (governance.getAnalyzerFi() != null && governance.getAnalyzerFi().getSelect() != null) {
                                    disc.getOutput().putValue(Functionality.Type.DISCIPLINE.toString(), disc.getName());
                                    governance.getAnalyzerFi().getSelect().analyze(governance, disc.getOutput());
                                } else {
                                    governance.getOutput().append(disc.getOutput());
                                }
                            } catch (ServiceException e) {
                                e.printStackTrace();
                            } catch (RemoteException e) {
                                throw new ContextException(e);
                            }
                        }
                    }

                }
            }
        }
    }

    @Override
    public Context supervise(Context searchContext, Arg... args) throws SuperviseException, RemoteException {
        try {
            governance.setInput(searchContext);
            return (Context) execute(args);
        } catch (ServiceException e) {
            throw new SuperviseException(e);
        }
    }
}
