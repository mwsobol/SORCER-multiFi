package sorcer.sml.regions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sml.builder.MuiltidisciplinaryBuilder;
import sorcer.core.service.Governance;
import sorcer.core.service.Region;
import sorcer.service.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sorcer.co.operator.setValue;
import static sorcer.co.operator.*;
import static sorcer.ent.operator.invoker;
import static sorcer.ent.operator.pl;
import static sorcer.eo.operator.fi;
import static sorcer.eo.operator.loop;
import static sorcer.eo.operator.result;
import static sorcer.eo.operator.*;
import static sorcer.mo.operator.out;
import static sorcer.mo.operator.value;
import static sorcer.mo.operator.*;
import static sorcer.so.operator.eval;
import static sorcer.so.operator.exec;

/**
 * Created by Mike Sobolewski on 03/13/2021.
 */
@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/sml")
public class Regions {

    private final static Logger logger = LoggerFactory.getLogger(Regions.class);

    @Test
    public void multiNodeRegion() throws Exception {

        // the explicit input context with exploration/analysis
        Context rgnCxt = context(explFi("explorer",
            expl("explorer1",
                (Context cxt) -> {
                    double x1, x2, x3;
                    String clbName = clbName(cxt);
                    if (clbName.equals("multi-node")) {
                        x1 = (double)value(cxt, "pl1");
                        x2 = (double)value(cxt, "pl2");
                        x3 = (double)value(cxt, "m1");
                        setValue(cxt, "g1", x3/(x1 * x1));
                        setValue(cxt, "g2", x3/(x1 + x1));
                    }
                    return cxt;
                })),
            mdaFi("analyzer",
                (mda("analyzer1",
                    (Request rgn, Context cxt) -> {
                        String dmnName = dmnName(cxt);
                        if (dmnName.equals("plDisc")) {
                            setValue(cxt, "pl1", value(cxt, "lambdaOut"));
                            setValue(cxt, "pl2", value(cxt, "exprOut"));
                        } else if (dmnName.equals("morphModelDisc")) {
                            setValue(cxt, "m1", value(cxt, "morpher3"));
                        }
                    }))));

        Region region = (Region) instance(
            sig("getMultinodeRegion", MuiltidisciplinaryBuilder.class));

        logger.info("node morphModelDisc name: " + name(rnd(region, "morphModelDisc")));
        logger.info("node plDisc name: " + name(rnd(region, "plDisc")));
        assertEquals(name(rnd(region, "morphModelDisc")), "morphModelDisc");
        assertEquals(name(rnd(region, "plDisc")), "plDisc");

        Context out = eval(region, rgnCxt);
        logger.info("node morphModelDisc out: " + out(rnd(region, "morphModelDisc")));
        logger.info("node plDisc out: " + out(rnd(region, "plDisc")));
        logger.info("node out: " + out);

        assertEquals(0.092, value(out(region), "g1"));
        assertEquals(4.6, value(out(region), "g2"));
    }

    @Test
    public void multiRgnGovernance1() throws Exception {

        // the explicit input context with MDA
        Context govCxt = context(mdaFi("multidiscMdaFi",
            mda("analyzer",
                (Request gov, Context cxt) -> {
                    double x1, x2, x3;
                    String discName = rgnn(cxt);
                    if (discName.equals("morphModelDisc")) {
                        setValue(gov, "m1", value(cxt, "morpher3"));
                    } else if (discName.equals("plDisc")) {
                        setValue(gov, "pl1", value(cxt, "lambdaOut"));
                        setValue(gov, "pl2", value(cxt, "exprOut"));
                    } else if (discName.equals(name(gov))) {
                        x1 = (double)value(gov, "pl1");
                        x2 = (double)value(gov, "pl2");
                        x3 = (double)value(gov, "m1");
                        setValue(gov, "g1", x3/(x1 * x1));
                        setValue(gov, "g2", x3/(x1 + x1));
                    }
                }))
        );

        Governance gov = (Governance) instance(
            sig("getMultidiscGovernance1", MuiltidisciplinaryBuilder.class));

        logger.info("discipline morphModelDisc name: " + name(rgn(gov, "morphModelDisc")));
        logger.info("discipline plDisc name: " + name(rgn(gov, "plDisc")));
        assertEquals(name(rgn(gov, "morphModelDisc")), "morphModelDisc");
        assertEquals(name(rgn(gov, "plDisc")), "plDisc");

        Context out = eval(gov, govCxt);
        logger.info("gov morphModelDisc out: " + out(rgn(gov, "morphModelDisc")));
        logger.info("gov plDisc out: " + out(rgn(gov, "plDisc")));
        logger.info("gov out: " + out);

        assertEquals(0.092, value(out(gov), "g1"));
        assertEquals(4.6, value(out(gov), "g2"));
    }

    @Test
    public void multiRgnGovernance2() throws Exception {
        // the default input context with MDA is defined with the governance

        Governance gov = (Governance) instance(
            sig("getMultidiscGovernance2", MuiltidisciplinaryBuilder.class));

        logger.info("discipline morphModelDisc name: " + name(rgn(gov, "morphModelDisc")));
        logger.info("discipline plDisc name: " + name(rgn(gov, "plDisc")));
        assertEquals(name(rgn(gov, "morphModelDisc")), "morphModelDisc");
        assertEquals(name(rgn(gov, "plDisc")), "plDisc");

        Context out = eval(gov);
        logger.info("gov morphModelDisc out: " + out(rgn(gov, "morphModelDisc")));
        logger.info("gov plDisc out: " + out(rgn(gov, "plDisc")));
        logger.info("gov out: " + out);

        assertEquals(0.092, value(out(gov), "g1"));
        assertEquals(4.6, value(out(gov), "g2"));
    }

}
