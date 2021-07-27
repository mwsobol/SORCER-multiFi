package sorcer.core.service;

import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import sorcer.service.*;

abstract public class TransdisciplineService extends MultiFiSlot implements Transdiscipline {

    protected Uuid id = UuidFactory.generate();

    @Override
    public Object getId() {
        return id;
    }
}
