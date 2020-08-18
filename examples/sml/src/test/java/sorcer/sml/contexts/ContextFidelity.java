package sorcer.sml.contexts;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import static sorcer.ent.operator.fxn;
import static sorcer.so.operator.eval;
import static sorcer.so.operator.exec;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.arithmetic.provider.impl.*;
import sorcer.service.Projection;
import sorcer.service.*;
import sorcer.service.modeling.Model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sorcer.co.operator.*;
import static sorcer.ent.operator.*;
import static sorcer.eo.operator.*;
import static sorcer.mo.operator.*;
import static sorcer.so.operator.*;

/**
 * @author Mike Sobolewski
 */
@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/sml")
public class ContextFidelity {
	private final static Logger logger = LoggerFactory.getLogger(ContextFidelity.class);

	@Test
	public void multiPathLambdaValue() throws Exception {

		// the model execute a fxn lambda expression with no model state altered
		Model mdl = model(ent(pthFi(paths("x1", "arg/x1")), 10.0), ent("x2", 20.0),
			fxn(pthFi(paths("x3", "arg/x3")), (Model model) -> ent("x5", (double)exec(model, "x2") + 100.0)));

		logger.info("x3: " + eval(mdl, "x3"));
		assertEquals(120.0, exec(mdl));
		assertEquals(120.0, exec(mdl, pthFi("x3", "arg/x3")));
		assertEquals(120.0, exec(mdl, pthFi("arg/x3", "x3")));
	}

	@Test
	public void exertMultiFiContextTask() throws Exception  {

		Context cxt1 = context("cxt1",
			inVal("arg/x1", 20.0),
			inVal("arg/x2", 80.0));

		Context cxt2 = context("cxt2",
			inVal("arg/x1", 30.0),
			inVal("arg/x2", 90.0));

		Task t5 = task("t5", sig("add", AdderImpl.class),
				cmFi(cxt1, cxt2));

		Routine out = exert(t5);
		Context cxt = context(out);

		// getValue a single context argument
		assertEquals(100.0, value(cxt, "result/eval"));

		// getValue the subcontext output from the context
		assertTrue(context(ent("result/eval", 100.0), ent("arg/x1", 20.0)).equals(
			value(cxt, outPaths("result/eval", "arg/x1"))));

		out = exert(t5, cxtFi("cxt2"));
		cxt = context(out);

		// getValue a single context argument
		assertEquals(120.0, value(cxt, "result/eval"));

		// getValue the subcontext output from the context
		assertTrue(context(ent("result/eval", 120.0), ent("arg/x1", 30.0)).equals(
			value(cxt, outPaths("result/eval", "arg/x1"))));
	}

	@Test
	public void multiPathMultiFiContextTask() throws Exception  {

		Context cxt1 = context("cxt1",
			inVal(pthFi(paths("arg/x1", "arg/y1")), 20.0),
			inVal(pthFi(paths("arg/x2", "arg/y2")), 80.0));

		Context cxt2 = context("cxt2",
			inVal(pthFi(paths("arg/x1", "arg/y1")), 30.0),
			inVal(pthFi(paths("arg/x2", "arg/y2")), 90.0));

		Projection outPrj = outPthProj(pthFi("arg/x1", "arg/y1"), pthFi("arg/x2", "arg/y2"));

		Task t5 = task("t5", sig("add", AdderImpl.class),
			cmFi(cxt1, cxt2), outPrj);

		Routine out = exert(t5);
		Context cxt = context(out);

		// getValue a single context argument
		assertEquals(100.0, value(cxt, "result/eval"));

		// getValue the subcontext output from the context
		assertTrue(context(ent("result/eval", 100.0), ent("arg/y1", 20.0)).equals(
			value(cxt, outPaths("result/eval", "arg/y1"))));

		out = exert(t5, cxtFi("cxt2"));
		cxt = context(out);

		// getValue a single context argument
		assertEquals(120.0, value(cxt, "result/eval"));

		// getValue the subcontext output from the context
		assertTrue(context(ent("result/eval", 120.0), ent("arg/y1", 30.0)).equals(
			value(cxt, outPaths("result/eval", "arg/y1"))));
	}
}
	
	
