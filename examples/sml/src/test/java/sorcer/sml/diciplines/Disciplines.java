package sorcer.sml.diciplines;

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

    public Pipeline getPipeline() throws Exception {

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

        // cxtn1 is a free contextion for a discipline dispatcher
        Block plDispatch = block(
            loop(condition(cxt -> (double)
                value(cxt, "morpher3") < 900.0), model("cxtn1")));

        Discipline plDis = disc(
            cxtnFi("cxtn1", opspl),
            dsptFi("dspt1", plDispatch));

        // out is the discipline output
        Context out  = eval(plDis, fi("cxtn1", "dspt1"));


        return opspl;

//        setContext(opspl, context("mfprc",
//            inVal("x", 20.0),
//            inVal("y", 80.0)));
//
//        Context out = (Context) exec(opspl);
//
//        logger.info("pipeline: " + out);
//        assertEquals(130.0, value(out, "lambdaOut"));
//        assertEquals(50.0, value(out, "exprOut"));
//        assertEquals(6500.0, value(out, "z"));
    }


    @Test
    public void morphingDiscipline() throws Exception {

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
}
