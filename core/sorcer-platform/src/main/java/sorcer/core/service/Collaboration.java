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

package sorcer.core.service;

import net.jini.core.transaction.Transaction;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.context.ContextList;
import sorcer.core.context.ModelStrategy;
import sorcer.core.context.ModelTask;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.OptimizerState;
import sorcer.core.context.model.ent.Coupling;
import sorcer.core.context.model.ent.Analyzer;
import sorcer.core.context.model.ent.Explorer;
import sorcer.core.plexus.FidelityManager;
import sorcer.service.*;
import sorcer.service.Node;
import sorcer.service.modeling.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static sorcer.so.operator.exec;
import static sorcer.so.operator.response;

public class Collaboration implements Transdiscipline, Dependency, cxtn {

	static final long serialVersionUID = 1L;

	protected static Logger logger = LoggerFactory.getLogger(Collaboration.class.getName());

	private static int count = 0;

	protected Uuid id = UuidFactory.generate();

	protected  String name;

	protected  String domainName;

    // the input of this collaboration
    protected Context input;

	protected ServiceFidelity contextMultiFi;

    // the output of this collaboration
    protected Context output;

	protected Map<String, Context> childrenContexts;

	// domain outputs
	protected ContextList outputs = new ContextList();

    protected Fi multiFi;

	protected Morpher morpher;

	protected Fidelity<Finalization> finalizerFi;

	protected Fidelity<Analysis> analyzerFi;

	protected Fidelity<Exploration> explorerFi;

	protected Fidelity<Development> developerFi;

	protected Map<String, Discipline> children = new HashMap<>();

	protected List<Coupling> couplings;

	// active disciplnes
	protected Paths domainPaths;

	// dependency management for this collaboration
	protected List<Evaluation> dependers = new ArrayList<Evaluation>();

	private FidelityManager fiManager;

	protected ServiceStrategy serviceStrategy;

	// context input connector
	protected Context inConnector;

	// context output connector
	protected Context outConnector;

	protected Context scope;

	protected Projection inPathProjection;

	protected Projection outPathProjection;

	protected Model.Pattern pattern =  Model.Pattern.COLLAB;

	protected Contextion parent;

	protected boolean isExec = true;

	public Collaboration() {
        this(null);
    }

    public Collaboration(String name) {
        if (name == null) {
            this.name = getClass().getSimpleName() + "-" + count++;
        } else {
            this.name = name;
        }
		serviceStrategy = new ModelStrategy(this);
    }

    public Collaboration(String name, Domain[] domains) {
        this(name);
        for (Domain domain : domains) {
                this.children.put(domain.getDomainName(), domain);
        }
    }

	public Collaboration(String name, List<Domain> domains) {
		this(name);
		for (Domain domain : domains) {
			this.children.put(domain.getDomainName(), domain);
		}
	}

    public Context getOutput(Arg... args) {
        return output;
    }

    public void setOutput(Context output) {
        this.output = output;
    }

    public Paths getDomainPaths() {
        return domainPaths;
    }

    public void setDomainPaths(Paths domainPaths) {
        this.domainPaths = domainPaths;
    }

	public Discipline getDomain(String name) {
		return children.get(name);
	}

	public Fidelity<Exploration> getExplorerFi() {
		return explorerFi;
	}

	public void setExplorerFi(Fidelity<Exploration> explorerFi) {
		this.explorerFi = explorerFi;
	}

	// default instance new Return(Context.RETURN);
	protected Context.Return contextReturn;

	@Override
	public Context getContext() throws ContextException {
		return input;
	}

	@Override
	public void setContext(Context input) {
		this.input = input;
	}

	@Override
	public Context appendContext(Context context) throws ContextException, RemoteException {
		return input.appendContext(context);
	}

	@Override
	public Context getDomainData() {
		return input;
	}

	@Override
	public Context getContext(Context contextTemplate) throws RemoteException, ContextException {
		return null;
	}

	@Override
	public Context appendContext(Context context, String path) {
		return null;
	}

	@Override
	public Context getContext(String path) throws ContextException, RemoteException {
		return null;
	}

	public boolean isExec() {
		return isExec;
	}

	public void setExec(boolean exec) {
		isExec = exec;
	}

	public Fidelity<Development> getDeveloperFi() {
		return developerFi;
	}

	public void setDeveloperFi(Fidelity<Development> developerFi) {
		this.developerFi = developerFi;
	}

	@Override
	public Context.Return getContextReturn() {
		return contextReturn;
	}

	@Override
	public void setName(String name) {
		this.name = name;
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
	public Object getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	public Fidelity<Analysis> getAnalyzerFi() {
		return analyzerFi;
	}

	public void setAnalyzerFi(Fidelity<Analysis> analyzerFi) {
		this.analyzerFi = analyzerFi;
	}

	public List<Discipline> getDisciplineList() {
		List<Discipline> domainList = new ArrayList<>();
		for (Discipline disc : children.values()) {
			if (disc instanceof Node) {
				domainList.add(disc);
			}
		}
		return domainList;
	}

	public Fidelity<Exploration> setExplorerFi(Context context) {
		if(explorerFi == null) {
			Object exploreComponent = context.get(Context.EXPLORER_PATH);
			if (exploreComponent != null) {
				if (exploreComponent instanceof Explorer) {
					explorerFi = new Fidelity(((Explorer)exploreComponent).getName());
					explorerFi.addSelect((Explorer) exploreComponent);
					explorerFi.setSelect((Explorer)exploreComponent);
					((Analyzer)exploreComponent).setContextion(this);
				} else if (exploreComponent instanceof ServiceFidelity
					&& ((ServiceFidelity) exploreComponent).getFiType().equals(Fi.Type.EXPLORER)) {
					explorerFi = (Fidelity) exploreComponent;
					((Explorer)explorerFi.getSelect()).setContextion(this);
				}

			}
		}
		((ServiceContext)context).getDomainStrategy().setExecState(Exec.State.INITIAL);
		if (output == null) {
			output = new ServiceContext(name);
		}
		return explorerFi;
	}

	public Fidelity<Analysis> setAnalyzerFi(Context context) {
		if(analyzerFi == null) {
			Object mdaComponent = context.get(Context.MDA_PATH);
			if (mdaComponent != null) {
				if (mdaComponent instanceof Analyzer) {
					analyzerFi = new Fidelity(((Analyzer)mdaComponent).getName());
					analyzerFi.addSelect((Analyzer) mdaComponent);
					analyzerFi.setSelect((Analyzer)mdaComponent);
					((Analyzer)mdaComponent).setContextion(this);
				} else if (mdaComponent instanceof ServiceFidelity
						&& ((ServiceFidelity) mdaComponent).getFiType().equals(Fi.Type.MDA)) {
					analyzerFi = (Fidelity) mdaComponent;
					((Analyzer)analyzerFi.getSelect()).setContextion(this);
				}
			}
		}
		((ServiceContext)context).getDomainStrategy().setExecState(Exec.State.INITIAL);
		if (output == null) {
			output = new ServiceContext(name);
		}
		return analyzerFi;
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

	public Context evaluateDomain(String domainName, Context context) throws ServiceException {
		return evaluateDomain(children.get(domainName), context);
	}

	public Context evaluateDomain(Request request, Context context) throws ServiceException {
			return response((Mogram) request, context);
	}

	@Override
	public Context evaluate(Context context, Arg... args) throws ServiceException {
		Context out = null;
		try {
			input = getInput();
			if (input == null) {
				input = context;
			} else if (context != null){
				input.append(context);
			}
			ModelStrategy strategy = ((ModelStrategy) input.getDomainStrategy());
			List<Fidelity> fis = Arg.selectFidelities(args);
			if (analyzerFi != null) {
				strategy.setExecState(Exec.State.RUNNING);
				// select mda Fi if provided
				for (Fi fi : fis) {
					if (analyzerFi.getName().equalsIgnoreCase(fi.getPath())) {
						analyzerFi.selectSelect(fi.getName());
					}
				}
				((Analyzer)analyzerFi.getSelect()).setContextion(this);
				logger.info("*** analyzerFi: {}", ((Analyzer)analyzerFi.getSelect()).getName());
			}
			if (explorerFi != null) {
				for (Fi fi : fis) {
					if (analyzerFi.getName().equalsIgnoreCase(fi.getPath())) {
						analyzerFi.selectSelect(fi.getName());
					}
				}
				((Explorer)explorerFi.getSelect()).setContextion(this);
				logger.info("*** explorerFi: {}", ((Identifiable)explorerFi.getSelect()).getName());
			}

			if (analyzerFi == null) {
				setAnalyzerFi(input);
			}
			if (explorerFi == null) {
				setExplorerFi(input);
			}
			if (explorerFi != null) {
				out = explorerFi.getSelect().explore(input);
				((ModelStrategy) serviceStrategy).setOutcome(out);
				strategy.setExecState(Exec.State.DONE);
			} else if (analyzerFi != null) {
				analyzerFi.getSelect().analyze(this, input);
				out = input;
				((ModelStrategy) serviceStrategy).setOutcome(out);
				strategy.setExecState(Exec.State.DONE);
			}
		} catch (ConfigurationException | ContextException | ExploreException | RemoteException | AnalysisException e) {
			throw new EvaluationException(e);
		}
		return out;
	}

	public void analyze(Context context) throws ServiceException {
		Context collabOut;
		if (((ServiceContext)context).getColabType() == Strategy.Colab.BBinCxt) {
			collabOut = input;
		} else {
			 collabOut = new ServiceContext(name);

		}
		Discipline domain = null;
		try {
			for (Path path : domainPaths) {
				domain = children.get(path.path);
				if (domain instanceof SignatureDomain) {
					domain = ((SignatureDomain) domain).getDomain();
					children.put(domain.getDomainName(), domain);
				}
				Context domainCxt = sorcer.mo.operator.getDomainContext(context, domain.getDomainName());
				Dispatch dispatcher = sorcer.mo.operator.getDomainDispatcher(context, domain.getDomainName());
				Context cxt = null;
				if (domainCxt != null) {
					if (domain instanceof Dispatch) {
						cxt = ((Dispatch) domain).dispatch(domainCxt);
						collabOut.append(cxt);
					} else if (dispatcher != null && dispatcher instanceof ModelTask) {
						((ModelTask) dispatcher).setContext(domainCxt);
						((ModelTask) dispatcher).setModel((Model) domain);
						Object response = exec((ModelTask) dispatcher);
						if (response instanceof Context) {
							cxt = (Context) response;
						} else  if (response instanceof Response) {
							cxt = ((Response)response).toContext();
						} else if (response instanceof OptimizerState) {
							cxt = ((OptimizerState)response).getDesignContext();
						} else {
							throw new ContextException("response not Context");
						}
						collabOut.append(cxt);
					} else if (domain.isExec()) {
						if (domain instanceof Mogram) {
							cxt = evaluateDomain(domain, domainCxt);
						} else {
							cxt = domain.evaluate(domainCxt);
						}
						collabOut.append(cxt);
					} else {
						collabOut = input;
					}
					if (cxt != null) {
						outputs.add(cxt);
					}
				} else if (domain.isExec()) {
					if (domain instanceof Context && ((ServiceContext) domain).getType() == Functionality.Type.EXEC) {
						// eventually add argument signatures per domain
						cxt = (Context) domain.execute();
					} else if (domain instanceof Mogram){
						cxt = response((Mogram)domain);
					} else {
						cxt = (Context) domain.execute();
					}
					collabOut.append(cxt.getDomainData());
					if (cxt != null) {
						outputs.add(cxt);
					}
				} else {
					collabOut = input;
				}

				Analysis analyzer = analyzerFi.getSelect();
				if (analyzerFi != null) {
					collabOut.putValue(Functionality.Type.DOMAIN.toString(), domain.getDomainName());
					analyzer.analyze(domain, collabOut);
				}

				collabOut.setSubject(name, this);
				((ServiceContext) collabOut).put(Context.DOMAIN_OUTPUTS_PATH, outputs);
			}
			output = collabOut;
		} catch (SignatureException | RemoteException | DispatchException | AnalysisException e) {
			throw new ContextException(e);
		}
	}

	public void initializeDomains() throws SignatureException {
		// initialize domains specified by builder signatures
		for (Discipline domain : children.values()) {
			if (domain instanceof SignatureDomain) {
				boolean isExec = domain.isExec();
				domain = ((SignatureDomain) domain).getDomain();
				children.put(domain.getDomainName(), domain);
				((Mogram)domain).setExec(isExec);
			}
		}
	}

	public OptimizationModeling getOptimizationDomain() {
		for (Discipline domain : children.values()) {
			if (domain instanceof OptimizationModeling) {
				return (OptimizationModeling) domain;
			}
		}
		return null;
	}

	public ServiceFidelity getContextMultiFi() {
		return contextMultiFi;
	}

	public void setContextMultiFi(ServiceFidelity contextMultiFi) {
		this.contextMultiFi = contextMultiFi;
	}

	public Context getInput() {
		// if no contextMultiFi then return direct input
		if (contextMultiFi == null || contextMultiFi.getSelect() == null) {
			return input;
		}
		input = (Context) contextMultiFi.getSelect();
		return input;
	}

	public Context setInput(Context input) {
		if (contextMultiFi == null) {
			contextMultiFi = new ServiceFidelity();
		}
		contextMultiFi.getSelects().add(input);
		contextMultiFi.setSelect(input);

		this.input = input;
		return input;
	}

	@Override
	public Context exert(Transaction txn, Arg... args) throws ServiceException {
		return evaluate(input, args);
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

	public Collaboration addDepender(Evaluation depender) {
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

	public List<Evaluation> getDependers() {
		return dependers;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public Map<String, Discipline> getChildren() {
		return children;
	}

	@Override
	public Discipline getChild(String name) {
		return children.get(name);
	}

	@Override
	public Context getScope() {
		return scope;
	}

	@Override
	public void setScope(Context scope) {
		this.scope = scope;;
	}

	public Object get(String path$domain) {
		String path = null;
		String domain = null;
		if (path$domain.indexOf("$") > 0) {
			int ind = path$domain.indexOf("$");
			path = path$domain.substring(0, ind);
			domain = path$domain.substring(ind + 1);
			return ((Mogram)getChild(domain)).get(path);
		} else if (path$domain != null) {
			return getChild(path$domain);
		}
		return null;
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
	public List<Contextion> getContextions(List<Contextion> contextionList) {
		for (Contextion e : getChildren().values()) {
			e.getContextions(contextionList);
		}
		contextionList.add(this);
		return contextionList;
	}

	@Override
	public Fidelity<Finalization> getFinalizerFi() {
		return finalizerFi;
	}

	public void setFinalizerFi(Fidelity<Finalization> finalizerFi) {
		this.finalizerFi = finalizerFi;
	}

	@Override
	public Map<String, Context> getChildrenContexts() {
		return childrenContexts;
	}

	public void setChildrenContexts(Map<String, Context> childrenContexts) {
		this.childrenContexts = childrenContexts;
	}

	public ContextList getOutputs() {
		return outputs;
	}

	public void setOutputs(ContextList outputs) {
		this.outputs = outputs;
	}

	public Functionality.Type getDependencyType() {
		return Functionality.Type.COLLABORATION;
	}

	@Override
	public void selectFidelity(Fidelity fi) {
	}

	public Contextion getParent() {
		return parent;
	}

	public void setParent(Contextion parent) {
		this.parent = parent;
	}

	public List<Coupling> getCouplings() {
		return couplings;
	}

	public void setCouplings(List<Coupling> couplings) {
		this.couplings = couplings;
	}

	public Functionality.Type getType() {
		return Functionality.Type.COLLABORATION;
	}

}
