package sorcer.service;

import sorcer.co.tuple.ExecDependency;
import sorcer.core.context.ModelStrategy;
import sorcer.core.context.ServiceContext;
import sorcer.core.service.Governance;
import sorcer.core.service.CollabRegion;
import sorcer.core.service.Region;
import sorcer.service.modeling.*;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import static sorcer.co.operator.path;

public class Gavernor implements Service, Hypervision {

    protected Governance governance;

    protected Hypervision rule;

    public Gavernor() {
        // do nothing
    }

    public Gavernor(Governance governance) {
        this.governance = governance;
    }

    public Gavernor(Governance governance, Hypervision rule) {
        this.governance = governance;
        this.rule = rule;
    }

    @Override
    public Object execute(Arg... args) throws ServiceException {
        try {
            List<Fi> fis = Arg.selectFidelities(args);
            Context input = Arg.selectContext(args);
            if (input != null) {
                governance.setInput(input);
            }
            if (fis != null && fis.size() > 0) {
                ((ServiceFidelity) governance.getMultiFi()).selectSelect(fis.get(0).getName());
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
                governance.getInput().putValue(Functionality.Type.REGION.toString(), governance.getName());
                analyzer.analyze(governance, governance.getInput());
            }
            return governance.getOutput();
        } catch (ConfigurationException | AnalysisException | RemoteException e) {
            throw new ServiceException(e);
        }
    }

    public void execDependencies(String path, Arg... args) throws ServiceException {
        Map<String, List<ExecDependency>> dpm = ((ModelStrategy) governance.getDomainStrategy()).getDependentDomains();
        if (dpm != null && dpm.get(path) != null) {
            List<ExecDependency> del = dpm.get(path);
            if (del != null && del.size() > 0) {
                for (ExecDependency de : del) {
                    List<Path> dpl = (List<Path>) de.getImpl();
                    if (dpl != null && dpl.size() > 0) {
                        for (Path p : dpl) {
                            try {
                                Region disc = governance.getRegion(p.path);
                                disc.evaluate(governance.getOutput(), args);
                                if (governance.getAnalyzerFi() != null && governance.getAnalyzerFi().getSelect() != null) {
                                    disc.getOutput().putValue(Functionality.Type.REGION.toString(), disc.getName());
                                    governance.getAnalyzerFi().getSelect().analyze(governance, disc.getOutput());
                                } else {
                                    governance.getOutput().append(disc.getOutput());
                                }
                            } catch (AnalysisException | RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
        }
    }

    public Hypervision getRule() {
        return rule;
    }

    public void setRule(Hypervision rule) {
        this.rule = rule;
    }


    @Override
    public Context hypervise(Context input, Arg... args) throws ExecutiveException, RemoteException {
        try {
            if (governance.getInput() == null)  {
                governance.setInput(input);
            } else {
                ((ServiceContext)governance.getInput()).substitute(input);
            }
            Context outCxt = (Context) execute(args);
            Hypervision executive = null;
            Context tmpCxt;
            if (governance.getExecutiveFi() != null) {
                executive = governance.getExecutiveFi().getSelect();
                tmpCxt = executive.hypervise(input, args);
                outCxt.appendContext(tmpCxt);
            }
            if (rule != null) {
                tmpCxt = rule.hypervise(outCxt);
                outCxt.appendContext(tmpCxt);
            }
            return outCxt;
        } catch (ServiceException e) {
            throw new ExecutiveException(e);
        }
    }
}
