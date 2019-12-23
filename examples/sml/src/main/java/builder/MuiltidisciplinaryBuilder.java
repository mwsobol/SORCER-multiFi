package builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.arithmetic.provider.impl.MultiplierImpl;
import sorcer.core.context.ModelTask;
import sorcer.core.exertion.EvaluationTask;
import sorcer.core.invoker.Pipeline;
import sorcer.service.*;

import java.io.File;

import static sorcer.co.operator.inPaths;
import static sorcer.co.operator.inVal;
import static sorcer.co.operator.val;
import static sorcer.ent.operator.ent;
import static sorcer.ent.operator.invoker;
import static sorcer.ent.operator.pl;
import static sorcer.eo.operator.*;
import static sorcer.mo.operator.*;

public class MuiltidisciplinaryBuilder {

	private final static Logger logger = LoggerFactory.getLogger(MuiltidisciplinaryBuilder.class);

	static public Discipline getMultiFiPipelineDiscipline() throws Exception {

		// evalTask dispatches the contextion Fi cxtn1
		// evaluator("cxtn1") is FreeEvaluator to be bound to Fi cxtn1
		Task evalTask = task(evaluator("cxtn1"));

		Context cxt1 = context("cxt1",
			inVal("lambdaOut", 0.0),
			inVal("x", 20.0),
			inVal("y", 80.0));

		Context cxt2 = context("cxt2",
			inVal("lambdaOut", 20.0),
			inVal("x", 20.0),
			inVal("y", 80.0));

		// cxtn1 is a free contextion for a discipline dispatcher
		Block blockDispatch = block(
			loop(condition(cxt -> (double)
				value(cxt, "lambdaOut") < 500.0), pipeline("cxtn2")));

		Discipline plDisc = disc("plDisc",
			discFi("plDisc1",
				cxtnFi("cxtn1", sig("getPipeline1",  MuiltidisciplinaryBuilder.class)),
				cxtFi("cxt1", cxt1),
				dsptFi("dspt1", evalTask)),

			discFi("plDisc2",
				cxtnFi("cxtn2", sig("getPipeline2",  MuiltidisciplinaryBuilder.class)),
				cxtFi("cxt2", cxt2),
				dsptFi("dspt2", blockDispatch)));

		return plDisc;
	}

	static public Pipeline getPipeline1() throws Exception {
		Opservice lambdaOut = invoker("lambdaOut",
			(Context<Double> cxt) -> value(cxt, "lambdaOut") + value(cxt, "x") + value(cxt, "y"),
			args("x", "y"));

		Opservice exprOut = invoker("exprOut", "lambdaOut - y", args("lambdaOut", "y"));

		Opservice sigOut = sig("multiply", MultiplierImpl.class,
			result("z", inPaths("lambdaOut", "exprOut")));

		Pipeline opspl = pl(
			lambdaOut,
			exprOut,
			sigOut);
		return opspl;
	}


	static public Pipeline getPipeline2() throws Exception {
		Opservice lambdaOut = invoker("lambdaOut",
			(Context<Double> cxt) -> { Double out = value(cxt, "x") + value(cxt, "y") + value(cxt, "lambdaOut");
				setValue(cxt, "lambdaOut", out);
				return out; },
			args("x", "y"));

		Opservice exprOut = invoker("exprOut", "lambdaOut - y", args("lambdaOut", "y"));

		Opservice sigOut = sig("multiply", MultiplierImpl.class,
			result("z", inPaths("lambdaOut", "exprOut")));

		Pipeline opspl = pl(
			lambdaOut,
			exprOut,
			sigOut);
		return opspl;
	}
}
