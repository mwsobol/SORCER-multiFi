package sorcer.sml.contexts;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import static sorcer.ent.operator.srv;
import static sorcer.so.operator.eval;
import static sorcer.so.operator.exec;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.arithmetic.provider.impl.*;
import sorcer.core.context.model.ent.Entry;
import sorcer.ent.operator;
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
public class TaskContextFidelity {
	private final static Logger logger = LoggerFactory.getLogger(TaskContextFidelity.class);

	@Test
	public void multiPathLambdaValue() throws Exception {

        // the model execute a fxn lambda expression with no model state altered
        Model mdl = model(ent(pthFis("x1", "arg/x1"), 10.0), ent(pthFis("x2", "arg/x2"), 20.0),
            operator.srv(pthFis("x3", "arg/x3"),
                (Model model) ->
                    ent("x5", (double)exec(model, "arg/x2") + 100.0)));

        Context cxt = eval(mdl, "x3", fromTo("x2", "arg/x2"));
        assertEquals(120.0, value(cxt, "x3"));

//      Entry x3 = (Entry) exec(mdl, "x3", fromTo("x2", "arg/x2"));
//		assertEquals(120.0, exec(x3));

        Entry x3 = (Entry) exec(mdl, "x3");
        assertEquals(120.0, exec(x3));
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
				cxtFis(cxt1, cxt2));

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
			inVal(pthFis("arg/x1", "arg/y1"), 20.0),
			inVal(pthFis("arg/x2", "arg/y2"), 80.0));

		Context cxt2 = context("cxt2",
			inVal(pthFis("arg/x1", "arg/y1"), 30.0),
			inVal(pthFis("arg/x2", "arg/y2"), 90.0));

		Projection outPrj = outProj(fromTo("arg/x1", "arg/y1"), fromTo("arg/x2", "arg/y2"));

		Task t5 = task("t5", sig("add", AdderImpl.class),
			cxtFis(cxt1, cxt2), outPrj);

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

	@Test
	public void cxtProjectionMultiFiContextTask() throws Exception  {

		Context cxt1 = context("cxt1",
			inVal(pthFis("arg/x1", "arg/y1"), 20.0),
			inVal(pthFis("arg/x2", "arg/y2"), 80.0));

		Context cxt2 = context("cxt2",
			inVal(pthFis("arg/x1", "arg/y1"), 30.0),
			inVal(pthFis("arg/x2", "arg/y2"), 90.0));

		Task t5 = task("t5", sig("add", AdderImpl.class),
			cxtFis(cxt1, cxt2));

		Projection cxtPrj1 = cxtPrj(cxtFi("cxt1"), outProj(fromTo("arg/x1", "arg/y1"), fromTo("arg/x2", "arg/y2")));
		Projection cxtPrj2 = cxtPrj(cxtFi("cxt2"), outProj(fromTo("arg/x1", "arg/y1"), fromTo("arg/x2", "arg/y2")));

		Routine out = exert(t5, cxtPrj1);
		Context cxt = context(out);

		// getValue a single context argument
		assertEquals(100.0, value(cxt, "result/eval"));

		// getValue the subcontext output from the context
		assertTrue(context(ent("result/eval", 100.0), ent("arg/y1", 20.0)).equals(
			value(cxt, outPaths("result/eval", "arg/y1"))));

		out = exert(t5, cxtPrj2);
		cxt = context(out);

		// getValue a single context argument
		assertEquals(120.0, value(cxt, "result/eval"));

		// getValue the subcontext output from the context
		assertTrue(context(ent("result/eval", 120.0), ent("arg/y1", 30.0)).equals(
			value(cxt, outPaths("result/eval", "arg/y1"))));
	}

	@Test
	public void cxtFiManagerMultiFiContextTask() throws Exception  {

		Context cxt1 = context("cxt1",
			inVal(pthFis("arg/x1", "arg/y1"), 20.0),
			inVal(pthFis("arg/x2", "arg/y2"), 80.0));

		Context cxt2 = context("cxt2",
			inVal(pthFis("arg/x1", "arg/y1"), 30.0),
			inVal(pthFis("arg/x2", "arg/y2"), 90.0));

		Task t5 = task("t5", sig("add", AdderImpl.class),
			cxtFis(cxt1, cxt2),
			cxtPrj("cxtPrj1", cxtFi("cxt1"), outProj(fromTo("arg/x1", "arg/y1"), fromTo("arg/x2", "arg/y2"))),
			cxtPrj("cxtPrj2", cxtFi("cxt2"), outProj(fromTo("arg/x1", "arg/y1"), fromTo("arg/x2", "arg/y2"))));

		Routine out = exert(t5, prjFi("cxtPrj1"));
		Context cxt = context(out);

		// getValue a single context argument
		assertEquals(100.0, value(cxt, "result/eval"));

		// getValue the subcontext output from the context
		assertTrue(context(ent("result/eval", 100.0), ent("arg/y1", 20.0)).equals(
			value(cxt, outPaths("result/eval", "arg/y1"))));

		out = exert(t5, prjFi("cxtPrj2"));
		cxt = context(out);

		// getValue a single context argument
		assertEquals(120.0, value(cxt, "result/eval"));

		// getValue the subcontext output from the context
		assertTrue(context(ent("result/eval", 120.0), ent("arg/y1", 30.0)).equals(
			value(cxt, outPaths("result/eval", "arg/y1"))));
	}


	@Test
    public void morphCxtFiManagerMultiFiContextTask() throws Exception  {
        // remapping outputs with ContextFidelityManager

        Context cxt1 = context("cxt1",
            inVal(pthFis("arg/x1", "arg/y1"), 20.0),
            inVal(pthFis("arg/x2", "arg/y2"), 80.0));

        Context cxt2 = context("cxt2",
            inVal(pthFis("arg/x1", "arg/y1"), 30.0),
            inVal(pthFis("arg/x2", "arg/y2"), 90.0));

        Morpher morpher = (mgr, mFi, value) -> {
            Context cxt = (Context) mFi.getSelect();
            Task task = (Task) value;
            if (task.isValid() && task.getContext().getName().equals("cxt1")) {
                mgr.morph("cxtPrj1");
            } else if (task.isValid() && task.getContext().getName().equals("cxt2")) {
                mgr.morph("cxtPrj2");
            }
        };

        Task t5 = task("t5", sig("add", AdderImpl.class),
            cxtFis(morpher, cxt1, cxt2),
            cxtPrj("cxtPrj1", cxtFi("cxt1"), outProj(fromTo("arg/x1", "arg/y1"), fromTo("arg/x2", "arg/y2"))),
            cxtPrj("cxtPrj2", cxtFi("cxt2"), outProj(fromTo("arg/x1", "arg/y1"), fromTo("arg/x2", "arg/y2"))));

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

    @Test
    public void morphCxtFiManagerInOutMultiFiContextTask() throws Exception  {
        // remapping inputs and outputs with ContextFidelityManager

        Context cxt1 = context("cxt1",
            inVal(pthFis("x1", "arg/x1", "arg/y1"), 20.0),
            inVal(pthFis("x2", "arg/x2", "arg/y2"), 80.0));

        Context cxt2 = context("cxt2",
            inVal(pthFis("x1", "arg/x1", "arg/y1"), 30.0),
            inVal(pthFis("x2", "arg/x2", "arg/y2"), 90.0));

        Morpher morpher = (mgr, mFi, value) -> {
            Context cxt = (Context) mFi.getSelect();
            Task task = (Task) value;
            if (task.isValid() && task.getContext().getName().equals("cxt1")) {
                mgr.morph("cxtPrj1");
            } else if (task.isValid() && task.getContext().getName().equals("cxt2")) {
                mgr.morph("cxtPrj2");
            }
        };

        Task t5 = task("t5", sig("add", AdderImpl.class),
            cxtFis(morpher, cxt1, cxt2),
            cxtPrj("cxtPrj1", cxtFi("cxt1"), inProj(fromTo("x1", "arg/x1"), fromTo("x2", "arg/x2")),
                outProj(fromTo("arg/x1", "arg/y1"), fromTo("arg/x2", "arg/y2"))),
            cxtPrj("cxtPrj2", cxtFi("cxt2"), inProj(fromTo("x1", "arg/x1"), fromTo("x2", "arg/x2")),
                outProj(fromTo("arg/x1", "arg/y1"), fromTo("arg/x2", "arg/y2"))));

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
	
	
