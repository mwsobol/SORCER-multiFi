package sorcer.pml.modeling;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.core.context.model.ent.Pcr;
import sorcer.ent.operator;
import sorcer.service.Context;
import sorcer.service.modeling.*;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sorcer.co.operator.*;
import static sorcer.eo.operator.*;
import static sorcer.ent.operator.*;
import static sorcer.mo.operator.*;
import static sorcer.so.operator.*;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/pml")
public class ProceduralCalls {
	private final static Logger logger = LoggerFactory.getLogger(ProceduralCalls.class.getName());

	@Test
	public void pcrScope() throws Exception {
		// a pcr is a variable (entry) evaluated with its own scope (context)
		Context<Double> cxt = context(val("x", 20.0), val("y", 30.0));

		// pcr with its context scope
		Pcr add = pcr("add", invoker("x + y", args("x", "y")), cxt);
		logger.info("pcr eval: " + exec(add));
		assertTrue(exec(add).equals(50.0));
	}


	@Test
	public void modelScope() throws Exception {

		Model mdl = model(pcr("x", 20.0), pcr("y", 30.0));
		Pcr add = pcr("add", invoker("x + y", args("x", "y")), mdl);

		// adding a pcr to the model updates pcr's scope
		add(mdl, add);

		// evaluate entry of the context
		logger.info("eval add : " + exec(mdl, "add"));
		assertTrue(exec(mdl, "add").equals(50.0));

	}

	@Test
	public void closingpcrWihEntries() throws Exception {
		Pcr y = pcr("y",
			invoker("(x1 * x2) - (x3 + x4)", args("x1", "x2", "x3", "x4")));
		Object val = exec(y, val("x1", 10.0), val("x2", 50.0), val("x3", 20.0), val("x4", 80.0));
		// logger.info("y eval: " + val);
		assertEquals(val, 400.0);
	}

	@Test
	public void closingpcrWitScope() throws Exception {

		// invokers use contextual scope of args
		Pcr add = pcr("add", invoker("x + y", args("x", "y")));

		Context<Double> cxt = context(val("x", 10.0), val("y", 20.0));
		logger.info("pcr eval: " + exec(add, cxt));
		// compute a pcr
		assertTrue(exec(add, cxt).equals(30.0));

	}

	@Test
	public void dbpcrOperator() throws Exception {

		Pcr<Double> dbp1 = persistent(pcr("design/in", 25.0));
		Pcr<String> dbp2 = dbEnt("url/sobol", "http://sorcersoft.org/sobol");

		// dbp1 is declared to be persisted
		assertTrue(dbp1.getOut().equals(25.0));
		assertEquals(dbp1.getOut().getClass(), Double.class);
		// dbp2 is persisted already
		assertEquals(impl(dbp2).getClass(), URL.class);

		URL dbp1Url = storeVal(dbp1);
		URL dbp2Url = (URL) impl(dbp2);

		assertTrue(content(dbp1Url).equals(25.0));
		assertEquals(content(dbp2Url), "http://sorcersoft.org/sobol");

		assertTrue(exec(dbp1).equals(25.0));
		assertEquals(exec(dbp2), "http://sorcersoft.org/sobol");

		// TODO update does not occur
		// update persistent values
		setValue(dbp1, 30.0);
		setValue(dbp2, "http://sorcersoft.org");

		logger.info("dbp1: " + exec(dbp1));
		logger.info("dbp2: " + exec(dbp2));

		assertTrue(exec(dbp1).equals(30.0));
		assertTrue(exec(dbp2).equals("http://sorcersoft.org"));

		assertEquals(asis(dbp1).getClass(), URL.class);
		assertEquals(asis(dbp2).getClass(), URL.class);
	}

	@Test
	public void substitutingValuesWithEntFidelities() throws Exception {

		Pcr<Double> dbp = dbEnt("shared/eval", 25.0);

		Pcr multi = pcr("multi",
			entFi(val("init/eval"),
				dbp,
				pcr("invoke", invoker("x + y", args("x", "y")))));

		Context cxt = context(val("x", 10.0),
			val("y", 20.0), val("init/eval", 49.0));

		setValue(dbp, 50.0);
		assertTrue(exec(dbp).equals(50.0));
		assertTrue(exec(multi, cxt, fi("shared/eval")).equals(50.0));
		assertTrue(exec(multi, cxt, fi("init/eval")).equals(49.0));
		assertTrue(exec(multi, cxt, fi("invoke")).equals(30.0));
	}

	@Test
	public void pcrModelOperator() throws Exception {

		Model mdl = entModel("pcr-model", val("v1", 1.0), val("v2", 2.0));
		add(mdl, val("x", 10.0), val("y", 20.0));
		// add an active pcr, no scope
		add(mdl, operator.pcr(invoker("add1", "x + y", args("x", "y"))));
		// add a pcr with own scope
		add(mdl, pcr(invoker("add2", "x + y", args("x", "y")),
			context(val("x", 30.0), val("y", 40.0))));

		assertEquals(exec(mdl, "add1"), 30.0);
		// change the scope of add1
		setValue(mdl, "x", 20.0);
		assertEquals(exec(mdl, "add1"), 40.0);

		assertEquals(exec(mdl, "add2"), 70.0);
		// x is changed but add2 eval is the same, has its own scope
		setValue(mdl, "x", 20.0);
		assertEquals(exec(mdl, "add2"), 70.0);
	}

	@Test
	public void arguments() throws Exception {
		Args as = args("span/filter", "span/url", ent("span", 10.0));
		Context cxt = context(as);
		logger.info("cxt " + cxt);
		assertEquals(value(cxt, "span/filter"), Context.none);
		assertEquals(value(cxt, "span"), 10.0);
		assertEquals(asis(cxt, "span"), val("span", 10.0));
	}
}
