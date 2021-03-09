package sorcer.service;

import net.jini.config.*;
import net.jini.core.transaction.Transaction;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.co.tuple.ExecPath;
import sorcer.core.SorcerConstants;
import sorcer.core.context.ContextSelector;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.ThrowableTrace;
import sorcer.core.context.model.ent.Coupling;
import sorcer.core.context.model.ent.Entry;
import sorcer.core.context.model.ent.Analyzer;
import sorcer.core.context.model.ent.Function;
import sorcer.core.monitor.MonitoringSession;
import sorcer.core.plexus.ContextFidelityManager;
import sorcer.core.plexus.FidelityManager;
import sorcer.core.plexus.MorphFidelity;
import sorcer.core.provider.ServiceBean;
import sorcer.core.provider.ServiceExerter;
import sorcer.core.signature.RemoteSignature;
import sorcer.core.signature.ServiceSignature;
import sorcer.security.util.SorcerPrincipal;
import sorcer.service.modeling.Data;
import sorcer.service.modeling.Finalization;
import sorcer.service.modeling.Functionality;
import sorcer.service.modeling.Model;
import sorcer.util.GenericUtil;
import sorcer.util.Pool;
import sorcer.util.Pools;

import javax.security.auth.Subject;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.*;

/**
 * Created by sobolemw on 5/4/15.
 */
public abstract class ServiceMogram extends MultiFiSlot<String, Object> implements Mogram, Activity, ServiceBean, Exec, Serializable, SorcerConstants {

    protected final static Logger logger = LoggerFactory.getLogger(ServiceMogram.class.getName());

    static final long serialVersionUID = 1L;

    protected Uuid mogramId;
    protected Uuid parentId;
    protected Contextion parent;
    protected String parentPath = "";
    protected ExecPath execPath;
    protected Uuid sessionId;
    protected String subjectId;
    protected Subject subject;
    protected String ownerId;
    protected String runtimeId;
    protected Long lsbId;
    protected Long msbId;
    protected String domainId;
    protected String subdomainId;
    protected String domainName;
    protected String subdomainName;
    protected FidelityManagement fiManager;
    protected ContextFidelityManager contextFidelityManager;
    protected Projection inPathProjection;
    protected Projection outPathProjection;
    // the last morphed projection
    protected Projection projection;
    protected Projection contextProjection;
    // list of fidelities of this mogram
    protected String[] metaFiNames;
    protected String[] profile;
    protected ServiceStrategy domainStrategy;
    protected Differentiator differentiator;
    protected Differentiator fdDifferentiator;
    protected Differentiator globalDifferentiator;
    protected Fidelity<Analyzer> mdaFi;
    protected List<Coupling> couplings;
    protected ContextSelector contextSelector;
    protected boolean isExec = true;
    /**
     * execution status: INITIAL|DONE|RUNNING|SUSPENDED|HALTED
     */
    protected Integer status = Exec.INITIAL;

    protected Integer priority;

    protected String description;

    protected String projectName;

    protected boolean isRevaluable = false;

    // indicates that is the parent of another mogram
    protected boolean isSuper = false;

    // true if the exertion has to be initialized (to original state)
    // or used as is after resuming from suspension or failure
    protected boolean isInitializable = true;

    protected String dbUrl;

    protected MetaFi multiMetaFi = new Metafidelity();

    protected MorphFidelity serviceMorphFidelity;

    protected SorcerPrincipal principal;

    // the current fidelity alias, as it is named in 'fidelities'
    // its original name might be different if aliasing is used
    // for already existing names
    protected String serviceFidelitySelector;

    // Date of creation of this Routine
    protected Date creationDate = new Date();

    protected Date lastUpdateDate;

    protected Date goodUntilDate;

    protected String accessClass;

    protected Boolean isExportControlled;

    protected static String defaultName = "mogram-";

    public static boolean debug = false;

    // sequence number for unnamed mogram instances
    protected static int count = 0;

    protected MonitoringSession monitorSession;

    protected Signature builder;

    protected String configFilename;

    protected ServiceContext dataContext;

    protected Fidelity<Finalization> finalizerFi;

    protected transient Exerter provider;

    protected boolean isEvaluated = false;

    protected ServiceMogram() {
        this(defaultName + count++);
    }

    public ServiceMogram(String name) {
        if (name == null || name.length() == 0)
            this.key = defaultName + count++;
        else
            this.key = name;
        init();
    }

    public ServiceMogram(String name, Signature builder) {
        this(name);
        this.builder = builder;
    }

    protected void init() {
        mogramId = UuidFactory.generate();
        multiFi = new ServiceFidelity();
        domainId = "0";
        subdomainId = "0";
        accessClass = PUBLIC;
        isExportControlled = Boolean.FALSE;
        status = INITIAL;
        principal = new SorcerPrincipal(System.getProperty("user.name"));
        principal.setId(principal.getName());
        setSubject(principal);
    }

    public void reset(int state) {
        status = state;
    }

    @Override
    public void setName(String name) {
        key = name;
    }

    public Uuid getMogramId() {
        return mogramId;
    }

    @Override
    public void setParentId(Uuid parentId) {
        this.parentId = parentId;
    }

    public Uuid getParentId() {
        return parentId;
    }

    @Override
    public ServiceContext getDataContext() throws ContextException {
        return dataContext;
    }

    public void setDataContext(Context dataContext) {
        this.dataContext = (ServiceContext) dataContext;
    }

    public List<Discipline> getAllMograms() {
        List<Discipline> exs = new ArrayList();
        getMograms(exs);
        return exs;
    }

    public List<Contextion> getAllContextions() {
        List<Contextion> exs = new ArrayList();
        getContextions(exs);
        return exs;
    }

    public List<Discipline> getMograms(List<Discipline> exs) {
        exs.add(this);
        return exs;
    }

    public List<Contextion> getContextions(List<Contextion> exs) {
        exs.add(this);
        return exs;
    }

    public List<String> getAllMogramIds() {
        List<String> mogIdsList = new ArrayList<String>();
        for (Discipline mo : getAllMograms()) {
            mogIdsList.add(mo.getId().toString());
        }
        return mogIdsList;
    }

    public void trimAllNotSerializableSignatures() throws SignatureException {
        trimNotSerializableSignatures();
        for (Discipline m : getAllMograms()) {
            ((ServiceMogram) m).trimNotSerializableSignatures();
        }
    }

    public Discipline getMogram(String componentMogramName) {
        if (key.equals(componentMogramName)) {
            return this;
        } else {
            List<Discipline> mograms = getAllMograms();
            for (Discipline m : mograms) {
                if (m.getName().equals(componentMogramName)) {
                    return m;
                }
            }
            return null;
        }
    }

    public void setService(Service provider) {
        RemoteSignature ps = (RemoteSignature) getProcessSignature();
        ps.setProvider(provider);
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int value) {
        status = value;
    }

    @Override
    public Context getContext() throws ContextException {
        return dataContext;
    }

    @Override
    public Context getOutput(Arg... args) throws ContextException {
        return dataContext;
    }

    @Override
    public void setContext(Context context) throws ContextException {
        dataContext = (ServiceContext) context;
    }

    @Override
    public Context appendContext(Context context) throws ContextException, RemoteException {
        return dataContext.appendContext(context);
    }

    @Override
    public Context getDomainData() throws ContextException, RemoteException {
        return dataContext;
    }

    @Override
    public Context getContext(Context contextTemplate) throws RemoteException, ContextException {
        return null;
    }

    @Override
    public Context appendContext(Context context, String path) throws ContextException, RemoteException {
        return dataContext.appendContext(context, path, false);
    }

    @Override
    public Context getContext(String path) throws ContextException, RemoteException {
        ServiceContext subcntxt = dataContext.getSubcontext();
        return subcntxt.appendContext(dataContext, path);
    }

    @Override
    public Uuid getId() {
        return mogramId;
    }

    public void setId(Uuid id) {
        mogramId = id;
    }

    @Override
    public <T extends Contextion> T exert(Transaction txn, Arg... args) throws MogramException {
        return null;
    }

    @Override
    public <T extends Contextion> T exert(Arg... args) throws MogramException {
        return null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public void setRuntimeId(String id) {
        runtimeId = id;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public void setSubdomainId(String subdomaindId) {
        this.subdomainId = subdomaindId;
    }

    public String getSubdomainId() {
        return subdomainId;
    }

    public Uuid getSessionId() {
        return sessionId;
    }

    public void setSessionId(Uuid sessionId) {
        this.sessionId = sessionId;
    }

    public Contextion getParent() {
        return parent;
    }

    public void setParent(Contextion parent) {
        this.parent = parent;
    }

    public SorcerPrincipal getPrincipal() {
        return principal;
    }

    public void setPrincipal(SorcerPrincipal principal) {
        this.principal = principal;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Override
    public Functionality.Type getType() {
        return Functionality.Type.MOGRAM;
    }

    private void setSubject(Principal principal) {
        if (principal == null)
            return;
        Set<Principal> principals = new HashSet<>();
        principals.add(principal);
        subject = new Subject(true, principals, new HashSet<>(), new HashSet<>());
    }

    public SorcerPrincipal getSorcerPrincipal() {
        if (subject == null)
            return null;
        Set<Principal> principals = subject.getPrincipals();
        Iterator<Principal> iterator = principals.iterator();
        while (iterator.hasNext()) {
            Principal p = iterator.next();
            if (p instanceof SorcerPrincipal)
                return (SorcerPrincipal) p;
        }
        return null;
    }

    public String getPrincipalId() {
        SorcerPrincipal p = getSorcerPrincipal();
        if (p != null)
            return getSorcerPrincipal().getId();
        else
            return null;
    }

    public void setPrincipalId(String id) {
        SorcerPrincipal p = getSorcerPrincipal();
        if (p != null)
            p.setId(id);
    }

    public long getMsbId() {
        return (msbId == null) ? -1 : msbId.longValue();
    }

    public void setLsbId(long leastSig) {
        if (leastSig != -1) {
            lsbId = new Long(leastSig);
        }
    }

    public void setMsbId(long mostSig) {
        if (mostSig != -1) {
            msbId = new Long(mostSig);
        }
    }

    public void setPriority(int p) {
        priority = p;
    }

    public int getPriority() {
        return (priority == null) ? MIN_PRIORITY : priority;
    }

    public Signature getProcessSignature() {
        ServiceFidelity selectedFi = (ServiceFidelity)multiFi.getSelect();
        if (selectedFi != null  && selectedFi.getSelect() != null) {
            return (Signature)selectedFi.getSelect();
        } else {
            if (selectedFi == null) {
                return null;
            }
        }

        Signature sig = null;
        for (Object s : selectedFi.selects) {
            if (s instanceof Signature && ((Signature)s).getExecType() == Signature.Type.PRO) {
                sig = (Signature)s;
                break;
            }
        }
        if (sig != null) {
            // a select is just a compute signature for the selection
            selectedFi.select = sig;
        }
        return sig;
    }

    public void trimNotSerializableSignatures() throws SignatureException {
        List<Discipline> mogs = getAllMograms();
        for (Discipline mog : mogs) {
            Fi mFi = mog.getMultiFi();
            if (mFi != null) {
                for (Object fi : mFi.getSelects()) {
                    if (fi instanceof ServiceFidelity)
                        trimNotSerializableSignatures((Fidelity) fi);
                }
            }
        }
    }

    private void trimNotSerializableSignatures(Fidelity<Signature> fidelity) throws SignatureException {
        if (fidelity.getSelect() instanceof Signature) {
            Iterator<Signature> i = fidelity.getSelects().iterator();
            while (i.hasNext()) {
                Signature sig = i.next();
                Class prvType = sig.getServiceType();
                if (!prvType.isInterface()
                    && !Serializable.class.isAssignableFrom(prvType)) {
                    i.remove();
                    if (sig == fidelity.getSelect()) {
                        fidelity.setSelect((Signature) null);
                    }
                    logger.warn("removed not serializable signature for: {}", prvType);
                }
            }
        }
    }

    public List<Signature> getApdProcessSignatures() {
        List<Signature> sl = new ArrayList<Signature>();
        for (Object s : ((ServiceFidelity)multiFi.getSelect()).getSelects()) {
            if (s instanceof Signature && ((Signature)s).getExecType() == Signature.Type.APD_DATA)
                sl.add((Signature)s);
        }
        return sl;
    }

    public List<Signature> getPreprocessSignatures() {
        List<Signature> sl = new ArrayList<Signature>();
        for (Object s : ((ServiceFidelity)multiFi.getSelect()).getSelects()) {
            if (s instanceof Signature && ((Signature)s).getExecType() == Signature.Type.PRE)
                sl.add((Signature)s);
        }
        return sl;
    }

    public List<Signature> getPostprocessSignatures() {
        List<Signature> sl = new ArrayList<Signature>();
        for (Object s : ((ServiceFidelity)multiFi.getSelect()).getSelects()) {
            if (s instanceof Signature && ((Signature)s).getExecType() == Signature.Type.POST)
                sl.add((Signature)s);
        }
        return sl;
    }

    /**
     * Adds a new signature <code>signature</code> for this mogram fidelity.
     **/
    public void addSignature(Signature... signatures) {
        if (signatures == null)
            return;
        String id = getOwnerId();
        if (id == null) {
            id = System.getProperty("user.name");
        }
        for (Signature sig : signatures) {
            ((ServiceSignature) sig).setOwnerId(id);
        }
        ServiceFidelity sFi = (ServiceFidelity) multiFi.getSelect();
        if (sFi == null) {
            multiFi.setSelect(new ServiceFidelity());
            sFi = (ServiceFidelity) multiFi.getSelect();
        }
        for (Signature sig : signatures) {
            sFi.getSelects().add(sig);
        }
    }

    /**
     * Removes a signature <code>signature</code> for this exertion.
     *
     * @see #addSignature
     */
    public void removeSignature(Signature signature) {
        ((ServiceFidelity)multiFi.getSelect()).getSelects().remove(signature);
    }

    public void setAccessClass(String s) {
        if (SENSITIVE.equals(s) || CONFIDENTIAL.equals(s) || SECRET.equals(s))
            accessClass = s;
        else
            accessClass = PUBLIC;
    }

    public String getAccessClass() {
        return (accessClass == null) ? PUBLIC : accessClass;
    }

    public void isExportControlled(boolean b) {
        isExportControlled = new Boolean(b);
    }

    public boolean isExportControlled() {
        return isExportControlled.booleanValue();
    }

    public Date getGoodUntilDate() {
        return goodUntilDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public void setGoodUntilDate(Date date) {
        goodUntilDate = date;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String id) {
        ownerId = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getDomainName() {
        if (domainName == null) {
            return key;
        }
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getSubdomainName() {
        return subdomainName;
    }

    public void setSubdomainName(String subdomainName) {
        this.subdomainName = subdomainName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String path) {
        parentPath = path;
    }

    public boolean isInitializable() {
        return isInitializable;
    }

    public void setIsInitializable(boolean isInitializable) {
        this.isInitializable = isInitializable;
    }

    public Mogram setExecPath(ExecPath execPath)
            throws ContextException {
        this.execPath = execPath;
        return this;
    }

    public ExecPath getExecPath() {
        return execPath;
    }

    public boolean isSuper() {
        return isSuper;
    }

    public void setSuper(boolean aSuper) {
        isSuper = aSuper;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public Fidelity getSelectedFidelity() {
        return (Fidelity) multiFi.getSelect();
    }

    public ContextSelector getContextSelector() {
        return contextSelector;
    }

    public void setContextSelector(ContextSelector contextSelector) {
        this.contextSelector = contextSelector;
    }

    public Discipline getComponentMogram(String path) {
        return this;
    }

    abstract public Mogram clearScope() throws MogramException;

    @Override
    public void applyFidelity(String name) {
        // implement in subclasses
    }

    /**
     * <p>
     * Returns <code>true</code> if this context is for modeling, otherwise
     * <code>false</code>. If context is for modeling then the values of this
     * context that implement the {@link Evaluation} interface are evaluated for
     * its requested evaluated values.
     * </p>
     *
     * @return the <code>true</code> if this context is revaluable.
     */
    public boolean isModeling() {
        return isRevaluable;
    }

    /*public boolean setValid() {
        return setValid;
    }

    public void setValid(boolean state) {
        setValid = state;
    }*/

    public void setModeling(boolean isRevaluable) {
        this.isRevaluable = isRevaluable;
    }

    public String toString() {
        StringBuffer info = new StringBuffer()
                .append(this.getClass().getName()).append(": " + key);
        info.append("\n  status=").append(status);
        info.append(", mogram ID=").append(mogramId);
        return info.toString();
    }

    /**
     * <p>
     * Returns the monitor session of this exertion.
     * </p>
     *
     * @return the monitorSession
     */
    public MonitoringSession getMonitorSession() {
        return monitorSession;
    }

    /**
     * <p>
     * Assigns a monitor session for this domains.
     * </p>
     *
     * @param monitorSession the monitorSession to set
     */
    public void setMonitorSession(MonitoringSession monitorSession) {
        this.monitorSession = monitorSession;
    }

    public MorphFidelity getServiceMorphFidelity() {
        return serviceMorphFidelity;
    }

    public void setServiceMorphFidelity(MorphFidelity morphFidelity) {
        this.serviceMorphFidelity = morphFidelity;
    }

    @Override
    public Signature getBuilder(Arg... args)  {
        return builder;
    }

    /**
     * Initialization by a service provider (container)
     * when this mogram is used as as a service bean.
     */
    public void init(Exerter provider) {
        this.provider = provider;
        logger.info("*** provider init properties:\n"
                + GenericUtil.getPropertiesString(((ServiceExerter)provider).getProviderProperties()));
        System.getProperties().putAll(((ServiceExerter)provider).getProviderProperties());
    }

    public void setBuilder(Signature builder) {
        this.builder = builder;
    }

    public void setSelectedFidelity(ServiceFidelity fidelity) {
        this.multiFi.setSelect(fidelity);
    }

    public MetaFi getMultiMetaFi() {
        return multiMetaFi;
    }

    public void setMultiMetaFi(MetaFi multiMetaFi) {
        this.multiMetaFi = multiMetaFi;
    }

    public FidelityManagement getFidelityManager() {
        return fiManager;
    }

    public void setFidelityManager(FidelityManagement fiManager) {
        this.fiManager = fiManager;
    }

    public ContextFidelityManager getContextFidelityManager() {
        return contextFidelityManager;
    }

    public void setContextFidelityManager(ContextFidelityManager contextFidelityManager) {
        this.contextFidelityManager = contextFidelityManager;
    }

    public FidelityManagement getRemoteFidelityManager() throws RemoteException {
        return getFidelityManager();
    }

    @Override
    public boolean isMonitorable() throws RemoteException {
        return false;
    }

    public Projection getProjection() {
        return projection;
    }

    public void setProjection(Projection projection) {
        this.projection = projection;
    }

    public String[] getProfile() {
        return profile;
    }

    public void setProfile(String[] profile) {
        this.profile = profile;
    }

    public Fidelity selectFidelity(Arg... entries) throws ConfigurationException {
        Fidelity fi = null;
        try {
            if (entries != null && entries.length > 0) {
                for (Arg a : entries)
                    if (a instanceof Projection) {
                        if (((Projection)a).fiType.equals((Fi.Type.CXT_PRJ))) {
                            Projection inPrj = ((Projection)a).getInPathProjection();
                            Projection outPrj = ((Projection)a).getOutPathProjection();
                            Fidelity cxtFi = ((Projection)a).getContextFidelity();
                            if (inPrj != null) {
                                inPathProjection = inPrj;
                            }
                            if (outPrj != null) {
                                outPathProjection = outPrj;
                            }
                            if (cxtFi != null) {
                                dataContext.selectFidelity(cxtFi.getName());
                            }
                        }
                    } else if (a instanceof Fidelity && ((Fidelity) a).fiType == Fidelity.Type.SELECT) {
                        Mogram mog = null;
                        if (((Fidelity) a).getPath() != null && ((Fidelity) a).getPath().length() > 0) {
                            mog = (Mogram) this.getComponentMogram(((Fidelity) a).getPath());
                        } else {
                            mog = this;
                        }
                        if (mog != null) {
                            fi = mog.selectFidelity(a.getName());
                        }
                    } else if (a instanceof Fidelity && ((Fidelity) a).fiType == Fidelity.Type.META) {
                        fi = selectMetafidelity((Fidelity) a);
                    } else if (a instanceof Fidelity && ((Fidelity) a).fiType == Fidelity.Type.CONTEXT) {
                        if (contextFidelityManager == null) {
                            dataContext.selectFidelity(a.getName());
                        } else {
                            contextFidelityManager.reconfigure((Fidelity)a);
                        }
                    } else if (a instanceof Fidelity && ((Fidelity) a).fiType == Fidelity.Type.PROJECTION) {{
                            contextFidelityManager.morph(a.getName());
                        }

                    }
            }
            ServiceContext cxt = (ServiceContext) getContext();
            if (cxt.getMorpher() != null) {
                contextFidelityManager.morph();
            }
        } catch (ContextException e) {
            throw new ConfigurationException(e);
        }
        return fi;
    }

    public Fidelity selectFidelity(String selector) throws ConfigurationException {
        if (multiFi.size() == 1) {
            return (Fidelity) multiFi.getSelect();
        }
        multiFi.selectSelect(selector);
        return (Fidelity) multiFi.getSelect();
    }

    public Fidelity selectMetafidelity(Fidelity fidelity) throws ConfigurationException {
        Metafidelity metaFi = null;
        Fidelity fi = fidelity;
        if (fidelity.fiType.equals(Fi.Type.META) && fidelity.getSelect() == null) {
            if (multiMetaFi != null) {
                multiMetaFi.selectSelect(fidelity.getName());
                metaFi = (Metafidelity) multiMetaFi.getSelect();
            } else {
                metaFi = (Metafidelity) fidelity;
            }
            Mogram mog = null;
            for (Object obj : metaFi.selects) {
                if (((Fidelity) obj).getPath() != null && ((Fidelity) obj).getPath().length() > 0) {
                    mog = (Mogram) this.getComponentMogram(((Fidelity) obj).getPath());
                } else {
                    mog = this;
                }
                fi = mog.selectFidelity(((Fidelity) obj).getName());
            }
        }
        return fi;
    }

    @Override
    public void reconfigure(Fidelity... fidelities) throws ConfigurationException {
        if (fiManager != null) {
            try {
                if (fidelities.length == 1 && fidelities[0] instanceof ServiceFidelity) {
                    List<Service> fiList = ((ServiceFidelity) fidelities[0]).getSelects();
                    Fidelity[] fiArray = new Fidelity[fiList.size()];
                    fiList.toArray(fiArray);
                    fiManager.reconfigure(fiArray);
                }
                fiManager.reconfigure(fidelities);
            } catch (EvaluationException | RemoteException e) {
                throw new ConfigurationException(e);
            }
        }
    }

    @Override
    public void morph(String... metaFiNames) throws ConfigurationException {
        if (fiManager != null) {
            this.metaFiNames = metaFiNames;
            try {
                fiManager.morph(metaFiNames);
            } catch (EvaluationException | RemoteException e) {
                throw new ConfigurationException(e);
            }
            profile = metaFiNames;
        } else {
            throw new ConfigurationException("No fiManager available in " + this.getClass().getName());
        }
    }

    @Override
    public ServiceStrategy getDomainStrategy() {
        return domainStrategy;
    }

    public void setModelStrategy(ServiceStrategy strategy) {
        domainStrategy = strategy;
    }

    public boolean isBatch() {
        return ((ServiceFidelity)multiFi.getSelect()).getSelects().size() > 1;
    }

    public void setConfigFilename(String configFilename) {
        this.configFilename = configFilename;
    }

    public void loadFiPool() {
        if (configFilename == null) {
            logger.warn("No mogram configuration file available for: {}", key);
        } else {
            initConfig(new String[]{configFilename});
        }
    }

    public void initConfig(String[] args) {
        Configuration config;
        try {
            config = ConfigurationProvider.getInstance(args, getClass()
                    .getClassLoader());

            Pool[] pools = (Pool[]) config.getEntry(Pools.COMPONENT, Pools.FI_POOL, Pool[].class);
            Pool<Fidelity, Service> pool = new Pool<>();
            pool.setFiType(Fi.Type.VAR_FI);
            for (int i = 0; i < pools.length; i++) {
                pool.putAll((Map<? extends Fidelity, ? extends ServiceFidelity>) pools[i]);
            }
            Pools.putFiPool(this, pool);

            List[] projections = (List[]) config.getEntry(Pools.COMPONENT, Pools.FI_PROJECTIONS, List[].class);
            Map<String, ServiceFidelity> metafidelities =
                    ((FidelityManager) getFidelityManager()).getMetafidelities();
            for (int i = 0; i < projections.length; i++) {
                for (Projection po : (List<Projection>) projections[i]) {
                    metafidelities.put(po.getName(), po);
                }
            }
        } catch (net.jini.config.ConfigurationException e) {
            logger.warn("configuratin failed for: " + configFilename);
            e.printStackTrace();
        }
        logger.debug("config fiPool: " + Pools.getFiPool(mogramId));
    }

    public <T> T getInstance() throws SignatureException {
        if (builder != null) {
            ServiceMogram mogram = (ServiceMogram) sorcer.co.operator.instance(builder);
            Class<T> clazz;
            clazz = (Class<T>) mogram.getClass();
            return (T) clazz.cast(mogram);
        } else {
            throw new SignatureException("No mogram builder available");
        }
    }

    public List<Coupling> getCouplings() {
        return couplings;
    }

    public void setCouplings(List<Coupling> couplings) {
        this.couplings = couplings;
    }

    public Fidelity<Analysis> getAnalysisFi(Context context) throws ConfigurationException {
        Fidelity<Analysis> analysisFi = null;
            Object mdaComponent = context.get(Context.MDA_PATH);
            if (mdaComponent != null) {
                if (mdaComponent instanceof Analyzer) {
                    analysisFi = new Fidelity(((Analyzer)mdaComponent).getName());
                    analysisFi.addSelect((Analyzer) mdaComponent);
                    analysisFi.setSelect((Analyzer)mdaComponent);
                } else if (mdaComponent instanceof ServiceFidelity
                    && ((ServiceFidelity) mdaComponent).getFiType().equals(Fi.Type.MDA)) {
                    analysisFi = (Fidelity) mdaComponent;
                }
            }
        return analysisFi;
    }

    public Fidelity<Analyzer> setMdaFi(Context context) throws ConfigurationException {
       if(mdaFi == null) {
           Object mdaComponent = context.get(Context.MDA_PATH);
           if (mdaComponent != null) {
               if (mdaComponent instanceof Analyzer) {
                   mdaFi = new Fidelity(((Analyzer)mdaComponent).getName());
                   mdaFi.addSelect((Analyzer) mdaComponent);
                   mdaFi.setSelect((Analyzer)mdaComponent);
               } else if (mdaComponent instanceof ServiceFidelity
                       && ((ServiceFidelity) mdaComponent).getFiType().equals(Fi.Type.MDA)) {
                   mdaFi = (Fidelity) mdaComponent;
               }
           }
       }
       return mdaFi;
    }

    public Fidelity<Analyzer> getMdaFi() {
        return mdaFi;
    }

    @Override
    public String getProjectionFi(String projectionName) throws ContextException, RemoteException {
        return ((FidelityManager)fiManager).getProjectionFi(projectionName);
    }

    public Differentiator getFdDifferentiator() {
        return fdDifferentiator;
    }

    public void setFdDifferentiator(Differentiator fdDifferentiator) {
        this.fdDifferentiator = fdDifferentiator;
    }

    public Differentiator getDifferentiator() {
        return differentiator;
    }

    public void setDifferentiator(Differentiator mogramDifferentiator) {
        this.differentiator = mogramDifferentiator;
    }

    @Override
    public Mogram deploy(List<Signature> builders) throws ConfigurationException {
        // to be implemented in subclasses
        return this;
    }

    @Override
    public void update(Setup... contextEntries) throws ContextException, RemoteException {
        // implement in subclasses
    }

    @Override
    public Entry act(Arg... args) throws ServiceException, RemoteException {
        Object result = this.execute(args);
        if (result instanceof Entry) {
            return (Entry)result;
        } else {
            return new Entry(key, result);
        }
    }

    @Override
    public Data act(String entryName, Arg... args) throws ServiceException, RemoteException {
        Object result = this.execute(args);
        if (result instanceof Entry) {
            return (Entry)result;
        } else {
            return new Entry(entryName, result);
        }
    }

    public Fidelity<Finalization> getFinalizerFi() {
        return finalizerFi;
    }

    public void setFinalizerFi(Fidelity<Finalization> finalizerFi) {
        this.finalizerFi = finalizerFi;
    }

    @Override
    public void reportException(String message, Throwable t) {
        domainStrategy.addException(t);
    }

    @Override
    public void reportException(String message, Throwable t, ProviderInfo info) {
        // reimplement in sublasses
        domainStrategy.addException(t);
    }

    @Override
    public void reportException(String message, Throwable t, Exerter provider) {
        // reimplement in sublasses
        domainStrategy.addException(t);
    }

    @Override
    public void reportException(String message, Throwable t, Exerter provider, ProviderInfo info) {
        // reimplement in sublasses
        domainStrategy.addException(t);
    }

    @Override
    public List<String> getTrace() throws RemoteException {
        return null;
    }

    @Override
    public void appendTrace(String info) throws RemoteException {

    }

    public Differentiator getGlobalDifferentiator() {
        return globalDifferentiator;
    }

    public void setGlobalDifferentiator(Differentiator globalDifferentiator) {
        this.globalDifferentiator = globalDifferentiator;
    }

    @Override
    public List<ThrowableTrace> getAllExceptions() throws RemoteException {
        return null;
    }

    public String getServiceFidelitySelector() {
        return serviceFidelitySelector;
    }

    public void setServiceFidelitySelector(String serviceFidelitySelector) {
        this.serviceFidelitySelector = serviceFidelitySelector;
    }

    public boolean equals(Object object) {
        if (object instanceof Mogram && mogramId.equals(((Mogram) object).getId())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Object getEvaluatedValue(String path) throws ContextException {
        // reimplement in subclasses
        if (isEvaluated) {
            if (this instanceof Context) {
                try {
                    if (this instanceof Model) {
                        return ((Context)((Model) this).getResult()).getValue(path);
                    } else {
                        return ((Context) this).getValue(path);
                    }
                } catch (RemoteException e) {
                    throw new ContextException(e);
                }
            } else if (this instanceof Routine) {
                ((Routine) this).getValue(path);
            }
        }
        throw new ContextException(getName() + "mogram not evaluated yet");
    }

    public boolean isEvaluated() {
        return isEvaluated;
    }

    public void setEvaluated(boolean evaluated) {
        isEvaluated = evaluated;
    }


    public String[] getMetaFiNames() {
        return metaFiNames;
    }

    public void setMetaFiNames(String[] metaFiNames) {
        this.metaFiNames = metaFiNames;
    }

    @Override
    public List<Discipline> getMograms() {
        List<Discipline> mograms = new ArrayList<>();
        mograms.add(this);
        return mograms;
    }

    @Override
    public List<Contextion> getContextions() {
        List<Contextion> contextiona = new ArrayList<>();
        contextiona.add(this);
        return contextiona;
    }

    public Mogram clear() throws MogramException {
        if (domainStrategy != null) {
            domainStrategy.getOutcome().clear();
        }
        isValid = false;
        isChanged = true;
        clearScope();
        return this;
    }

    public Functionality.Type getDependencyType() {
        return Function.Type.MOGRAM;
    }

    @Override
    public void substitute(Arg... args) throws SetterException, RemoteException {
            dataContext.substitute(args);
    }

    /**
     * Returns true if this exertion is a branching or looping exertion.
     */
    public boolean isConditional() {
        return false;
    }

    /**
     * Returns true if this exertion is composed of other exertions.
     */
    public boolean isCompound() {
        return false;
    }

    public Context getInConnector(Arg... args) throws ContextException, RemoteException {
        return null;
    }

    public Context getOutConnector(Arg... args) throws ContextException, RemoteException {
        return null;
    }

    public void execDependencies(String path, Arg... args) throws ContextException {
        // implement in subclasses
    }

    public Projection getContextProjection() {
        return contextProjection;
    }

    public void setContextProjection(Projection contextProjection) {
        this.contextProjection = contextProjection;
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

    public boolean isExec() {
        return isExec;
    }

    public void setExec(boolean exec) {
        isExec = exec;
    }

    public ServiceMogram copyFrom(ServiceMogram mogram) {
        super.copyFrom(mogram);

        // properties from ServiceMogram
        this.multiMetaFi = mogram.multiMetaFi;
        this.mogramId = mogram.mogramId;
        this.parentId = mogram.parentId;
        this.parent = mogram.parent;
        this.parentPath = mogram.parentPath;
        this.execPath = mogram.execPath;
        this.sessionId = mogram.sessionId;
        this.subjectId = mogram.subjectId;
        this.subject = mogram.subject;
        this.ownerId = mogram.ownerId;
        this.runtimeId = mogram.runtimeId;
        this.lsbId = mogram.lsbId;
        this.msbId = mogram.msbId;
        this.domainId = mogram.domainId;
        this.subdomainId = mogram.subdomainId;
        this.domainName = mogram.domainName;
        this.subdomainName = mogram.subdomainName;
        this.fiManager = mogram.fiManager;
        this.projection = mogram.projection;
        this.metaFiNames = mogram.metaFiNames;
        this.profile = mogram.profile;
        this.domainStrategy = mogram.domainStrategy;
        this.differentiator = mogram.differentiator;
        this.fdDifferentiator = mogram.fdDifferentiator;
        this.globalDifferentiator = mogram.globalDifferentiator;
        this.mdaFi = mogram.mdaFi;
        this.couplings = mogram.couplings;
        this.contextSelector = mogram.contextSelector;
        this.status = mogram.status;
        this.priority = mogram.priority;
        this.description = mogram.description;
        this.projectName = mogram.projectName;
        this.isRevaluable = mogram.isRevaluable;
        this.isSuper = mogram.isSuper;
        this.isInitializable = mogram.isInitializable;
        this.dbUrl = mogram.dbUrl;
        this.multiMetaFi = mogram.multiMetaFi;
        this.serviceMorphFidelity = mogram.serviceMorphFidelity;
        this.principal = mogram.principal;
        this.serviceFidelitySelector = mogram.serviceFidelitySelector;
        this.creationDate = mogram.creationDate;
        this.lastUpdateDate = mogram.lastUpdateDate;
        this.goodUntilDate = mogram.goodUntilDate;
        this.accessClass = mogram.accessClass;
        this.isExportControlled = mogram.isExportControlled;
        this.monitorSession = mogram.monitorSession;
        this.builder = mogram.builder;
        this.configFilename = mogram.configFilename;
        this.dataContext = mogram.dataContext;
        this.provider = mogram.provider;
        this.isEvaluated = mogram.isEvaluated;
        this.isExec = mogram.isExec;

        return this;
    }
}
