package sorcer.sml.regions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.arithmetic.provider.impl.MultiplierImpl;
import sorcer.core.invoker.Pipeline;
import sorcer.service.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sorcer.co.operator.setValue;
import static sorcer.co.operator.*;
import static sorcer.ent.operator.invoker;
import static sorcer.ent.operator.pl;
import static sorcer.eo.operator.loop;
import static sorcer.eo.operator.result;
import static sorcer.eo.operator.*;
import static sorcer.mo.operator.value;
import static sorcer.mo.operator.*;
import static sorcer.so.operator.eval;

/**
 * Created by Mike Sobolewski on 03/09/21.
 */
@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/sml")
public class DisciplinaryFidelities {

    private final static Logger logger = LoggerFactory.getLogger(DisciplinaryFidelities.class);

    @Test
    public void conditionalPipelineDiscipline() throws Exception {

        Opservice lambdaOut = invoker("lambdaOut",
            (Context<Double> cxt) -> value(cxt, "lambdaOut") + value(cxt, "x") + value(cxt, "y") + 10,
            args("x", "y"));

        Opservice exprOut = invoker("exprOut", "lambdaOut - y", args("lambdaOut", "y"));

        Opservice sigOut = sig("multiply", MultiplierImpl.class,
            result("z", inPaths("lambdaOut", "exprOut")));

        Pipeline opspl = pl("cxtn1",
            lambdaOut,
            exprOut,
            sigOut);

        // cxtn1 is a free contextion for a discipline dispatcher
        Block plDispatch = block(
            loop(condition(cxt -> (double)
                value(cxt, "lambdaOut") < 500.0), pipeline("cxtn1")));

        Node plDis = rnd("pln-nd",
            rndFi("pln-nd", cxtnFi("cxtn1", opspl), rndFi("dspt1", plDispatch)));

        Node plDis2 = rnd("pln-nd2",
            rndFi("pln-nd", cxtnFi(opspl), rndFi(plDispatch)));

        Node plDis3 = rnd("pln-nd3",
            rndFi("pln-nd",
                cxtnFi(bet("cxt1", opspl), bet("cxt2", opspl)),
                rndFi(bet("dsp1", plDispatch), bet("dsp2", opspl))));

        setContext(opspl, context("mfprc",
            inVal("lambdaOut", 20.0),
            inVal("x", 20.0),
            inVal("y", 80.0)));

        // out is the discipline output
        Context out  = eval(plDis);

        logger.info("pipeline out: " + out);
        assertEquals(570.0, value(out, "lambdaOut"));
        assertEquals(490.0, value(out, "exprOut"));
        assertEquals(279300.0, value(out, "multiply"));
    }
}
