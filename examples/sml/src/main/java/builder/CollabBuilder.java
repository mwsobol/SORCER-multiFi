package builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.arithmetic.provider.impl.*;
import sorcer.core.service.Collaboration;
import sorcer.service.*;
import sorcer.service.modeling.Model;

import static sorcer.co.operator.*;
import static sorcer.ent.operator.*;
import static sorcer.eo.operator.alt;
import static sorcer.eo.operator.args;
import static sorcer.eo.operator.*;
import static sorcer.eo.operator.fi;
import static sorcer.eo.operator.result;
import static sorcer.mo.operator.*;
import static sorcer.mo.operator.value;
import static sorcer.so.operator.*;

/**
 * @author Mike Sobolewski
 */
public class CollabBuilder {
	private final static Logger logger = LoggerFactory.getLogger(CollabBuilder.class);
	private Morpher mFi1Morpher;

	public static Model getEntryModel() throws Exception {

		Model mdl = model("entModel",
			val("x1", 10.0d), val("x2", 50.0d),
			val("x3", 20.0d), val("x4", 80.0d),
			ent("y1", expr("x1 * x2", args("x1", "x2"))),
			ent("y2", expr("x3 + x4", args("x3", "x4"))),
			ent("y3", expr("y1 - y2", args("y1", "y2"))),
			response("y1", "y2", "y3"));

		return mdl;
	}


	public static Routine getArithmeticBlock() throws Exception {

		Task t3 = task("t3", sig("subtract", SubtractorImpl.class),
			context("subtract", inVal("arg/t4"), inVal("arg/t5"),
				result("block/result", Signature.Direction.OUT)));

		Task t4 = task("t4", sig("multiply", MultiplierImpl.class),
			context("multiply", inVal("arg/x1"), inVal("arg/x2"),
				result("arg/t4", Signature.Direction.IN)));

		Task t5 = task("t5", sig("add", AdderImpl.class),
			context("add", inVal("arg/x3"), inVal("arg/x4"),
				result("arg/t5", Signature.Direction.IN)));

		Block block = block("srvBlock", block(t4, t5), t3,
			context(inVal("arg/x1", 10.0), inVal("arg/x2", 50.0),
				inVal("arg/x3", 20.0), inVal("arg/x4", 80.0)));

		return block;
	}

	public static Model getAmorphousModel() throws Exception {

		Morpher mFi1Morpher =  (mgr, mFi, value) -> {
			Fidelity fi =  mFi.getFidelity();
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

		Morpher mFi2Morpher = (mgr, mFi, value) -> {
			Fidelity<Signature> fi =  mFi.getFidelity();
			if (fi.getSelectName().equals("divide")) {
				if (((Double) value) <= 9.0) {
					mgr.morph("sysFi4");
				} else {
					mgr.morph("sysFi3");
				}
			}
		};

		Metafidelity fi2 = metaFi("sysFi2",fi("divide", "mFi2"), fi("multiply", "mFi3"));
		Metafidelity fi3 = metaFi("sysFi3", fi("average", "mFi2"), fi("divide", "mFi3"));
		Metafidelity fi4 = metaFi("sysFi4", fi("average", "mFi3"));

		Signature add = sig("add", AdderImpl.class,
			result("result/y1", inPaths("arg/x1", "arg/x2")));
		Signature subtract = sig("subtract", SubtractorImpl.class,
			result("result/y2", inPaths("arg/x1", "arg/x2")));
		Signature average = sig("average", AveragerImpl.class,
			result("result/y2", inPaths("arg/x1", "arg/x2")));
		Signature multiply = sig("multiply", MultiplierImpl.class,
			result("result/y1", inPaths("arg/x1", "arg/x2")));
		Signature divide = sig("divide", DividerImpl.class,
			result("result/y2", inPaths("arg/x1", "arg/x2")));

		// multifidelity model with morphers
		Model mod = model(inVal("arg/x1", 90.0), inVal("arg/x2", 10.0),
			ent("mFi1", mphFi(mFi1Morpher, add, multiply)),
			ent("mFi2", mphFi(mFi2Morpher, average, divide, subtract)),
			ent("mFi3", mphFi(average, divide, multiply)),
			fi2, fi3, fi4,
			response("mFi1", "mFi2", "mFi3", "arg/x1", "arg/x2"));

		return mod;

//		Context out = response(mod);
//		logger.info("out: " + out);
//		assertTrue(get(out, "mFi1").equals(100.0));
//		assertTrue(get(out, "mFi2").equals(9.0));
//		assertTrue(get(out, "mFi3").equals(50.0));

	}

	public static Collaboration getArithmeticColab1() throws Exception {

		Collaboration collab = clb("domainCollab",
			instance(sig("getEntryModel", CollabBuilder.class)),
			instance(sig("getArithmeticBlock", CollabBuilder.class)));

		return collab;
	}

	public static Collaboration getArithmeticColab2() throws Exception {

		Context collabCxt = context(explFi("explorer",
			expl("explorer1",
				(Context cxt) -> {
					double z1, z2;
					String clbName = clb(cxt);
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
						double y1, y2, y3, y4;
						String dmnName = dmn(cxt);
						if (dmnName.equals("srvBlock")) {
							y1 = (double)value(cxt, "arg/x1");
							y2 = (double)value(cxt, "arg/x2");
							y3 = (double)value(cxt, "arg/t5");
							setValue(collab, "z1", (y3 * y2) / y1);
						} else if (dmnName.equals("entModel")) {
							y4 = (double)value(cxt, "block/result");
							setValue(collab, "z2", y4 * 2.4);
						}
					}))));

		Collaboration collab = clb("domainCollab",
			instance(sig("getEntryModel", CollabBuilder.class)),
			instance(sig("getArithmeticBlock", CollabBuilder.class)),
			collabCxt);

		return collab;
	}
}
	
	
