package sorcer.pml.modeling;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.arithmetic.provider.impl.MultiplierImpl;
import sorcer.core.invoker.*;
import sorcer.service.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sorcer.co.operator.*;
import static sorcer.co.operator.setValue;
import static sorcer.ent.operator.*;
import static sorcer.ent.operator.alt;
import static sorcer.ent.operator.loop;
import static sorcer.ent.operator.opt;
import static sorcer.eo.operator.args;
import static sorcer.eo.operator.*;
import static sorcer.eo.operator.fi;
import static sorcer.eo.operator.result;
import static sorcer.mo.operator.add;
import static sorcer.mo.operator.model;
import static sorcer.mo.operator.value;
import static sorcer.so.operator.eval;
import static sorcer.so.operator.exec;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/pml")
public class Pipelines {
	private final static Logger logger = LoggerFactory.getLogger(Pipelines.class);

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

        setContext(opspl, context("mfpcr",
                inVal("x", 20.0),
                inVal("y", 80.0)));

        Context out = (Context) exec(opspl);

        logger.info("pipeline: " + out);
        assertEquals(130.0, value(out, "lambdaOut"));
        assertEquals(50.0, value(out, "exprOut"));
        assertEquals(6500.0, value(out, "z"));
    }

	@Test
	public void n2Pipeline() throws Exception {

		Context data = context("mfpcr",
				inVal("x", 20.0),
				inVal("y", 80.0));

		Opservice lambdaOut = invoker("lambdaOut",
				(Context<Double> cxt) -> value(cxt, "x") + value(cxt, "y") + 30,
				args("x", "y"));

		Opservice exprOut = invoker("exprOut", "lambdaOut - y", args("lambdaOut", "y"));

		Opservice sigOut = sig("multiply", MultiplierImpl.class,
				result("z", inPaths("lambdaOut", "exprOut")));

		Pipeline pp = n2("n-squared", data,
				lambdaOut,
				exprOut,
				sigOut,
                appendInput(context(inVal("x", 40.0), inVal("y", 160.0))),
                lambdaOut);
//                exert(sig("getMogram", Invokers.class)));

		Context out = (Context) exec(pp);

		logger.info("pipeline: " + out);
//		assertEquals(130.0, value(out, "lambdaOut"));
        assertEquals(230.0, value(out, "lambdaOut"));
		assertEquals(50.0, value(out, "exprOut"));
		assertEquals(6500.0, value(out, "z"));
	}

}
