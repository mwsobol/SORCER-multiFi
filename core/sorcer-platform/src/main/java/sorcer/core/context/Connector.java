package sorcer.core.context;

import sorcer.service.Opservice;

/**
 * Created by Mike Sobolewski on 02/13/15.
 */
public class Connector extends ServiceContext implements Opservice {

    public enum Direction { IN, OUT, FILTER }

    public Direction direction;

    public boolean isRedundant = false;

    public Connector() {
        super();

    }

    /**
     * Constructor for a named instance of MapContext
     * @param name
     * @see ServiceContext
     */
    public Connector(String name) {
        super(name);
    }
}
