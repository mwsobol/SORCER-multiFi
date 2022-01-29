/*
 * Copyright 2019 the original author or authors.
 * Copyright 2019 SorcerSoft.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Mike Sobolewski
 */
package sorcer.core.service;

import net.jini.core.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.context.ModelStrategy;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.ent.Analyzer;
import sorcer.core.context.model.ent.Supervisor;
import sorcer.core.plexus.FidelityManager;
import sorcer.service.*;
import sorcer.service.Node;
import sorcer.service.modeling.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Governance extends Realm implements Dependency {

	private static final long serialVersionUID = 1L;

	protected static Logger logger = LoggerFactory.getLogger(Governance.class.getName());

	private static int count = 0;

    // the input of this governance
    protected Context input;

    // the output of this governance
    protected Context output;

    // active disciplnes
    protected Paths disciplnePaths = new Paths();

	protected Fidelity<Initialization> initializerFi;

	protected Fidelity<Finalization> finalizerFi;

	protected Fidelity<Analysis> analyzerFi;

	protected Fidelity<Exploration> explorerFi;

	protected Fidelity<Supervision> supervisorFi;

	protected Fidelity<Hypervision> executiveFi;

	protected ServiceFidelity contextMultiFi;

	protected Map<String, Region> regions = new HashMap<>();

	protected Map<String, Context> childrenContexts;

	private Hypervision governor;

	// dependency management for this governance
	protected List<Evaluation> dependers = new ArrayList<Evaluation>();

	private FidelityManager fiManager;

	protected ServiceStrategy serviceStrategy;

	// context input connector
	protected Context inConnector;

	// context output connector
	protected Context outConnector;

	protected Projection inPathProjection;

	protected Projection outPathProjection;

	protected boolean isExec = true;

    public Governance() {
        this(null);
    }

    public Governance(String name) {
        if (name == null) {
            this.key = getClass().getSimpleName() + "-" + count++;
        } else {
            this.key = name;
        }
		serviceStrategy = new ModelStrategy(this);
		governor = new Gavernor(this);
    }

    public Governance(String name, Region[] regions) {
        this(name);
        for (Region rgn : regions) {
                this.regions.put(rgn.getName(), rgn);
				disciplnePaths.add(new Path(rgn.getName()));
        }
    }

	public Governance(String name, List<Region> regions) {
		this(name);
		for (Region rgn : regions) {
			this.regions.put(rgn.getName(), rgn);
			disciplnePaths.add(new Path(rgn.getName()));
		}
	}

    public Context getOutput(Arg... args) {
        return output;
    }

    public void setOutput(Context output) {
        this.output = output;
    }

    public Paths getDisciplnePaths() {
        return disciplnePaths;
    }

    public void setDisciplnePaths(Paths disciplnePaths) {
        this.disciplnePaths = disciplnePaths;
    }

    public Map<String, Region> getRegions() {
		return regions;
	}

    public Supervision getSuperviser() {
        return supervisorFi.getSelect();
    }

	public void selectSuperviser(String name) throws ConfigurationException {
		supervisorFi.selectSelect(name);
	}
	// default instance new Return(Context.RETURN);
	protected Context.Return contextReturn;

	public ServiceFidelity getContextMultiFi() {
		return contextMultiFi;
	}

	public void setContextMultiFi(ServiceFidelity contextMultiFi) {
		this.contextMultiFi = contextMultiFi;
	}

	public Context getInput() throws ContextException {
		// if no contextMultiFi then return direct input
		if (contextMultiFi == null || contextMultiFi.getSelect() == null) {
			return input;
		}
		input = (Context) contextMultiFi.getSelect();
		return input;
	}

	public Context setInput(Context input) throws ContextException {
		if (contextMultiFi == null) {
			contextMultiFi = new ServiceFidelity();
		}
		contextMultiFi.getSelects().add(input);
		contextMultiFi.setSelect(input);

		this.input = input;
		return input;
	}

	@Override
	public Context getContext() throws ContextException {
		return input;
	}

	@Override
	public void setContext(Context input) throws ContextException {
		setInput(input);
	}

	@Override
	public Context appendContext(Context context) throws ContextException, RemoteException {
		return input.appendContext(context);
	}

	@Override
	public Context getDomainData() throws ContextException, RemoteException {
		return input;
	}

	@Override
	public Context getContext(Context contextTemplate) throws RemoteException, ContextException {
		return null;
	}

	@Override
	public Context appendContext(Context context, String path) throws ContextException, RemoteException {
		return null;
	}

	@Override
	public Context getContext(String path) throws ContextException, RemoteException {
		return null;
	}

	@Override
	public Context.Return getContextReturn() {
		return contextReturn;
	}

	@Override
	public void setName(String name) {
		this.key = name;
	}

	@Override
	public Fi getMultiFi() {
		return multiFi;
	}

	@Override
	public Morpher getMorpher() {
		return morpher;
	}

	@Override
	public String getName() {
		return (String) key;
	}

	@Override
	public Fidelity<Exploration> getExplorerFi() {
		return explorerFi;
	}

	@Override
	public Context analyze(Context modelContext, Arg... args) throws EvaluationException, RemoteException {
		try {
			analyzerFi.getSelect().analyze(this, modelContext);
		} catch (ServiceException | AnalysisException e) {
			throw new EvaluationException(e);
		}
		return output;
	}

	@Override
	public Context explore(Context context, Arg... args) throws ContextException, RemoteException {
		return explorerFi.getSelect().explore(context);
	}

	public void setExplorerFi(Fidelity<Exploration> explorerFi) {
		this.explorerFi = explorerFi;
	}

	public Fidelity<Analysis> getAnalyzerFi() {
		return analyzerFi;
	}

	public void setAnalyzerFi(Fidelity<Analysis> analyzerFi) {
		this.analyzerFi = analyzerFi;
	}

	public List<Region> getRegionList() {
		List<Region> discList = new ArrayList<>();
		for (Region disc : regions.values()) {
			if (disc instanceof Node) {
				discList.add(disc);
			}
		}
		return discList;
	}

	public Fidelity<Analysis> setAnalyzerFi(Context context) throws ConfigurationException {
		if(analyzerFi == null) {
			Object mdaComponent = ((ServiceContext)context).get(Context.MDA_PATH);
			if (mdaComponent != null) {
				if (mdaComponent instanceof Analyzer) {
					analyzerFi = new Fidelity(((Analyzer) mdaComponent).getName());
					analyzerFi.addSelect((Analyzer) mdaComponent);
					analyzerFi.setSelect((Analyzer) mdaComponent);
				} else if (mdaComponent instanceof ServiceFidelity
					&& ((ServiceFidelity) mdaComponent).getFiType().equals(Fi.Type.MDA)) {
					analyzerFi = (Fidelity) mdaComponent;
				}
			}
		}
		((ServiceContext)context).getDomainStrategy().setExecState(Exec.State.INITIAL);
		if (output == null) {
			output = new ServiceContext((String) key);
		}
		return analyzerFi;
	}

	public Fidelity<Supervision> setSupervisorFi(Context context) throws ConfigurationException {
		if(supervisorFi == null) {
			Object supComponent = ((ServiceContext)context).get(Context.MDA_PATH);
			if (supComponent != null) {
				if (supComponent instanceof Supervisor) {
					supervisorFi = new Fidelity(((Supervisor) supComponent).getName());
					supervisorFi.addSelect((Supervisor) supComponent);
					supervisorFi.setSelect((Supervisor) supComponent);
				} else if (supComponent instanceof ServiceFidelity
					&& ((ServiceFidelity) supComponent).getFiType().equals(Fi.Type.SUP)) {
					supervisorFi = (Fidelity) supComponent;
				}
			}
		}
		((ServiceContext)context).getDomainStrategy().setExecState(Exec.State.INITIAL);
		if (output == null) {
			output = new ServiceContext((String) key);
		}
		return supervisorFi;
	}

	public Fidelity<Hypervision> getExecutiveFi() {
		return executiveFi;
	}

	public void setExecutiveFi(Fidelity<Hypervision> executiveFi) {
		this.executiveFi = executiveFi;
	}

	public Context getInConnector() {
		return inConnector;
	}

	public void setInConnector(Context inConnector) {
		this.inConnector = inConnector;
	}

	public Context getOutConnector() {
		return outConnector;
	}

	public void setOutConnector(Context outConnector) {
		this.outConnector = outConnector;
	}

	public FidelityManager getFiManager() {
		return fiManager;
	}

	public void setFiManager(FidelityManager fiManager) {
		this.fiManager = fiManager;
	}

	public ServiceStrategy getDomainStrategy() {
		return serviceStrategy;
	}

	public void setModelStrategy(ServiceStrategy strategy) {
		serviceStrategy = strategy;
	}

	@Override
	public Object execute(Arg... args) throws ServiceException, RemoteException {
		Context context = Arg.selectContext(args);
		Context out = null;
		out = evaluate(context, args);
		return out;
	}

	@Override
	public Context evaluate(Context context, Arg... args) throws EvaluationException, RemoteException {
		Context out = null;
		Context cxt = context;
		if (cxt == null) {
			try {
				cxt = getInput();
			} catch (ContextException e) {
				throw new EvaluationException(e);
			}
		}

		// set mda if available
		try {
			if (analyzerFi == null) {
				setAnalyzerFi(cxt);
			}
			if (supervisorFi == null) {
				setSupervisorFi(cxt);
			}

			ModelStrategy strategy = ((ModelStrategy) cxt.getDomainStrategy());
			strategy.setExecState(Exec.State.RUNNING);
			governor.hypervise(cxt, args);
			((ModelStrategy) serviceStrategy).setOutcome(output);
			strategy.setExecState(Exec.State.DONE);
		} catch (ConfigurationException | ExecutiveException | ServiceException e) {
			throw new EvaluationException(e);
		}
		return out;
	}

	@Override
	public <T extends Contextion> T exert(Arg... args) throws ServiceException, RemoteException {
		return (T) execute(args);
	}

	@Override
	public String getDomainName() {
		return (String) key;
	}

	@Override
	public Context exert(Transaction txn, Arg... args) throws ContextException, RemoteException {
		return evaluate(input, args);
	}

	public Fidelity<Supervision> getSupervisorFi() {
		return supervisorFi;
	}

	public void setSupervisorFi(Fidelity<Supervision> supervisorFi) {
		this.supervisorFi = supervisorFi;
	}

	public void reportException(String message, Throwable t) {
		serviceStrategy.addException(t);
	}

	public void reportException(String message, Throwable t, ProviderInfo info) {
		// reimplement in sublasses
		serviceStrategy.addException(t);
	}

	public void reportException(String message, Throwable t, Exerter provider) {
		// reimplement in sublasses
		serviceStrategy.addException(t);
	}

	public void reportException(String message, Throwable t, Exerter provider, ProviderInfo info) {
		// reimplement in sublasses
		serviceStrategy.addException(t);
	}

	public Governance addDepender(Evaluation depender) {
		if (this.dependers == null)
			this.dependers = new ArrayList<Evaluation>();
		dependers.add(depender);
		return this;
	}

	@Override
	public void addDependers(Evaluation... dependers) {
		if (this.dependers == null)
			this.dependers = new ArrayList<Evaluation>();
		for (Evaluation depender : dependers)
			this.dependers.add(depender);
	}

	@Override
	public List<Evaluation> getDependers() {
		return dependers;
	}

	public Map<String, Region> getChildren() {
		return regions;
	}

	@Override
	public Map<String, Context> getChildrenContexts() {
		return childrenContexts;
	}

	@Override
	public Region getChild(String name) {
		return regions.get(name);
	}

	public Region getRegion(String name) {
		return regions.get(name);
	}

	@Override
	public Context getScope() {
		return scope;
	}

	@Override
	public void setScope(Context scope) {
		this.scope = scope;
	}

	@Override
	public Projection getInPathProjection() {
		return inPathProjection;
	}

	public void setInPathProjection(Projection inPathProjection) {
		this.inPathProjection = inPathProjection;
	}

	@Override
	public Projection getOutPathProjection() {
		return outPathProjection;
	}

	public void setOutPathProjection(Projection outPathProjection) {
		this.outPathProjection = outPathProjection;
	}

	@Override
	public List<Contextion> getContextions(List<Contextion> contextionList) throws RemoteException {
		for (Contextion e : regions.values()) {
			e.getContextions(contextionList);
		}
		contextionList.add(this);
		return contextionList;
	}

	public Fidelity<Initialization> getInitializerFi() {
		return initializerFi;
	}

	public void setInitializerFi(Fidelity<Initialization> initializerFi) {
		this.initializerFi = initializerFi;
	}

	@Override
	public Fidelity<Finalization> getFinalizerFi() {
		return finalizerFi;
	}

	public void setFinalizerFi(Fidelity<Finalization> finalizerFi) {
		this.finalizerFi = finalizerFi;
	}

	public Functionality.Type getDependencyType() {
		return Functionality.Type.GOVERNANCE;
	}

	@Override
	public void selectFidelity(Fi fi) throws ConfigurationException {

	}

	@Override
	public boolean isExec() {
		return isExec;
	}

	public void setExec(boolean exec) {
		isExec = exec;
	}

	@Override
	public List<Signature> getAllSignatures() throws RemoteException {
		return null;
	}
}
