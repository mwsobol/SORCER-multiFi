package sorcer.core.context.model.srv;

import sorcer.core.plexus.FidelityManager;
import sorcer.service.*;
import sorcer.service.modeling.Transmodel;

import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by Mike Sobolewski on 11/10/2019.
 */
public class SvrTransmodel extends SrvModel implements Transmodel {

    protected Map<String, Domain> children = new HashMap<>();

    protected Paths childrenPaths;

    protected FidelityManager collabFiManager;

    public SvrTransmodel() {
        super();
    }

    public SvrTransmodel(String name) {
        super(name);
    }

    public Paths getChildrenPaths() {
        return childrenPaths;
    }

    public void setChildrenPaths(Paths childrenPaths) {
        this.childrenPaths = childrenPaths;
    }

    @Override
    public Domain getDomain(String domainName) {
        return children.get(domainName);
    }

    public FidelityManager getCollabFiManager() {
        return collabFiManager;
    }

    public void setCollabFiManager(FidelityManager collabFiManager) {
        this.collabFiManager = collabFiManager;
    }

    @Override
    public boolean configure(Object... configs) throws ConfigurationException, RemoteException {
        return false;
    }
}
