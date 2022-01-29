package sorcer.sml.mograms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.arithmetic.provider.*;
import sorcer.arithmetic.provider.impl.*;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.core.context.model.ent.Entry;
import sorcer.core.invoker.Observable;
import sorcer.core.plexus.FidelityManager;
import sorcer.core.plexus.MorphFidelity;
import sorcer.core.plexus.MorphMogram;
import sorcer.service.*;
import sorcer.service.Strategy.FidelityManagement;
import sorcer.service.modeling.*;

import java.rmi.RemoteException;

import static org.junit.Assert.assertTrue;
import static sorcer.co.operator.*;
import static sorcer.eo.operator.*;
import static sorcer.co.operator.get;
import static sorcer.eo.operator.loop;
import static sorcer.mo.operator.*;
import static sorcer.ent.operator.*;
import static sorcer.so.operator.*;

/**
 * Created by Mike Sobolewski on 03/21/21.
 */
@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/sml")
public class ModelMultiFidelities {

    private final static Logger logger = LoggerFactory.getLogger(ModelMultiFidelities.class);

    @Test
    public void sigMultiFidelityModel() throws Exception {

        // three entry model
        Model mod = model(inVal("arg/x1", 10.0), inVal("arg/x2", 90.0),
                ent("mFi", sigFi(sig("add", AdderImpl.class, result("result/y", inPaths("arg/x1", "arg/x2"))),
                        sig("multiply", MultiplierImpl.class, result("result/y", inPaths("arg/x1", "arg/x2"))))),
                response("mFi", "arg/x1", "arg/x2"));

        Context out = eval(mod, fi("multiply", "mFi"));
        logger.info("out: " + out);
        assertTrue(get(out, "mFi").equals(900.0));
        assertTrue(get(mod, "result/y").equals(900.0));
    }

    @Test
    public void entMultiFidelityModel() throws Exception {

        // three entry model
        Model mdl = model(
                ent("arg/x1", entFi(inVal("arg/x1/fi1", 10.0), inVal("arg/x1/fi2", 11.0))),
                ent("arg/x2", entFi(inVal("arg/x2/fi1", 90.0), inVal("arg/x2/fi2", 91.0))),
                ent("mFi", sigFi(sig("add", AdderImpl.class, result("result/y", inPaths("arg/x1", "arg/x2"))),
                        sig("multiply", MultiplierImpl.class, result("result/y", inPaths("arg/x1", "arg/x2"))))),
                response("mFi", "arg/x1", "arg/x2"));

        logger.info("DEPS: " + printDeps(mdl));

        Context out = response(mdl, fi("arg/x1/fi2", "arg/x1"), fis(fi("arg/x2/fi2", "arg/x2"), fi("multiply", "mFi")));
        logger.info("out: " + out);
        assertTrue(get(out, "arg/x1").equals(11.0));
        assertTrue(get(out, "arg/x2").equals(91.0));
        assertTrue(get(out, "mFi").equals(1001.0));
        assertTrue(get(mdl, "result/y").equals(1001.0));
    }

    @Test
    public void entMultiFidelityModeWithFM() throws Exception {

        // three entry model
        Model mdl = model(
                ent("arg/x1", entFi(inVal("arg/x1/fi1", 10.0), inVal("arg/x1/fi2", 11.0))),
                ent("arg/x2", entFi(inVal("arg/x2/fi1", 90.0), inVal("arg/x2/fi2", 91.0))),
                ent("sigFi", sigFi(sig("add", AdderImpl.class, result("result/y", inPaths("arg/x1", "arg/x2"))),
                        sig("multiply", MultiplierImpl.class, result("result/y", inPaths("arg/x1", "arg/x2"))))),
                response("sigFi", "arg/x1", "arg/x2"));

        logger.info("DEPS: " + printDeps(mdl));

        reconfigure(mdl, fi("arg/x1/fi2", "arg/x1"), fi("arg/x2/fi2", "arg/x2"), fi("multiply", "sigFi"));
        logger.info("trace: " + fiTrace(mdl));
        Context out = response(mdl);
        logger.info("out: " + out);
        assertTrue(get(out, "arg/x1").equals(11.0));
        assertTrue(get(out, "arg/x2").equals(91.0));
        assertTrue(get(out, "sigFi").equals(1001.0));
        assertTrue(get(mdl, "result/y").equals(1001.0));
    }

    @Test
    public void sigMultiFidelityModel2() throws Exception {

        // three entry model
        Model mod = model(inVal("arg/x1", 10.0), inVal("arg/x2", 90.0),
                ent("mFi", sigFi(sig("add", AdderImpl.class, result("result/y", inPaths("arg/x1", "arg/x2"))),
                        sig("multiply", MultiplierImpl.class, result("result/y", inPaths("arg/x1", "arg/x2"))))),
                response("mFi", "arg/x1", "arg/x2"));

        Context out = response(mod, fi("add", "mFi"));
        logger.info("out: " + out);
        assertTrue(get(out, "mFi").equals(100.0));
        assertTrue(get(mod, "result/y").equals(100.0));
    }

    @Test
    public void refSigMultiFidelityModel() throws Exception {

        // three entry model
        Model mod = reqModel(inVal("arg/x1", 10.0), inVal("arg/x2", 90.0),
            val("sig1", sig("add", AdderImpl.class, result("result/y", inPaths("arg/x1", "arg/x2")))),
            val("sig2", sig("multiply", MultiplierImpl.class, result("result/y", inPaths("arg/x1", "arg/x2")))),

            ent("mFi", sigFi(ref("sig1"), ref("sig2"))),
            response("mFi", "arg/x1", "arg/x2"));

        Context out = response(mod, fi("sig1", "mFi"));
        logger.info("out: " + out);
//        assertTrue(getValue(out, "mFi").equals(100.0));
        assertTrue(get(mod, "result/y").equals(100.0));

        out = response(mod, fi("sig2", "mFi"));
        logger.info("out2: " + out);
//        assertTrue(getValue(out, "mFi").equals(900.0));
        assertTrue(get(mod, "result/y").equals(900.0));
    }

    @Test
    public void refInvokerMultiFidelityModel() throws Exception {

        // three entry model
        Model mod = reqModel(inVal("x1", 10.0), inVal("x2", 90.0),
                ent("eval1", invoker("add", "x1 + x2", args("x1", "x2"))),
                ent("eval2", invoker("multiply", "x1 * x2", args("x1", "x2"))),
                ent("mFi", entFi(ref("eval1"), ref("eval2"))),
                response("mFi", "x1", "x2"));

        Context out = response(mod, fi("eval1", "mFi"));
        logger.info("out: " + out);
        assertTrue(get(out, "mFi").equals(100.0));

        out = response(mod, fi("eval2", "mFi"));
        logger.info("out2: " + out);
        assertTrue(get(out, "mFi").equals(900.0));
    }

    @Test
    public void sigMultiFidelityAmorphousModel() throws Exception {

        FidelityManager manager = new FidelityManager() {
            @Override
            public void initialize() {
                // define model metafidelities Fidelity<Fidelity>
                add(metaFi("sysFi2", fi("divide", "mFi2"), fi("multiply", "mFi3")));
                add(metaFi("sysFi3", fi("average", "mFi2"), fi("divide", "mFi3")));
            }

            @Override
            public void update(Observable mFi, Object value) throws EvaluationException, RemoteException {
                if (mFi instanceof MorphFidelity) {
                    Fidelity<Signature> fi = ((MorphFidelity) mFi).getFidelity();
                    if (fi.getPath().equals("mFi1") && fi.getSelectName().equals("add")) {
                        if (((Double) value) <= 200.0) {
                            morph("sysFi2");
                        } else {
                            morph("sysFi3");
                        }
                    } else if (fi.getPath().equals("mFi1") && fi.getSelectName().equals("multiply")) {
                        morph("sysFi3");
                    }
                }
            }
        };

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

        // three entry multifidelity model
        Model mod = model(inVal("arg/x1", 90.0), inVal("arg/x2", 10.0),
                ent("mFi1", mphFi(add, multiply)),
                ent("mFi2", mphFi(average, divide, subtract)),
                ent("mFi3", mphFi(average, divide, multiply)),
                manager,
                response("mFi1", "mFi2", "mFi3", "arg/x1", "arg/x2"));

        Context out = response(mod);
        logger.info("out: " + out);
        assertTrue(get(out, "mFi1").equals(100.0));
        assertTrue(get(out, "mFi2").equals(9.0));
        assertTrue(get(out, "mFi3").equals(900.0));

        // closing the fidelity for mFi1
        out = response(mod , fi("multiply", "mFi1"));
        logger.info("out: " + out);
        assertTrue(get(out, "mFi1").equals(900.0));
        assertTrue(get(out, "mFi2").equals(50.0));
        assertTrue(get(out, "mFi3").equals(9.0));
    }

    @Test
    public void notInitializedFidelityManager() throws Exception {

        FidelityManager manager = new FidelityManager() {

            @Override
            public void update(Observable mFi, Object value) throws EvaluationException, RemoteException {
                if (mFi instanceof MorphFidelity) {
                    Fidelity<Signature> fi = ((MorphFidelity) mFi).getFidelity();
                    if (fi.getPath().equals("mFi1") && fi.getSelectName().equals("add")) {
                        if (((Double) value) <= 200.0) {
                            morph("sysFi2");
                        } else {
                            morph("sysFi3");
                        }
                    } else if (fi.getPath().equals("mFi1") && fi.getSelectName().equals("multiply")) {
                        morph("sysFi3");
                    }
                }
            }
        };

        Metafidelity fi2 = metaFi("sysFi2",fi("divide$mFi2"), fi("multiply$mFi3"));
        Metafidelity fi3 = metaFi("sysFi3", fi("average$mFi2"), fi("divide$mFi3"));

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

        // three entry multifidelity model
        Model mod = model(inVal("arg/x1", 90.0), inVal("arg/x2", 10.0),
                ent("mFi1", mphFi(add, multiply)),
                ent("mFi2", mphFi(average, divide, subtract)),
                ent("mFi3", mphFi(average, divide, multiply)),
                manager, fi2, fi3,
                response("mFi1", "mFi2", "mFi3", "arg/x1", "arg/x2"));

        Context out = response(mod);
        logger.info("out: " + out);
        assertTrue(get(out, "mFi1").equals(100.0));
        assertTrue(get(out, "mFi2").equals(9.0));
        assertTrue(get(out, "mFi3").equals(900.0));

        // first closing the fidelity for mFi1
        out = response(mod , fi("mFi1$multiply"));
        logger.info("out: " + out);
        assertTrue(get(out, "mFi1").equals(900.0));
        assertTrue(get(out, "mFi2").equals(50.0));
        assertTrue(get(out, "mFi3").equals(9.0));
    }

    @Test
    public void updateFiManagaerAmorphousModel() throws Exception {

        FidelityManager manager = new FidelityManager() {
            @Override
            public void initialize() {
                // define model metafidelities Fidelity<Fidelity>
                add(metaFi("sysFi2", fi("divide", "mFi2"), fi("multiply", "mFi3")));
                add(metaFi("sysFi3", fi("average", "mFi2"), fi("divide", "mFi3")));
            }

            @Override
            public void update(Observable mFi, Object value) throws EvaluationException, RemoteException {
                if (mFi instanceof MorphFidelity) {
                    Fidelity<Signature> fi = ((MorphFidelity) mFi).getFidelity();
                    if (fi.getPath().equals("mFi1") && fi.getSelectName().equals("add")) {
                        if (((Double) value) <= 200.0) {
                            morph("sysFi2");
                        } else {
                            morph("sysFi3");
                        }
                    } else if (fi.getPath().equals("mFi1") && fi.getSelectName().equals("multiply")) {
                        morph("sysFi3");
                    }
                }
            }
        };

        Entry addEnt = ent(sig("add", AdderImpl.class,
                result("result/y1", inPaths("arg/x1", "arg/x2"))));
        Entry subtractEnt = ent(sig("subtract", SubtractorImpl.class,
                result("result/y1", inPaths("arg/x1", "arg/x2"))));
        Entry multiplyEnt = ent(sig("multiply", MultiplierImpl.class,
                result("result/y1", inPaths("arg/x1", "arg/x2"))));
        Entry divideEnt = ent(sig("divide", DividerImpl.class,
                result("result/y2", inPaths("arg/x1", "arg/x2"))));
        Entry averageEnt = ent(sig("average", AveragerImpl.class,
                result("result/y2", inPaths("arg/x1", "arg/x2"))));

        Model mod = model(inVal("arg/x1", 90.0), inVal("arg/x2", 10.0),
                addEnt, multiplyEnt, divideEnt, averageEnt,
                ent("mFi1", mphFi(addEnt, multiplyEnt)),
                ent("mFi2", mphFi(averageEnt, divideEnt, subtractEnt)),
                ent("mFi3", mphFi(averageEnt, divideEnt, multiplyEnt)),
                manager,
                response("mFi1", "mFi2", "mFi3", "arg/x1", "arg/x2"));

        Context out = response(mod);
        logger.info("out: " + out);
        assertTrue(get(out, "mFi1").equals(100.0));
        assertTrue(get(out, "mFi2").equals(9.0));
        assertTrue(get(out, "mFi3").equals(900.0));

        // first closing the fidelity for mFi1
        out = response(mod , fi("mFi1", "multiply"));
        logger.info("out: " + out);
        assertTrue(get(out, "mFi1").equals(900.0));
        assertTrue(get(out, "mFi2").equals(50.0));
        assertTrue(get(out, "mFi3").equals(9.0));
    }

    @Test
    public void morphingMultiFidelityModel() throws Exception {

        Morpher morpher1 = (mgr, mFi, value) -> {
            Fidelity<Signature> fi =  mFi.getFidelity();
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

        // five entry multifidelity model with morphers
        Model mod = model(inVal("arg/x1", 90.0), inVal("arg/x2", 10.0),
				ent("arg/y1", entFi(inVal("arg/y1/fi1", 10.0), inVal("arg/y1/fi2", 11.0))),
				ent("arg/y2", entFi(inVal("arg/y2/fi1", 90.0), inVal("arg/y2/fi2", 91.0))),
                ent("mFi1", mphFi(morpher1, add, multiply)),
                ent("mFi2", mphFi(morpher2, average, divide, subtract)),
                ent("mFi3", mphFi(average, divide, multiply)),
                fi2, fi3, fi4,
                FidelityManagement.YES,
                response("mFi1", "mFi2", "mFi3", "arg/x1", "arg/x2"));

        Context out = response(mod);
        logger.info("out: " + out);
        assertTrue(get(out, "mFi1").equals(100.0));
        assertTrue(get(out, "mFi2").equals(9.0));
        assertTrue(get(out, "mFi3").equals(50.0));

        // closing the fidelity for mFi1
        out = response(mod , fi("mFi1", "multiply"));
        logger.info("out: " + out);
        assertTrue(get(out, "mFi1").equals(900.0));
        assertTrue(get(out, "mFi2").equals(50.0));
        assertTrue(get(out, "mFi3").equals(9.0));
    }

    @Test
    public void selectMultifidelityEntries() throws Exception {
        Entry e1 = ent("x1", 5.0);
        Entry e2 = ent("x2", 6.0);
        Entry e3 = ent("x3", 7.0);

        Mogram mfs = fiMog("args", rFi(e1, e2, e3));

        Object out = exec(mfs);
        logger.info("out: " + out);
        assertTrue(out.equals(5.0));

        selectFi(mfs, "x2");
        out = exec(mfs);
        logger.info("out: " + out);
        assertTrue(out.equals(6.0));

        selectFi(mfs, "x3");
        out = exec(mfs);
        logger.info("out: " + out);
        assertTrue(out.equals(7.0));
    }

    @Test
    public void morphMultifidelityEntries() throws Exception {
        Entry e1 = ent("x1", 5.0);
        Entry e2 = ent("x2", 6.0);
        Entry e3 = ent("x3", 7.0);

        Morpher morpher = (mgr, mFi, value) -> {
            Fidelity<Signature> fi =  mFi.getFidelity();
            if (fi.getSelectName().equals("x1")) {
                if (((double)value) <= 5.0) {
                    mgr.reconfigure("x3");
                }
            }
        };

        MorphMogram mfs = fiMog(mphFi(morpher, e1, e2, e3));

        Object out = exec(mfs);
        logger.info("out: " + out);
        assertTrue(out.equals(5.0));

        out = exec(mfs);
        logger.info("out: " + out);
        assertTrue(out.equals(7.0));
    }

    @Test
    public void selectMultifidelitySignatures() throws Exception {

        Context cxt = context(inVal("arg/x1", 10.0), inVal("arg/x2", 50.0),
                outVal("result/y"));
        Signature ms = sig("multiply", MultiplierImpl.class);
        Signature as = sig("add", AdderImpl.class);

        MorphMogram mfs = fiMog(rFi(ms, as), cxt);

        Context out = (Context) exec(mfs);
        logger.info("out: " + out);
        assertTrue(value(context(out), "result/y").equals(500.0));

        selectFi(mfs, "add");
        out = (Context) exec(mfs);
        assertTrue(value(context(out), "result/y").equals(60.0));
    }

    @Test
    public void morphMultifidelitySignatures() throws Exception {

        Context cxt = context(inVal("arg/x1", 10.0), inVal("arg/x2", 50.0),
                outVal("result/y"));
        Signature ms = sig("multiply", MultiplierImpl.class);
        Signature as = sig("add", AdderImpl.class);

        Morpher morpher = (mgr, mFi, value) -> {
            Fidelity<Signature> fi =  mFi.getFidelity();
            if (fi.getSelectName().equals("multiply")) {
                if (((Double) value(context(value), "result/y")) >= 500.0) {
                    mgr.reconfigure(fi("add", "sigFi"));
                }
            }
        };

        MorphMogram mfs = fiMog(mphFi("sigFi", morpher, ms, as), cxt);

        Context out = (Context) exec(mfs);
        logger.info("out: " + out);
        assertTrue(value(context(out), "result/y").equals(500.0));

        out = (Context) exec(mfs);
        assertTrue(value(context(out), "result/y").equals(60.0));
    }

    @Test
    public void morphWithInOutMorphers() throws Exception {

        Context cxt = context(inVal("arg/x1", 10.0), inVal("arg/x2", 50.0),
            outVal("result/y"));
        Signature ms = sig("multiply", MultiplierImpl.class);
        Signature as = sig("add", AdderImpl.class);

        Morpher inMorpher = (mgr, mFi, value) -> {
            Fidelity<Signature> fi =  mFi.getFidelity();
            Fi.Type type = mFi.getFidelity().getFiType();
            if (fi.getSelectName().equals("multiply")) {
                // change value fom 10.0 to 20.0
                setValue((Context)value, "arg/x1", 20.0);
            }
        };

        Morpher outMorpher = (mgr, mFi, value) -> {
            Fidelity<Signature> fi =  mFi.getFidelity();
            Fi.Type type = mFi.getFidelity().getFiType();
            if (fi.getSelectName().equals("multiply")) {
                if (((Double) value(context(value), "result/y")) >= 500.0) {
                    mgr.reconfigure(fi("add", "sigFi"));
                }
            }
        };

        // the first morpher is for the input context the next one for the output context
        MorphMogram mfs = fiMog(mphFi("sigFi", inMorpher, outMorpher, ms, as), cxt);

        Context out = (Context) exec(mfs);
        logger.info("out: " + out);
        assertTrue(value(context(out), "result/y").equals(1000.0));

        out = (Context) exec(mfs);
        assertTrue(value(context(out), "result/y").equals(70.0));
    }

    @Test
    public void morphWithSameInOutMorpher() throws Exception {

        Context cxt = context(inVal("arg/x1", 10.0), inVal("arg/x2", 50.0),
            outVal("result/y"));
        Signature ms = sig("multiply", MultiplierImpl.class);
        Signature as = sig("add", AdderImpl.class);

        Morpher morpher = (mgr, mFi, value) -> {
            Fidelity<Signature> fi =  mFi.getFidelity();
            Fi.Type type = mFi.getFidelity().getFiType();
            if (type == Fi.Type.IN) {
                if (fi.getSelectName().equals("multiply")) {
                    // change value fom 10.0 to 20.0
                    setValue(( Context ) value, "arg/x1", 20.0);
                }
            } else if (type == Fi.Type.OUT) {
                if (((Double) value(context(value), "result/y")) >= 500.0) {
                    mgr.reconfigure(fi("add", "sigFi"));
                }
            }
        };

        // the first morpher is for the input context the next one for the output context
        MorphMogram mfs = fiMog(mphFi("sigFi", morpher, morpher, ms, as), cxt);

        Context out = (Context) exec(mfs);
        logger.info("out: " + out);
        assertTrue(value(context(out), "result/y").equals(1000.0));

        out = (Context) exec(mfs);
        assertTrue(value(context(out), "result/y").equals(70.0));
    }

    @Test
    public void selectMultiFiRequest() throws Exception {

        Task t4 = task("t4",
                sig("multiply", MultiplierImpl.class),
                context("multiply", inVal("arg/x1", 10.0), inVal("arg/x2", 50.0),
                        outVal("result/y")));

        Task t5 = task("t5",
                sig("add", AdderImpl.class),
                context("add", inVal("arg/x1", 20.0), inVal("arg/x2", 80.0),
                        outVal("result/y")));


        MorphMogram mfs = fiMog(mphFi("taskFi", t5, t4));
        Mogram mog = exert(mfs);
        logger.info("out: " + mog.getContext());
        assertTrue(value(context(mog), "result/y").equals(100.0));

        selectFi(mfs, "t4");
        mog = exert(mfs);
        logger.info("out: " + mog.getContext());
        assertTrue(value(context(mog), "result/y").equals(500.0));
    }

    @Test
    public void morphMultiFiRequest() throws Exception {

        mog t4 = task(
                "t4",
                sig("multiply", MultiplierImpl.class),
                context("multiply", inVal("arg/x1", 10.0), inVal("arg/x2", 50.0),
                        outVal("result/y")));

        mog t5 = task(
                "t5",
                sig("add", AdderImpl.class),
                context("add", inVal("arg/x1", 20.0), inVal("arg/x2", 80.0),
                        outVal("result/y")));


        Morpher morpher = (mgr, mFi, value) -> {
            Fidelity<Signature> fi =  mFi.getFidelity();
            if (fi.getSelectName().equals("t5")) {
                if (((Double) value(context(value), "result/y")) <= 200.0) {
                    mgr.reconfigure("t4");
                }
            }
        };

        MorphMogram mfs = fiMog(mphFi(morpher, t5, t4));
        Mogram mog = exert(mfs);
        logger.info("out: " + context(mog));
        assertTrue(value(context(mog), "result/y").equals(100.0));

        mog = exert(mfs);
        logger.info("out: " + mog.getContext());
        assertTrue(value(context(mog), "result/y").equals(500.0));
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

        cxtn t4 = task("t4",
            sig("multiply", MultiplierImpl.class,
                result("result/y", inPaths("arg/x1", "arg/x2"))));

        cxtn t5 = task("t5",
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
            FidelityManagement.YES,
            response("mFi1", "mFi2", "mFi3", "mFi4", "arg/x1", "arg/x2", "morpher3"));

        return mdl;
    }

    @Test
    public void morphingFidelities() throws Exception {
        mog mdl = getMorphingModel();
        traced(mdl, true);
        cxt out = response(mdl);

        logger.info("out: " + out);
        logger.info("trace: " + fiTrace(mdl));
        assertTrue(value(out, "mFi1").equals(100.0));
        assertTrue(value(out, "mFi2").equals(9.0));
        assertTrue(value(out, "mFi3").equals(900.0));
        assertTrue(value(out, "mFi4").equals(900.0));
        assertTrue(value(out, "morpher3").equals(920.0));

        // closing the fidelity for mFi1
        out = response(mdl , fi("mFi1", "multiply"));
        logger.info("out: " + out);
        logger.info("trace: " + fiTrace(mdl));
        assertTrue(value(out, "mFi1").equals(900.0));
        assertTrue(value(out, "mFi2").equals(50.0));
        assertTrue(value(out, "mFi3").equals(9.0));
        assertTrue(value(out, "morpher3").equals(920.0));

        // check if metaFi("mFi1", "multiply") was executed
        out = response(mdl);
        logger.info("out: " + out);
        logger.info("trace: " + fiTrace(mdl));
        assertTrue(value(out, "mFi1").equals(900.0));
        assertTrue(value(out, "mFi2").equals(50.0));
        assertTrue(value(out, "mFi3").equals(9.0));
        logger.info("out mFi4: " + get(out, "morpher3"));
        assertTrue(value(out, "morpher3").equals(920.0));
    }

    @Test
    public void morphingModelDefaultFidelities() throws Exception {

        Morpher mdlMorpher = (mgr, mFi, value) -> {
            // model mFi not set
            Double x1 = (Double) exec(((Model)value), "arg/x1");
            Double x2 = (Double) exec(((Model)value), "arg/x2");
            if (x1  > x2) {
                mgr.reconfigure(fi("multiply", "mFi1"));
            }
        };

        mog mdl = getMorphingModel();
        setMorpher(mdl, mdlMorpher);
        traced(mdl, true);
        cxt out = response(mdl);
        logger.info("out: " + out);
        logger.info("trace: " + fiTrace(mdl));
        assertTrue(value(out, "mFi1").equals(900.0));
        assertTrue(value(out, "mFi2").equals(50.0));
        assertTrue(value(out, "mFi3").equals(9.0));
        assertTrue(value(out, "mFi4").equals(100.0));
        assertTrue(value(out, "morpher3").equals(110.0));
    }
}
