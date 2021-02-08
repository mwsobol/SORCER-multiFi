package sorcer.core.service;

import sorcer.core.context.ModelStrategy;
import sorcer.service.Node;
import sorcer.service.Supervision;

import java.util.List;

public class Region extends Collaboration {

    private static int count = 0;

    private Supervision supervisor;

    public Region(String name) {
        if (name == null) {
            this.name = getClass().getSimpleName() + "-" + count++;
        } else {
            this.name = name;
        }
        serviceStrategy = new ModelStrategy(this);
    }

    public Region(String name, Node[] nodes) {
        this(name);
        for (Node node : nodes) {
            this.children.put(node.getName(), node);
        }
    }

    public Region(String name, List<Node> nodes) {
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

}
