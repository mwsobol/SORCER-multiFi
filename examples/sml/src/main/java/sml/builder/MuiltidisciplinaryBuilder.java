package sml.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.arithmetic.provider.*;
import sorcer.arithmetic.provider.impl.AdderImpl;
import sorcer.arithmetic.provider.impl.MultiplierImpl;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.core.invoker.Pipeline;
import sorcer.core.service.Governance;
import sorcer.core.service.Region;
import sorcer.service.*;
import sorcer.service.modeling.fi;
import sorcer.service.modeling.mog;
import sorcer.service.modeling.sig;

import static sorcer.co.operator.*;
import static sorcer.co.operator.instance;
import static sorcer.ent.operator.ent;
import static sorcer.ent.operator.invoker;
import static sorcer.ent.operator.pl;
import static sorcer.eo.operator.*;
import static sorcer.mo.operator.*;
import static sorcer.so.operator.response;

/**
 * @author Mike Sobolewski 03/11/2021
 */
public class MuiltidisciplinaryBuilder {

	private final static Logger logger = LoggerFactory.getLogger(MuiltidisciplinaryBuilder.class);


	public static Node getMorphModelDiscipline() throws Exception {

		// cxtn1 is a free contextion for a discipline dispatcher
		Block mdlDispatch = block(
			loop(condition(cxt -> (double)
				value(cxt, "morpher3") < 900.0), model("cxtn1")));

		Node morphDis = rnd("morphModelDisc",
			rndFi("mmd",
				cxtnFi("cxtn1", sig(MuiltidisciplinaryBuilder.class, "getMorphingModel")),
				dspFi(mdlDispatch)));

		return morphDis;
	}

	public static mog getMorphingModel() throws Exception {

		sig add = sig("add", Adder.class,
			result("y1", inPaths("arg/x1", "arg/x2")));
		sig subtract = sig("subtract", Subtractor.class,
			result("y2", inPaths("arg/x1", "arg/x2")));
		sig average = sig("average", Averager.class,
			result("y3", inPaths("arg/x1", "arg/x2")));
		sig multiply = sig("multiply", Multiplier.class,
			result("y4", inPaths("arg/x1", "arg/x2")));
		sig divide = sig("divide", Divider.class,
			result("y5", inPaths("arg/x1", "arg/x2")));

		mog t4 = task("t4",
			sig("multiply", MultiplierImpl.class,
				result("result/y", inPaths("arg/x1", "arg/x2"))));

		mog t5 = task("t5",
			sig("add", AdderImpl.class,
				result("result/y", inPaths("arg/x1", "arg/x2"))));

		Morpher morpher1 = (mgr, mFi, value) -> {
			Fidelity<Signature> fi = mFi.getFidelity();
			if (fi.getSelectName().equals("add")) {
				if (((Double) value) <= 200.0) {
					mgr.morph("sysFi2");
				} else {
					mgr.morph("sysFi3");
				}
			} else if (fi.getPath().equals("mFi1") && fi.getSelectName().equals("multiply")) {
				mgr.morph("sysFi3");
			}
		};

		Morpher morpher2 = (mgr, mFi, value) -> {
			Fidelity<Signature> fi = mFi.getFidelity();
			if (fi.getSelectName().equals("divide")) {
				if (((Double) value) <= 9.0) {
					mgr.morph("sysFi4");
				} else {
					mgr.morph("sysFi3");
				}
			}
		};

		Morpher morpher3 = (mgr, mFi, value) -> {
			Fidelity<Signature> fi = mFi.getFidelity();
			Double val = (Double) value;
			if (fi.getSelectName().equals("t5")) {
				if (val <= 200.0) {
					((EntryModel)mgr.getMogram()).putValue("morpher3", val + 10.0);
					mgr.reconfigure(fi("t4", "mFi4"));
				}
			} else if (fi.getSelectName().equals("t4")) {
				// t4 is a mutiply task
				((EntryModel)mgr.getMogram()).putValue("morpher3", val + 20.0);
			}
		};

		Morpher morpher4 = (mgr, mFi, value) -> {
			Fidelity<Signature> fi = mFi.getFidelity();
			if (fi.getSelectName().equals("divide")) {
				if (((Double) value) <= 9.0) {
					mgr.morph("sysFi5");
				} else {
					mgr.morph("sysFi3");
				}
			}
		};

		fi fi2 = metaFi("sysFi2", mphFi("ph4", "mFi2"), fi("divide", "mFi2"), fi("multiply", "mFi3"));
		fi fi3 = metaFi("sysFi3", fi("average", "mFi2"), fi("divide", "mFi3"));
		fi fi4 = metaFi("sysFi4", fi("average", "mFi3"));
		fi fi5 = metaFi("sysFi5", fi("t4", "mFi4"));

		// four entry multifidelity model with four morphers
		mog mdl = model(inVal("arg/x1", 90.0), inVal("arg/x2", 10.0), inVal("morpher3", 100.0),
			ent("mFi1", mphFi(morpher1, add, multiply)),
			ent("mFi2", mphFi(entFi(ent("ph2", morpher2), ent("ph4", morpher4)), average, divide, subtract)),
			ent("mFi3", mphFi(average, divide, multiply)),
			ent("mFi4", mphFi(morpher3, t5, t4)),
			fi2, fi3, fi4, fi5,
			Strategy.FidelityManagement.YES,
			response("mFi1", "mFi2", "mFi3", "mFi4", "arg/x1", "arg/x2", "morpher3"));

		return mdl;
	}

	static public Node getMultiFiPipelineDiscipline() throws Exception {

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

		Node plDisc = rnd("plDisc",
			rndFi("plDisc1",
				cxtnFi("cxtn1", sig("getPipeline1",  MuiltidisciplinaryBuilder.class)),
				cxtFi("cxt1", cxt1),
				dspFi("dspt1", evalTask)),
			rndFi("plDisc2",
				cxtnFi("cxtn2", sig("getPipeline2",  MuiltidisciplinaryBuilder.class)),
				cxtFi("cxt2", cxt2),
				dspFi("dspt2", blockDispatch)));

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

	static public Region getMultinodeRegion() throws Exception {

		Region region = rgn("multi-node",
			instance(sig("getMorphModelDiscipline", MuiltidisciplinaryBuilder.class)),
			instance(sig("getMultiFiPipelineDiscipline", MuiltidisciplinaryBuilder.class), fi("plDisc1")));

		return region;
	}

	static public Governance getMultidiscGovernance1() throws Exception {

		Governance govc = gov("multi-disc",
			rgn(instance(sig("getMorphModelDiscipline", MuiltidisciplinaryBuilder.class))),
			rgn(instance(sig("getMultiFiPipelineDiscipline", MuiltidisciplinaryBuilder.class), fi("plDisc1"))));

		return govc;
	}

	static public Governance getMultidiscGovernance2() throws Exception {

		Context govcCxt = context(mdaFi("multidiscMdaFi",
			mda("analyzer",
				(Requestor gov, Context cxt) -> {
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

		Governance mdisc = gov("multi-disc",
			rgn(instance(sig("getMorphModelDiscipline", MuiltidisciplinaryBuilder.class))),
			rgn(instance(sig("getMultiFiPipelineDiscipline", MuiltidisciplinaryBuilder.class), fi("plDisc1"))),
			govcCxt);

		return mdisc;
	}

}
