package sorcer.pml.modeling;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.arithmetic.provider.impl.AdderImpl;
import sorcer.arithmetic.provider.impl.MultiplierImpl;
import sorcer.arithmetic.provider.impl.SubtractorImpl;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.core.context.model.ent.Pcr;
import sorcer.core.invoker.*;
import sorcer.core.provider.rendezvous.ServiceJobber;
import sorcer.service.*;
import sorcer.service.modeling.Model;
import sorcer.service.modeling.ent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sorcer.co.operator.*;
import static sorcer.ent.operator.*;
import static sorcer.ent.operator.mfEval;
import static sorcer.eo.operator.args;
import static sorcer.eo.operator.*;
import static sorcer.eo.operator.pipe;
import static sorcer.mo.operator.*;
import static sorcer.mo.operator.add;
import static sorcer.so.operator.*;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/pml")
public class Invokers {
	private final static Logger logger = LoggerFactory.getLogger(Invokers.class);

	private EntryModel em;
	private ent x;
	private ent y;
	private ent z;

	/// member subclass of Updater with Context parameter used below with
	// contextMethodAttachmentWithArgs()
	// there are constructor's context and invoke metod's context as parameters
	public class ContextUpdater extends Updater {
		public ContextUpdater(Context context) {
			super(context);
		}

		public Double update(Context arg) throws Exception {
			setValue(x, value(arg, "x"));
			setValue(y, value(context, "y"));
			// x set from 'arg'
			assertTrue(exec(x).equals(200.0));
			// y set from construtor's context 'in'
			assertTrue(exec(y).equals(30.0)); //-10?
			assertTrue(exec(z).equals(170.0));
			return (double)exec(x) + (double)exec(y) + (double)exec(em, "z");
		}
	};

	@Before
	public void initEntModel() throws Exception {
		em = new EntryModel();
		//force for x and y procedural entries
		x = pcr("x", 10.0);
		y = pcr("y", 20.0);
		z = ent("z", invoker("x - y", args("x", "y")));
	}

	@Test
	public void lambdaInvoker() throws Exception {

		Model mo = model(val("x", 10.0), val("y", 20.0),
				ent(invoker("fxn",
					(Context<Double> cxt) -> value(cxt, "x") + value(cxt, "y") + 30,
					args("x", "y"))));
//		logger.info("invoke eval: " + eval(mo, "fxn"));
		assertEquals(exec(mo, "fxn"), 60.0);
	}

	@Test
	public void objectMethodInvoker() throws Exception {
		setValue(x, 10.0);
		setValue(y, 20.0);
		add(em, x, y, z);

//		logger.info("x:" + eval(em, "x"));
//		logger.info("y:" + eval(em, "y"));
//		logger.info("y:" + eval(em, "z"));

		Context in = context(val("x", 20.0), val("y", 30.0));
		Context arg = context(val("x", 200.0), val("y", 300.0));
		add(em, methodInvoker("update", new ContextUpdater(in), arg));
		logger.info("exec:" + exec(em, "update"));
		assertEquals(exec(em, "update"), 400.0);
	}

	@Test
	public void groovyInvoker() throws Exception {
		EntryModel pm = entModel("ent-model");
		add(pm, val("x", 10.0), val("y", 20.0));
		add(pm, invoker("expr", "x + y + 30", args("x", "y")));
		logger.info("invoke eval: " + invoke(pm, "expr"));
		assertEquals(invoke(pm, "expr"), 60.0);
		logger.info("getValue eval: " + eval(pm, "expr"));
		assertTrue(exec(pm, "expr").equals(60.0));
	}

	@Test
	public void serviceNeurons() throws Exception {
		Model nm = snrModel("neural-model");
		add(nm, snr("x1", 10.0), snr("x2", 20.0));
		add(nm, snr("x3", weights(val("x1", 2.0), val("x2", 10.0)), signals("x1", "x2")));

//        logger.info("activate x1: " + activate(em, "x1"));
        assertEquals(activate(nm, "x1"), 10.0);

//        logger.info("activate x3: " + activate(em, "x3"));
        assertEquals(activate(nm, "x3"), 220.0);

//        logger.info("activate x3: " + activate(em, "x3", th("x3", 200.0)));
        assertEquals(activate(nm, "x3", th("x3", 200.0)), 1.0);

//        logger.info("activate x3: " + activate(em, "x3", th("x3", 0.0), bias("x3", 50.0)));
        assertEquals(activate(nm, "x3", th("x3", 0.0), bias("x3", 50.0)), 270.0);
	}

	@Test
	public void serviceNeuronFidelities() throws Exception {
		Model nm = model("neural-model",
			snr("x1", 10.0), snr("x2", 20.0),
			snr("x3", weights(val("x1", 2.0), val("x2", 5.0)), signals("x1", "x2")),
			snr("x4", mnFi(
					nFi("n1", signals("x1", "x2"), weights(val("x1", 1.5), val("x2", 10.0))),
					nFi("n2", signals("x1", "x2"), weights(val("x1", 2.0), val("x2", 12.0))))));

//      logger.info("activate1 x4: " + activate(nm, "x4", metaFi("x4", "n1")));
		assertEquals(activate(nm, "x4", fi("n1", "x4")), 215.0);

//		logger.info("activate2 x4: " + activate(em, "x4", th("x4", 200.0), metaFi("x4", "n1")));
		assertEquals(activate(nm, "x4", th("n1", 200.0), fi("n1", "x4")), 1.0);

//      logger.info("activate3 x4: " + activate(em, "x4", th("x4", 0.0), metaFi("x4", "n2")));
        assertEquals(activate(nm, "x4", th("n2", 0.0), fi("n2", "x4")), 260.0);
	}

    @Test
    public void multiFiEvaluator() throws Exception {

        Evaluator mfeval = mfEval(
                invoker("fxn",
                        (Context<Double> cxt) -> value(cxt, "x") + value(cxt, "y") + 30,
                        args("x", "y")),
                invoker("expr", "x - y", args("x", "y")));

        setContext(mfeval, context("mfpcr",
                inVal("x", 20.0),
                inVal("y", 80.0),
                result("result/z")));

//        logger.info("fxn: " + exec(mfeval));
        assertEquals(130.0, exec(mfeval));
        // change signature fidelity
//        logger.info("expr: " + exec(mfeval, fi("expr")));
        assertEquals(-60.0, exec(mfeval, fi("expr")));

    }

    @Test
    public void execProcedure() throws Exception {

	    // constant entry
        ent x1 = ent("x1", 1.0);
        assertEquals(exec(x1), 1.0);

        // procedural entry
        Context cxt = context(val("x", 10.0), val("y", 20.0));

        Pcr p1 = pcr(invoker("expr", "x + y + 30", args("x", "y")));
//        logger.info("expr: " + exec(p1, cxt));
        assertEquals(exec(p1, cxt), 60.0);

        // invoke for a given state
        p1 = pcr(invoker("expr", "x + y + 30", args("x", "y")));
        setContext(p1, cxt);
        assertEquals(exec(p1), 60.0);
    }

	@Test
	public void multiFiProcedure() throws Exception {

		Pcr mfpcr = mfpcr(
			invoker("fxn",
				(Context<Double> cxt) -> value(cxt, "x") + value(cxt, "y") + 30,
				args("x", "y")),
			invoker("expr", "x - y", args("x", "y")));

        Context data = context("mfpcr",
                inVal("x", 20.0),
                inVal("y", 80.0),
                result("result/z"));

//		setContext(mfpcr, data);

		assertEquals(130.0, exec(mfpcr, data));
		// invalidate output and change signature fidelity
		invalid(mfpcr);
        assertEquals(-60.0, exec(mfpcr, fi("expr")));
	}

	@Test
	public void invokeContext() throws Exception {
		y = ent("y", invoker("x1 + x2", args("x1", "x2")));
		Object val = exec(y, context(val("x1", 10.0), val("x2", 20.0)));
		logger.info("y: " + val);
		assertTrue(val.equals(30.0));
	}

	@Test
	public void invokerDoublesDecimals() throws Exception {
		y = ent("y", invoker("x1 + x2", args("x1", "x2")));
		Object val = exec(y, context(val("x1", 10.0), val("x2", 20.0)));
		logger.info("y: " + val);
		assertTrue(val.equals(30.0));

		z = ent("z", invoker("x1 = 10.0d; x2 = 20.0d; x1 + x2"));
		val = exec(z);
		logger.info("z: " + val);
		assertTrue(val.equals(30.0));
	}

	@Test
	public void invokerOfInvokers() throws Exception {

		y = ent("y", invoker("import sorcer.core.context.model.ent.Entry;"
			+ " import static sorcer.so.operator.exec;"

			+ " Entry z = ent(invoker('x0 = x1 = 10.0d; x2 = 20.0d; x1 + x2'));"
			+ " def zv = exec(z);"
			+ " zv + 10.0d"));

		Object val = exec(y);
		logger.info("y: " + val);
		assertTrue(val.equals(40.0));
	}

	@Test
	public void substituteInvokeArgs() throws Exception {
		ent x1, x2, y;

		x1 = ent("x1", 1.0);
		x2 = ent("x2", 2.0);
		y = ent("y", invoker("x1 + x2", x1, x2));
		
		logger.info("y: " + exec(y));
		assertTrue(exec(y).equals(3.0));

		Object val = exec(y, context(val("x1", 10.0), val("x2", 20.0)));
		logger.info("y: " + val);
		assertTrue(val.equals(30.0));
	}

	static public Mogram getMogram() throws Exception {
		Context c4 = context("multiply", inVal("arg/x1", 50.0),
			inVal("arg/x2", 10.0), result("result/y"));
		Context c5 = context("add", inVal("arg/x1", 20.0), inVal("arg/x2", 80.0),
			result("result/y"));

		// domains
		Task t3 = task(
			"t3",
			sig("subtract", SubtractorImpl.class),
			context("subtract", inVal("arg/x1"), inVal("arg/x2"), outVal("result/y")));
		Task t4 = task("t4", sig("multiply", MultiplierImpl.class), c4);
		Task t5 = task("t5", sig("add", AdderImpl.class), c5);

		Job j1 = job("j1", sig("exert", ServiceJobber.class),
			job("j2", t4, t5, sig("exert", ServiceJobber.class)), t3,
			pipe(outPoint(t4, "result/y"), inPoint(t3, "arg/x1")),
			pipe(outPoint(t5, "result/y"), inPoint(t3, "arg/x2")),
			result("j1/t3/result/y"));

		return j1;
	}

    @Test
	public void modelConditions() throws Exception {
		final EntryModel pm = new EntryModel("ent-model");
		pm.putValue("x", 10.0);
		pm.putValue("y", 20.0);
		pm.putValue("condition", invoker("x > y", args("x", "y")));

		assertEquals(pm.getValue("x"), 10.0);
		assertEquals(pm.getValue("y"), 20.0);
		// logger.info("condition eval: " + em.execute("condition"));
		assertEquals(exec(pm,"condition"), false);

//		pm.putValue("x", 300.0);
//		pm.putValue("y", 200.0);
//		logger.info("condition eval: " + pm.getValue("condition"));
//		assertEquals(pm.getValue("condition"), true);
//
//		// enclosing class conditional context
//		Condition c = new Condition() {
//			@Override
//			public boolean isTrue() throws ContextException {
//				return (Boolean) pm.getValue("condition");
//			}
//		};
//		assertEquals(c.isTrue(), true);
//
//		// provided conditional context
//		Condition eval = new Condition(pm) {
//			@Override
//			public boolean isTrue() throws ContextException {
//				try {
//					return (Boolean) conditionalContext.getValue("condition");
//				} catch (RemoteException e) {
//					throw new ContextException(e);
//				}
//			}
//		};
//		assertEquals(eval.evaluate(), true);
	}

	@Test
	public void optInvoker() throws Exception {
		EntryModel pm = new EntryModel("ent-model");

		OptInvoker opt = new OptInvoker("opt", new Condition(pm,
				"{ x, y -> x > y }", "x", "y"), 
					invoker("x + y", args("x", "y")));

		pm.add(opt);
		pm.putValue("x", 10.0);
		pm.putValue("y", 20.0);

		logger.info("x: " + value(pm, "x"));
		logger.info("y: " + value(pm, "y"));
		logger.info("opt" + value(pm, "opt"));
		
		assertEquals(opt.evaluate(), null);

		pm.putValue("x", 300.0);
		pm.putValue("y", 200.0);
		logger.info("opt eval: " + opt.evaluate());
		assertEquals(opt.evaluate(), 500.0);
	}

	@Test
	public void createOptInvoker() throws Exception {
		EntryModel pm = entModel("ent-model");
		add(pm,
				val("x", 10.0),
				val("y", 20.0),
				opt("opt", condition(pm, "{ x, y -> x > y }", "x", "y"),
						invoker("x + y", args("x", "y"))));

		logger.info("opt eval: " + exec(pm, "opt"));
		assertEquals(exec(pm, "opt"), null);

		setValues(pm, val("x", 300.0), val("y", 200.0));
		logger.info("opt eval: " + exec(pm, "opt"));
        assertTrue(exec(pm, "opt").equals(500.0));
	}

	@Test
	public void altInvoker() throws Exception {
		EntryModel pm = new EntryModel("ent-model");
		pm.putValue("x", 30.0);
		pm.putValue("y", 20.0);
		pm.putValue("x2", 50.0);
		pm.putValue("y2", 40.0);
		pm.putValue("x3", 70.0);
		pm.putValue("y3", 60.0);

		Evaluator opt1 = opt("opt1", condition(pm,
				"{ x, y -> x > y }", "x", "y"), invoker("x + y + 10",
					args("x", "y")));

		Evaluator opt2 = opt("opt2", condition(pm,
				"{ x2, y2 -> x2 > y2 }", "x2", "y2"), invoker(
				"x + y + 20", args("x", "y")));

		Evaluator opt3 = opt("op3", condition(pm,
				"{ x3, y3 -> x3 > y3 }", "x3", "y3"), invoker(
				"x + y + 30", args("x", "y")));

		// no condition means condition(true)
		Evaluator opt4 = opt("opt4", invoker("x + y + 40",
				args("x", "y")));

		AltInvoker alt = alt("alt", opt1, opt2, opt3, opt4);
		add(pm, opt1, opt2, opt3, opt4, alt);

		logger.info("opt1 exec: " + exec(opt1));
		assertEquals(exec(opt1), 60.0);
		logger.info("opt2 exec: " + exec(opt2));
		assertEquals(exec(opt2), 70.0);
		logger.info("opt3 exec: " + exec(opt3));
		assertEquals(exec(opt3), 80.0);
		logger.info("opt4 exec: " + exec(opt4));
		assertEquals(exec(opt4), 90.0);
		logger.info("alt exec: " + exec(alt));
		assertEquals(exec(alt), 60.0);

		pm.putValue("x", 300.0);
		pm.putValue("y", 200.0);
		logger.info("opt exec: " + exec(alt));
		assertEquals(exec(alt), 510.0);

		pm.putValue("x", 10.0);
		pm.putValue("y", 20.0);
		pm.putValue("x2", 40.0);
		pm.putValue("y2", 50.0);
		pm.putValue("x3", 50.0);
		pm.putValue("y3", 60.0);
		logger.info("opt exec: " + alt.evaluate());
		assertEquals(exec(alt), 70.0);

		pm.putValue("x2", 50.0);
		pm.putValue("y2", 40.0);
		logger.info("opt exec: " + alt.evaluate());
		assertEquals(exec(alt), 50.0);
	}

	@Test
	public void smlAltInvoker() throws Exception {
		EntryModel pm = entModel("ent-model");
		add(pm, val("x", 10.0), val("y", 20.0), val("x2", 50.0),
				val("y2", 40.0), val("x3", 50.0), val("y3", 60.0));

		AltInvoker alt = alt(
				"alt",
				opt("opt1", condition(pm, "{ x, y -> x > y }", "x", "y"),
						invoker("x + y + 10", args("x", "y"))),
				opt("opt2", condition(pm, "{ x2, y2 -> x2 > y2 }", "x2", "y2"),
						invoker("x + y + 20", args("x", "y"))),
				opt("opt3", condition(pm, "{ x3, y3 -> x3 > y3 }", "x3", "y3"),
						invoker("x + y + 30", args("x", "y"))),
				opt("opt4", invoker("x + y + 40", args("x", "y"))));

		add(pm, alt, get(alt, 0), get(alt, 1), get(alt, 2), get(alt, 3));

		logger.info("opt1 exec : " + exec(pm, "opt1"));
		assertEquals(exec(pm, "opt1"), null);
		logger.info("opt2 exec: " + exec(pm, "opt2"));
        assertTrue(exec(pm, "opt2").equals(50.0));
		logger.info("opt3 exec: " + exec(pm, "opt3"));
		assertEquals(exec(pm, "opt3"), null);
		logger.info("opt4 exec: " + exec(pm, "opt4"));
        assertTrue(exec(pm, "opt4").equals(70.0));
		logger.info("alt exec: " + exec(alt));
		assertEquals(exec(alt), 50.0);

		setValues(pm, val("x", 300.0), val("y", 200.0));

		assertEquals(exec(alt), 510.0);

		setValues(pm, val("x", 10.0), val("y", 20.0), val("x2", 40.0),
				val("y2", 50.0), val("x3", 50.0), val("y3", 60.0));

		assertEquals(exec(alt), 70.0);
	}

	@Test
	public void invokerLoop() throws Exception {
		EntryModel pm = entModel("ent-model");
		add(pm, val("x", 1));
		add(pm, ent("y", invoker("x + 1", args("x"))));
		add(pm, ent("z", inc(invoker(pm, "y"), 2)));
		Invocation z2 = invoker(pm, "z");

		ServiceInvoker iloop = loop("iloop", condition(pm, "{ z -> z < 50 }", "z"), z2);
		add(pm, iloop);
		assertEquals(exec(pm, "iloop"), 48);
	}

	@Test
	public void incrementorStepBy1() throws Exception {
		EntryModel pm = entModel("ent-model");
		add(pm, val("x", 1));
		add(pm, ent("y", invoker("x + 1", args("x"))));
		add(pm, ent("z", inc(invoker(pm, "y"))));
		for (int i = 0; i < 10; i++) {
			logger.info("" + value(pm, "z"));
		}
        assertTrue(exec(pm, "z").equals(13));
	}

	@Test
	public void incrementorStepBy2() throws Exception {
		EntryModel pm = entModel("ent-model");
		add(pm, val("x", 1));
		add(pm, ent("y", invoker("x + 1", args("x"))));
		add(pm, ent("z", inc(invoker(pm, "y"), 2)));

		for (int i = 0; i < 10; i++) {
			logger.info("" + value(pm, "z"));
		}
		assertEquals(exec(pm, "z"), 24);
	}

	@Test
	public void incrementorDouble() throws Exception {
		EntryModel pm = entModel("ent-model");
		add(pm, ent("x", 1.0));
		add(pm, ent("y", invoker("x + 1.2", args("x"))));
		add(pm, ent("z", inc(invoker(pm, "y"), 2.1)));

		for (int i = 0; i < 10; i++) {
			logger.info("" + next(pm, "z"));
		}
		// logger.info("" + exec(em,"y++2.1"));
		assertEquals(exec(pm, "z"), 25.300000000000004);
	}
}
