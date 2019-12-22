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
import static sorcer.mo.operator.disc;
import static sorcer.mo.operator.pipeline;
import static sorcer.mo.operator.value;

public class MuiltidisciplinaryBuilder {

	private final static Logger logger = LoggerFactory.getLogger(MuiltidisciplinaryBuilder.class);

	static public Discipline getMultiFiPipelineDiscipline() throws Exception {

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

		// evalTask dispatches the contextion Fi c1
		// evaluator("c1") is FreeEvaluator
		Task evalTask = task(evaluator("c1"));

		Context cxt1 = context("mfprc",
			inVal("lambdaOut", 0.0),
			inVal("x", 20.0),
			inVal("y", 80.0));

		Context cxt2 = context("mfprc",
			inVal("lambdaOut", 20.0),
			inVal("x", 20.0),
			inVal("y", 80.0));

		// cxtn1 is a free contextion for a discipline dispatcher
		Block blockDispatch = block(
			loop(condition(cxt -> (double)
				value(cxt, "lambdaOut") < 500.0), pipeline("cxtn1")));

		Discipline plDisc = disc("plDisc",
			discFi("plDisc1",
				cxtnFi("c1", opspl),
				cxtFi("cxt1", cxt1),
				dsptFi("d1", evalTask)),

			discFi("plDisc2",
				cxtnFi("c1"),
				cxtFi("cxt2", cxt2),
				dsptFi("d2", blockDispatch)));

		return plDisc;
	}
}
