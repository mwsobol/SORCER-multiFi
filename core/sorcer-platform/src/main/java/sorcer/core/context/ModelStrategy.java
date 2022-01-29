package sorcer.core.context;

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import sorcer.co.tuple.ExecDependency;
import sorcer.service.*;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;
import sorcer.service.Strategy.Opti;
import sorcer.util.FileURLHandler;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by Mike Sobolewski
 */
public class ModelStrategy implements ServiceStrategy, Serializable {

    protected List<ThrowableTrace> exceptions = new ArrayList<ThrowableTrace>();

    protected List<String> traceList;

    private boolean isTraceable = false;

    private boolean isMonitorable = false;

    private boolean isProvisionable = false;

    private Contextion target;

    private Access accessType;

    private Flow flowType;

    private Opti optiType;

    private List<Signature> signatures = new ArrayList<Signature>();

    private  Map<String, Service> freeServices;

    private Context strategyContext;

    protected transient FileURLHandler dataService;

    // dependency management for this Model
    protected List<Evaluation> modelDependers = new ArrayList<Evaluation>();

    // dependency management for this Model entries
    protected List<Evaluation> dependers = new ArrayList<Evaluation>();


    protected boolean modelDependeciesExecuted = false;

    protected String currentSelector;

    // mapping from paths of this inConnector to input paths of this context
    protected Context inConnector;

    // mapping from paths of this context to input paths of requestors
    protected Context outConnector;

    // functional exec model dependencies
    protected Map<String, List<ExecDependency>> dependentPaths;

    // doamin exec transmodel dependencies
    protected Map<String, List<ExecDependency>> dependentDomains;

    protected ServiceFidelity  selectedFidelity;

    // select fidelities for this service context
    protected Map<String, ServiceFidelity> selectFidelities;

    // evaluated model response args
    protected Context outcome;

    protected volatile Exec.State execState = Exec.State.NULL;
    protected volatile Exec.State finalizeExecState = Exec.State.NULL;
    protected volatile Exec.State analysisExecState = Exec.State.NULL;
    protected volatile Exec.State exploreExecState = Exec.State.NULL;

    protected Exec.State superviseExecState = Exec.State.NULL;

    // reponse paths of the runtime model
    protected List<Path> responsePaths = new ArrayList<Path>();

    public ModelStrategy(Contextion service) {
        target = service;
    }

    public void setExceptions(List<ThrowableTrace> exceptions) {
        this.exceptions = exceptions;
    }

    public boolean isMonitorable() {
        return isMonitorable;
    }

    @Override
    public void setMonitorable(boolean state) {
        isMonitorable = state;
    }

    @Override
    public boolean isProvisionable() {
        return isProvisionable;
    }

    @Override
    public void setProvisionable(boolean state) {
        isProvisionable = state;
    }

    @Override
    public boolean isTracable() {
        return isTraceable;
    }

    @Override
    public void setTracable(boolean state) {
        isTraceable = state;
    }

    @Override
    public void setOpti(Strategy.Opti optiType) {
        this.optiType = optiType;
    }
    @Override
    public Strategy.Opti getOpti() {
        return optiType;
    }

    @Override
    public void addException(Throwable t) {
        exceptions.add(new ThrowableTrace(t));
    }

    @Override
    public void addException(String message, Throwable t) {
        exceptions.add(new ThrowableTrace(message, t));
    }

    public void setIsMonitorable(boolean isMonitorable) {
        this.isMonitorable = isMonitorable;
    }

    public List<ThrowableTrace> getExceptions() {
        return exceptions;
    }

    public List<String> getTraceList() {
        return traceList;
    }

    public void setTraceList(List<String> traceList) {
        this.traceList = traceList;
    }

    public List<ThrowableTrace> getAllExceptions() {
        return getExceptions();
    }

    public Map<String, List<ExecDependency>> getDependentPaths() {
        if (dependentPaths == null) {
            dependentPaths = new HashMap<String, List<ExecDependency>>();
        }
        return dependentPaths;
    }

    public Map<String, List<ExecDependency>> getDependentDomains() {
        if (dependentDomains == null) {
            dependentDomains = new HashMap<String, List<ExecDependency>>();
        }
        return dependentDomains;
    }

    public void setDependentDomains(Map<String, List<ExecDependency>> dependentDomains) {
        this.dependentDomains = dependentDomains;
    }

    public Context getInConnector(Arg... arg) {
        return inConnector;
    }

    public void setInConnector(Context inConnector) {
        this.inConnector = inConnector;
    }


    public Context getOutConnector(Arg... args) {
        return outConnector;
    }

    public void setOutConnector(Context outConnector) {
        this.outConnector = outConnector;
    }

    public String getCurrentSelector() {
        return currentSelector;
    }

    public void setCurrentSelector(String currentSelector) {
        this.currentSelector = currentSelector;
    }

    public void addMdelDependers(Evaluation... dependers) {
        if (this.modelDependers == null)
            this.modelDependers = new ArrayList<Evaluation>();
        for (Evaluation depender : dependers)
            this.modelDependers.add(depender);
    }

    public List<Evaluation> getModelDependers() {
        return modelDependers;
    }

    public void setModelDependers(List<Evaluation> modelDependers) {
        this.modelDependers = modelDependers;
    }

    public void addDependers(Evaluation... dependers) {
        if (this.dependers == null)
            this.dependers = new ArrayList<Evaluation>();
        for (Evaluation depender : dependers)
            this.dependers.add(depender);
    }

    public List<Evaluation> getDependers() {
        return dependers;
    }

    public void setSelectFidelities(Map<String, ServiceFidelity> selectFidelities) {
        this.selectFidelities = selectFidelities;
    }

    public Context getOutcome() {
        return outcome;
    }

    public void setResult(String path, Object value) throws ContextException {
        if (!responsePaths.contains(path))
            throw new ContextException("no such response path: " + path);
        outcome.putValue(path, value);
    }

    public List<Path> getResponsePaths() {
        return responsePaths;
    }

    public void setResponsePaths(String... paths) {
        List<Path> list = new ArrayList<>();
        for (String s : paths) {
            list.add(new Path(s));
        }
        this.responsePaths = list;
    }

    public void setResponsePaths(Path[] responsePaths) {
        this.responsePaths = Arrays.asList(responsePaths);
    }

    public void setResponsePaths(List responsePaths) {
        this.responsePaths = responsePaths;
    }


    public <T extends Mogram> Mogram exert(Transaction txn, Arg... entries) throws TransactionException, ServiceException, RemoteException {
        return target.exert(txn, entries);
    }

    public <T extends Mogram> Mogram exert(Arg... entries) throws TransactionException, ServiceException, RemoteException {
        return ((Mogram)target).exert(entries);
    }

    public boolean isModelDependeciesExecuted() {
        return modelDependeciesExecuted;
    }

    public void setModelDependeciesExecuted(boolean modelDependeciesExecuted) {
        this.modelDependeciesExecuted = modelDependeciesExecuted;
    }

    public Map<String, Service> getFreeServices() {
        return freeServices;
    }

    public List<Signature> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<Signature> signatures) {
        this.signatures = signatures;
    }

    public void setAccessType(Access access) {
        accessType = access;
    }

    public Access getAccessType() {
        return accessType;
    }

    public Flow getFlowType() {
        return flowType;
    }

    public void setFlowType(Flow flowType) {
        this.flowType = flowType;
    }

    public void setOutcome(Context outcome) {
        this.outcome = outcome;
    }

    public Contextion getTarget() {
        return target;
    }

    public void setTarget(Mogram target) {
        this.target = target;
    }

    public ServiceFidelity getFidelity() {
        return selectedFidelity;
    }

    public void setFidelity(ServiceFidelity fidelity) {
        selectedFidelity = fidelity;
    }

    public void appendTrace(String info) {
        if (traceList != null) {
            traceList.add(info);
        }
    }

    public List<String> getTrace() {
        return traceList;
    }

    public FileURLHandler getDataService() {
        return dataService;
    }

    public void setDataService(FileURLHandler dataService) {
        this.dataService = dataService;
    }

    public Exec.State getExecState() {
        return execState;
    }

    public void setExecState(Exec.State state) {
        execState = state;
    }
    public Context getStrategyContext() {
        return strategyContext;
    }

    public void setStrategyContext(Context strategyContext) {
        this.strategyContext = strategyContext;
    }

    public Exec.State getFinalizeExecState() {
        return finalizeExecState;
    }

    public void setFinalizeExecState(Exec.State finalizeExecState) {
        this.finalizeExecState = finalizeExecState;
    }

    public Exec.State getExploreExecState() {
        return exploreExecState;
    }

    public void setExploreExecState(Exec.State exploreExecState) {
        this.exploreExecState = exploreExecState;
    }

    public Exec.State getAnalysisExecState() {
        return analysisExecState;
    }

    public void setAnalysisExecState(Exec.State analysisExecState) {
        this.analysisExecState = analysisExecState;
    }

    public Exec.State getSuperviseExecState() {
        return superviseExecState;
    }

    public void setSuperviseExecState(Exec.State superviseExecState) {
        this.superviseExecState = superviseExecState;
    }

}
