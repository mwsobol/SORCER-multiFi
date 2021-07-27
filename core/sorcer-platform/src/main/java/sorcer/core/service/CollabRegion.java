package sorcer.core.service;

import sorcer.core.context.ModelStrategy;
import sorcer.service.*;

import java.rmi.RemoteException;
import java.util.List;

public class CollabRegion extends Collaboration implements Region {

    private static int count = 0;

    private Supervision supervisor;

    public CollabRegion(String name) {
        if (name == null) {
            this.key = getClass().getSimpleName() + "-" + count++;
        } else {
            this.key = name;
        }
        serviceStrategy = new ModelStrategy(this);
    }

    public CollabRegion(String name, Node[] nodes) {
        this(name);
        for (Node node : nodes) {
            this.children.put(node.getName(), node);
        }
    }

    public CollabRegion(String name, List<Node> nodes) {
        this(name);
        for (Node node : nodes) {
            this.children.put(node.getName(), node);
        }
    }

    public Supervision getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Supervision supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public Context evaluate(Context context, Arg... args) throws ServiceException {
        if (children.size() == 1) {
            try {
                output =  children.values().iterator().next().evaluate(context, args);
            } catch (MogramException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            output = super.evaluate(context, args);
        }
        return output;
    }

}
