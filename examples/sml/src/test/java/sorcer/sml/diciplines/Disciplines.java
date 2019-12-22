package sorcer.sml.diciplines;

import builder.MuiltidisciplinaryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.arithmetic.provider.*;
import sorcer.arithmetic.provider.impl.*;
import sorcer.core.context.model.EntModel;
import sorcer.core.context.model.ent.Entry;
import sorcer.core.invoker.Observable;
import sorcer.core.invoker.Pipeline;
import sorcer.core.plexus.FidelityManager;
import sorcer.core.plexus.MorphFidelity;
import sorcer.core.plexus.MultiFiMogram;
import sorcer.service.*;
import sorcer.service.Strategy.FidelityManagement;
import sorcer.service.modeling.*;
import sorcer.service.modeling.cxt;
import sorcer.service.modeling.fi;
import sorcer.service.modeling.mog;
import sorcer.service.modeling.sig;
import sorcer.sml.mograms.ModelMultiFidelities;

import java.rmi.RemoteException;

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
import static sorcer.mo.operator.value;
import static sorcer.so.operator.*;

/**
 * Created by Mike Sobolewski on 10/26/15.
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

        Discipline plDis = disc(
            cxtnFi("cxtn1", opspl),
            dsptFi("dspt1", plDispatch));

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

        // cxtn1 is a free contextion for a discipline dispatcher
        Block mdlDispatch = block(
            loop(condition(cxt -> (double)
                value(cxt, "morpher3") < 900.0), model("cxtn1")));

        Discipline morphDis = disc(
            cxtnFi("cxtn1", sig("cxtn1", ModelMultiFidelities.class, "getMorphingModel")),
            dsptFi("dspt1", mdlDispatch));

        // out is the discipline output
        Context out  = eval(morphDis, fi("cxtn1", "dspt1"));

        assertTrue(value(out, "morpher3").equals(920.0));
    }

    @Test
    public void multiFiPiplineDisciplineFi_plDisc1() throws Exception {

        Signature discSig = sig("getMultiFiPipelineDiscipline",
            MuiltidisciplinaryBuilder.class);

        // first fidelity
        Context out = eval(discSig, fi("plDisc1"));

        logger.info("pipeline c1:d1: " + out);

        assertEquals(20.0, value(out, "x"));
        assertEquals(80.0, value(out, "y"));
        assertEquals(3300.0, value(out, "multiply"));
        assertEquals(110.0, value(out, "lambdaOut"));
        assertEquals(30.0, value(out, "exprOut"));

    }

//    @Test
//    public void multiFiPiplineyDiscipline_plDisc2() throws Exception {
//
//        Signature discSig = sig("getMultiFiPipelineDiscipline",
//            MuiltidisciplinaryBuilder.class);
//
//        // first fidelity
//        Context out = eval(discSig, fi("plDisc2"));
//
//        logger.info("pipeline plDisc1: " + out);
//
////        assertEquals(20.0, value(out, "x"));
////        assertEquals(80.0, value(out, "y"));
////        assertEquals(3300.0, value(out, "multiply"));
////        assertEquals(110.0, value(out, "lambdaOut"));
////        assertEquals(30.0, value(out, "exprOut"));
//
//    }
}
