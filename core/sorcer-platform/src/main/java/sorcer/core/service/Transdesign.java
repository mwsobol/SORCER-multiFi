package sorcer.core.service;

import net.jini.core.transaction.Transaction;
import sorcer.service.*;
import sorcer.service.modeling.Finalization;
import sorcer.service.modeling.Initialization;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A transdesign is a design class holding its multiple component designs.
 * Each transdesign manges its own component design.
 *
 * @author Mike Sobolewski 02/27/2023
 */

public class Transdesign implements Design {

    protected Map<String, Design> children;

    public Map<String, Design> getChildren() {
        return children;
    }

    public Transdesign(Map<String, Design> children) {
        this. children =children;
    }

    public Transdesign(List<Request> requests ) {
        if (requests.get(0) instanceof  Design) {
            if (this.children == null) {
                this.children = new HashMap<>(requests.size());
            }
            for (Request req : requests) {
                this.children.put(req.getName(), ( Design ) req);
            }
        } else if (requests.get(0) instanceof  Discipline) {
            if (this.children == null) {
                this.children = new HashMap<>(requests.size());
            }
            for (Request req : requests) {
                this.children.put(req.getName(),new ServiceDesign(( Discipline ) req));
            }
        }
    }

    public Transdesign(Request... requests ) {
        this(Arrays.asList(requests));
    }

    public void setChildren(Map<String, Design> children) {
        this.children = children;
    }

    public Design getChild(String name) {
        return children.get(name);
    }

    @Override
    public Context evaluate(Context context, Arg... args) throws ServiceException, RemoteException {
        return null;
    }

    @Override
    public <T extends Contextion> T exert(Arg... args) throws ServiceException, RemoteException {
        return null;
    }

    @Override
    public <T extends Contextion> T exert(Transaction txn, Arg... args) throws ServiceException, RemoteException {
        return null;
    }

    @Override
    public String getDomainName() throws RemoteException {
        return null;
    }

    @Override
    public Context getOutput(Arg... args) throws ContextException, RemoteException {
        return null;
    }

    @Override
    public void setContext(Context input) throws ContextException, RemoteException {

    }

    @Override
    public Context appendContext(Context context) throws ContextException, RemoteException {
        return null;
    }

    @Override
    public Context getDomainData() throws ContextException, RemoteException {
        return null;
    }

    @Override
    public Context getContext(Context contextTemplate) throws ContextException, RemoteException {
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
    public boolean isExec() throws RemoteException {
        return false;
    }

    @Override
    public Context.Return getContextReturn() throws RemoteException {
        return null;
    }

    @Override
    public ServiceStrategy getDomainStrategy() throws RemoteException {
        return null;
    }

    @Override
    public Projection getInPathProjection() throws RemoteException {
        return null;
    }

    @Override
    public Projection getOutPathProjection() throws RemoteException {
        return null;
    }

    @Override
    public List<Contextion> getContextions(List<Contextion> contextionList) throws RemoteException {
        return null;
    }

    @Override
    public FidelityManagement getFidelityManager() throws RemoteException {
        return null;
    }

    @Override
    public void selectFidelity(Fi fi) throws ConfigurationException, RemoteException {

    }

    @Override
    public void setParent(Contextion parent) {

    }

    @Override
    public Contextion getParent() {
        return null;
    }

    @Override
    public Context getContext() throws ContextException {
        return null;
    }

    @Override
    public Contextion getDiscipline() throws RemoteException {
        return null;
    }

    @Override
    public Fi getDevelopmentFi() throws RemoteException {
        return null;
    }

    @Override
    public Context design(Discipline discipline, Context context) throws DesignException, RemoteException {
        return null;
    }

    @Override
    public Fidelity<Initialization> getInitializerFi() {
        return null;
    }

    @Override
    public Fidelity<Finalization> getFinalizerFi() {
        return null;
    }

    @Override
    public Object getId() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public Fi getMultiFi() {
        return null;
    }

    @Override
    public Morpher getMorpher() {
        return null;
    }

    @Override
    public Context getScope() {
        return null;
    }

    @Override
    public void setScope(Context scope) {

    }

    @Override
    public Object execute(Arg... args) throws ServiceException, RemoteException {
        return null;
    }
}
