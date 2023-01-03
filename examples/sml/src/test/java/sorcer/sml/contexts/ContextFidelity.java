package sorcer.sml.contexts;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.service.Morpher;
import sorcer.service.*;
import sorcer.service.modeling.Model;

import static org.junit.Assert.assertTrue;
import static sorcer.eo.operator.*;
import static sorcer.mo.operator.*;
import static sorcer.ent.operator.*;
import static sorcer.so.operator.*;

/**
 * @author Mike Sobolewski
 */
@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/sml")
public class ContextFidelity {
	private final static Logger logger = LoggerFactory.getLogger(ContextFidelity.class);

	@Test
	public void cxtFiLambdaModel() throws Exception {

		Context cxt1 = context("cxt1",
			ent("multiply/x1", 10.0), ent("multiply/x2", 50.0),
			ent("add/x1", 20.0), ent("add/x2", 80.0));

		Context cxt2 = context("cxt2",
			ent(pthFis("multiply/x1", "m-z1"), 11.0), ent(pthFis("multiply/x2", "m-z2"), 51.0),
			ent(pthFis("add/x1", "a-z3"), 21.0), ent(pthFis("add/x2", "a-z4"), 81.0));

		Context cxt3 = context("cxt3",
			ent(pthFis("m-z1", "multiply/x1"), 10.0), ent(pthFis("m-z2", "multiply/x2"), 50.0),
			ent(pthFis("a-z3", "add/x1"), 20.0), ent(pthFis("a-z4", "add/x2"), 80.0));

		Model mdl = model(
			srv("add", (Context<Double> model) ->
				v(model, "add/x1") + v(model, "add/x2")),
			srv("multiply", (Context<Double> model) ->
				v(model, "multiply/x1") * v(model, "multiply/x2")),
			srv("subtract", (Context<Double> model) ->
				v(model, "multiply") - v(model, "add")),

			cxtFis(cxt1, cxt2, cxt3),
			cxtPrj("cxtPrj1", cxtFi("cxt1")),
			cxtPrj("cxtPrj2", cxtFi("cxt2"),
				outProj(fromTo("multiply/x1", "m-z1"), fromTo("multiply/x2", "m-z2"),
					fromTo("add/x1", "a-z3"), fromTo("add/x2", "a-z4"))),
			cxtPrj("cxtPrj3", cxtFi("cxt3"),
				inProj(fromTo("m-z1", "multiply/x1"), fromTo("m-z2", "multiply/x2"),
					fromTo("a-z3", "add/x1"), fromTo("a-z4", "add/x2"))),
			response("subtract", "multiply", "add"));

		logger.info("DEPS: " + printDeps(mdl));
		// defult configuration
		Context out = response(mdl);
		logger.info("model response: " + out);
		assertTrue(get(out, "subtract").equals(400.0));
		assertTrue(get(out, "multiply").equals(500.0));
		assertTrue(get(out, "add").equals(100.0));

		// select context fidelity
		out = response(mdl, cxtFi("cxt2"));
		logger.info("model response: " + out);

		assertTrue(get(out, "subtract").equals(459.0));
		assertTrue(get(out, "multiply").equals(561.0));
		assertTrue(get(out, "add").equals(102.0));

		// morph with projection cxtPrj2
		out = response(mdl, prjFi("cxtPrj2"));
		logger.info("model response: " + out);

		assertTrue(get(out, "subtract").equals(459.0));
		assertTrue(get(out, "multiply").equals(561.0));
		assertTrue(get(out, "add").equals(102.0));

		assertTrue(get(mdl, "m-z1").equals(11.0));
		assertTrue(get(mdl, "a-z3").equals(21.0));

		// morph with projection cxtPrj3
		out = response(mdl, prjFi("cxtPrj3"));
		logger.info("model response: " + out);
		assertTrue(get(out, "subtract").equals(400.0));
		assertTrue(get(out, "multiply").equals(500.0));
		assertTrue(get(out, "add").equals(100.0));

		assertTrue(get(mdl, "multiply/x1").equals(10.0));
		assertTrue(get(mdl, "add/x1").equals(20.0));
	}

	@Test
	public void morphCxtFiLambdaModel() throws Exception {

		Context cxt1 = context("cxt1",
			ent("multiply/x1", 10.0), ent("multiply/x2", 50.0),
			ent("add/x1", 20.0), ent("add/x2", 80.0));

		Context cxt2 = context("cxt2",
			ent(pthFis("multiply/x1", "m-z1"), 11.0), ent(pthFis("multiply/x2", "m-z2"), 51.0),
			ent(pthFis("add/x1", "a-z3"), 21.0), ent(pthFis("add/x2", "a-z4"), 81.0));

		Context cxt3 = context("cxt3",
			ent(pthFis("m-z1", "multiply/x1"), 10.0), ent(pthFis("m-z2", "multiply/x2"), 50.0),
			ent(pthFis("a-z3", "add/x1"), 20.0), ent(pthFis("a-z4", "add/x2"), 80.0));

		Morpher morpher = (mgr, mFi, value) -> {
			String prjName = mFi.getName();
			Model mdl = (Model) value;
			if (!mdl.isValid() && prjName.equals("cxtPrj1")) {
				setValue(mdl, "multiply/x1", 13.0);
			} else if (mdl.isValid() && prjName.equals("cxtPrj1")) {
				mgr.morph("cxtPrj2");
			} else if (mdl.isValid() && prjName.equals("cxtPrj2")) {
				mgr.morph("cxtPrj3");
			}
		};

		Model mdl = model(
			srv("add", (Context<Double> model) ->
				v(model, "add/x1") + v(model, "add/x2")),
			srv("multiply", (Context<Double> model) ->
				v(model, "multiply/x1") * v(model, "multiply/x2")),
			srv("subtract", (Context<Double> model) ->
				v(model, "multiply") - v(model, "add")),

			cxtFis(morpher, cxt1, cxt2, cxt3),
			cxtPrj("cxtPrj1", cxtFi("cxt1")),
			cxtPrj("cxtPrj2", cxtFi("cxt2"),
				outProj(fromTo("multiply/x1", "m-z1"), fromTo("multiply/x2", "m-z2"),
					fromTo("add/x1", "a-z3"), fromTo("add/x2", "a-z4"))),
			cxtPrj("cxtPrj3", cxtFi("cxt3"),
				inProj(fromTo("m-z1", "multiply/x1"), fromTo("m-z2", "multiply/x2"),
					fromTo("a-z3", "add/x1"), fromTo("a-z4", "add/x2"))),
			response("subtract", "multiply", "add"));

		logger.info("DEPS: " + printDeps(mdl));
		// defult configuration
		Context out = response(mdl);
		logger.info("model response: " + out);
		assertTrue(get(out, "subtract").equals(550.0));
		assertTrue(get(out, "multiply").equals(650.0));
		assertTrue(get(out, "add").equals(100.0));

		assertTrue(get(mdl, "add/x1").equals(21.0));
		assertTrue(get(mdl, "add/x2").equals(81.0));
		assertTrue(get(mdl, "multiply/x1").equals(11.0));
		assertTrue(get(mdl, "multiply/x2").equals(51.0));

		// select context fidelity
		out = response(mdl, prjFi("cxtPrj3"));
		logger.info("model response: " + out);

		assertTrue(get(out, "subtract").equals(400.0));
		assertTrue(get(out, "multiply").equals(500.0));
		assertTrue(get(out, "add").equals(100.0));

		assertTrue(get(mdl, "add/x1").equals(20.0));
		assertTrue(get(mdl, "add/x2").equals(80.0));
		assertTrue(get(mdl, "multiply/x1").equals(10.0));
		assertTrue(get(mdl, "multiply/x2").equals(50.0));

	}
}

