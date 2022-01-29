package sorcer.sml.collaborations;

import sml.builder.CollabBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.core.service.Collaboration;
import sorcer.service.*;

import static org.junit.Assert.assertEquals;
import static sorcer.co.operator.*;
import static sorcer.co.operator.setValue;
import static sorcer.eo.operator.*;
import static sorcer.mo.operator.*;
import static sorcer.mo.operator.value;
import static sorcer.so.operator.eval;


/**
 * Created by Mike Sobolewski on 12/27/19.
 */
@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/sml")
public class Collaborations {

    private final static Logger logger = LoggerFactory.getLogger(Collaborations.class);

    @Test
    public void domainCollaboration1() throws Exception {

        // the explicit input context with expl and MDA
        Context collabCxt = context(explFi("explorer",
            expl("explorer1",
                (Context cxt) -> {
                    double z1, z2;
                    String clbName = clbName(cxt);
                    if (clbName.equals("domainCollab")) {
                        z1 = (double)value(cxt, "z1");
                        z2 = (double)value(cxt, "z2");
                        setValue(cxt, "ratio", z2/z1);
                    }
                    return cxt;
                })),
            mdaFi("analyzer",
                (mda("analyzer1",
                    (Request collab, Context cxt) -> {
                        double y1, y2, y3;
                        String dmnName = dmnName(cxt);
                        if (dmnName.equals("srvBlock")) {
                            y1 = (double)value(cxt, "arg/x1");
                            y2 = (double)value(cxt, "arg/x2");
                            y3 = (double)value(cxt, "arg/t5");
                            setValue(cxt, "z1", (y3 * y2) / y1);
                        } else if (dmnName.equals("entModel")) {
                            y1 = (double)value(cxt, "y1");
                            y2 = (double)value(cxt, "y2");
                            setValue(cxt, "z2", (y1 + y2) / 2.4);
                        }
                    }))));

        Collaboration collab = (Collaboration) instance(
            sig("getArithmeticColab1", CollabBuilder.class));

        logger.info("domain srvblock name: " + dmn(collab, "srvBlock").getName());
        logger.info("domain entModel name: " + dmn(collab, "entModel").getName());
        assertEquals(dmn(collab, "srvBlock").getName(), "srvBlock");
        assertEquals(dmn(collab, "entModel").getName(), "entModel");

        Context out = eval(collab, collabCxt);
        logger.info("domainCollab out: " + out);
        assertEquals(0.5, value(out, "ratio"));
    }

    @Test
    public void domainCollaboration2() throws Exception {
        // the default input context with MDA is defined with the governance

        Collaboration collab = (Collaboration) instance(
            sig("getArithmeticColab2", CollabBuilder.class));

        logger.info("domain srvblock name: " + dmn(collab, "srvBlock").getName());
        logger.info("domain entModel name: " + dmn(collab, "entModel").getName());
        assertEquals(dmn(collab, "srvBlock").getName(), "srvBlock");
        assertEquals(dmn(collab, "entModel").getName(), "entModel");

        Context out = eval(collab);
        logger.info("domainCollab out: " + out);
        assertEquals(0.5, value(out, "ratio"));
    }

}
