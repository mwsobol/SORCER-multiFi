package sorcer.sml.diciplines;

import builder.MuiltidisciplinaryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.arithmetic.provider.impl.*;
import sorcer.core.invoker.Pipeline;
import sorcer.core.service.Governance;
import sorcer.service.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sorcer.co.operator.*;
import static sorcer.ent.operator.*;
import static sorcer.eo.operator.args;
import static sorcer.eo.operator.*;
import static sorcer.eo.operator.fi;
import static sorcer.eo.operator.get;
import static sorcer.eo.operator.loop;
import static sorcer.eo.operator.result;
import static sorcer.mo.operator.*;
import static sorcer.mo.operator.model;
import static sorcer.mo.operator.out;
import static sorcer.mo.operator.value;
import static sorcer.so.operator.*;

/**
 * Created by Mike Sobolewski on 12/22/19.
 */
@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/sml")
public class Disciplines {

    private final static Logger logger = LoggerFactory.getLogger(Disciplines.class);

    @Test
    public void opservicePipeline() throws Exception {

        Opservice lambdaOut = invoker("lambdaOut",
            (Context<Double> cxt) -> value(cxt, "x") + value(cxt, "y") + 30,
            args("x", "y"));

        Opservice exprOut = invoker("exprOut", "lambdaOut - y", args("lambdaOut", "y"));

        Opservice sigOut = sig("multiply", MultiplierImpl.class,
            result("z", inPaths("lambdaOut", "exprOut")));

        Pipeline opspl = pl(
            lambdaOut,
            exprOut,
            sigOut);

        setContext(opspl, context("mfprc",
            inVal("x", 20.0),
            inVal("y", 80.0)));

        Context out = (Context) exec(opspl);

        logger.info("pipeline: " + out);
        assertEquals(130.0, value(out, "lambdaOut"));
        assertEquals(50.0, value(out, "exprOut"));
        assertEquals(6500.0, value(out, "z"));
    }

    @Test
    public void conditionalPipelineDiscipline() throws Exception {

        Opservice lambdaOut = invoker("lambdaOut",
            (Context<Double> cxt) -> value(cxt, "lambdaOut") + value(cxt, "x") + value(cxt, "y") + 10,
            args("x", "y"));

        Opservice exprOut = invoker("exprOut", "lambdaOut - y", args("lambdaOut", "y"));

        Opservice sigOut = sig("multiply", MultiplierImpl.class,
            result("z", inPaths("lambdaOut", "exprOut")));

        Pipeline opspl = pl(
            lambdaOut,
            exprOut,
            sigOut);

        // cxtn1 is a free contextion for a discipline dispatcher
        Block plDispatch = block(
            loop(condition(cxt -> (double)
                value(cxt, "lambdaOut") < 500.0), pipeline("cxtn1")));

        Discipline plDis = dsc(
            cxtnFi("cxtn1", opspl),
            dspFi("dspt1", plDispatch));

        setContext(opspl, context("mfprc",
            inVal("lambdaOut", 20.0),
            inVal("x", 20.0),
            inVal("y", 80.0)));

        // out is the discipline output
        Context out  = eval(plDis, fi("cxtn1", "dspt1"));

        logger.info("pipeline out: " + out);
        assertEquals(570.0, value(out, "lambdaOut"));
        assertEquals(490.0, value(out, "exprOut"));
        assertEquals(279300.0, value(out, "multiply"));
    }

    @Test
    public void morphModelDiscipline() throws Exception {

        // evaluate a discipline specified by a signature
        Context out  = eval(sig("getMorphModelDiscipline", MuiltidisciplinaryBuilder.class), fi("cxtn1", "dspt1"));

        logger.info("morphModelDiscipline cxt1:dspt1: " + out);
        assertTrue(value(out, "morpher3").equals(920.0));
    }

    @Test
    public void multiFiPipelineDisciplineFi_plDisc1() throws Exception {

        Signature discSig = sig("getMultiFiPipelineDiscipline",
            MuiltidisciplinaryBuilder.class);

        // first fidelity
        Context out = eval(discSig, fi("plDisc1"));

        logger.info("pipeline cxtn1:dspt1:cxt1: " + out);

        assertEquals(20.0, value(out, "x"));
        assertEquals(80.0, value(out, "y"));
        assertEquals(2000.0, value(out, "multiply"));
        assertEquals(100.0, value(out, "lambdaOut"));
        assertEquals(20.0, value(out, "exprOut"));
    }

    @Test
    public void multiFiPipelineyDiscipline_plDisc2() throws Exception {

        Signature discSig = sig("getMultiFiPipelineDiscipline",
            MuiltidisciplinaryBuilder.class);

        // first fidelity
        Context out = eval(discSig, fi("plDisc2"));

        logger.info(" pipeline cxtn2:dspt2:cxt2: " + out);

        assertEquals(20.0, value(out, "x"));
        assertEquals(80.0, value(out, "y"));
        assertEquals(228800.0, value(out, "multiply"));
        assertEquals(520.0, value(out, "lambdaOut"));
        assertEquals(440.0, value(out, "exprOut"));
    }

    @Test
    public void multidiscGovernance1() throws Exception {

        // the explicit input context with MDA
        Context govCxt = context(mdaFi("multidiscMdaFi",
            mda("analyzer",
                (Request gov, Context cxt) -> {
                    double x1, x2, x3;
                    String discName = dsc(cxt);
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

        logger.info("discipline morphModelDisc name: " + dsc(gov, "morphModelDisc").getName());
        logger.info("discipline plDisc name: " + dsc(gov, "plDisc").getName());
        assertEquals(dsc(gov, "morphModelDisc").getName(), "morphModelDisc");
        assertEquals(dsc(gov, "plDisc").getName(), "plDisc");

        Context out = eval(gov, govCxt);
        logger.info("gov morphModelDisc out: " + out(dsc(gov, "morphModelDisc")));
        logger.info("gov plDisc out: " + out(dsc(gov, "plDisc")));
        logger.info("gov out: " + out);

        assertEquals(0.092, value(out(gov), "g1"));
        assertEquals(4.6, value(out(gov), "g2"));
    }

    @Test
    public void multidiscGovernance2() throws Exception {
        // the default input context with MDA is defined with the governance

        Governance gov = (Governance) instance(
            sig("getMultidiscGovernance2", MuiltidisciplinaryBuilder.class));

        logger.info("discipline morphModelDisc name: " + dsc(gov, "morphModelDisc").getName());
        logger.info("discipline plDisc name: " + dsc(gov, "plDisc").getName());
        assertEquals(dsc(gov, "morphModelDisc").getName(), "morphModelDisc");
        assertEquals(dsc(gov, "plDisc").getName(), "plDisc");

        Context out = eval(gov);
        logger.info("gov morphModelDisc out: " + out(dsc(gov, "morphModelDisc")));
        logger.info("gov plDisc out: " + out(dsc(gov, "plDisc")));
        logger.info("gov out: " + out);

        assertEquals(0.092, value(out(gov), "g1"));
        assertEquals(4.6, value(out(gov), "g2"));
    }

}
