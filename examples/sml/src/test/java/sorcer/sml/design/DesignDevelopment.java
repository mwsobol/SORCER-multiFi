package sorcer.sml.design;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.arithmetic.provider.*;
import sorcer.arithmetic.provider.impl.*;
import sorcer.core.context.DesignIntent;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.core.plexus.MorphFidelity;
import sorcer.service.*;
import sorcer.service.Strategy.FidelityManagement;
import sorcer.service.modeling.*;

import static org.junit.Assert.assertTrue;
import static sorcer.co.operator.get;
import static sorcer.co.operator.*;
import static sorcer.ent.operator.*;
import static sorcer.eo.operator.fi;
import static sorcer.eo.operator.loop;
import static sorcer.eo.operator.result;
import static sorcer.eo.operator.*;
import static sorcer.mo.operator.model;
import static sorcer.mo.operator.value;
import static sorcer.mo.operator.*;
import static sorcer.so.operator.*;

/**
 * Created by Mike Sobolewski on 05/20/21.
 */
@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/sml")
public class DesignDevelopment {

    private final static Logger logger = LoggerFactory.getLogger(DesignDevelopment.class);

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
    public void morphingFidelitiesLoop() throws Exception {
        mog mdl = getMorphingModel();

        Block mdlBlock = block(
            loop(condition(cxt -> (double)
                    value(cxt, "morpher3") < 900.0), mdl));

//        logger.info("DEPS: " + printDeps(mdl));
        mdlBlock = exert(mdlBlock, fi("multiply", "mFi1"));
//        logger.info("block context: " + context(mdlBlock));
//        logger.info("result: " + getValue(context(mdlBlock), "mFi4"));

        assertTrue(value(context(mdlBlock), "morpher3").equals(920.0));
    }

    @Test
    public void morphingDiscipline() throws Exception {

        // cxtn1 is a free contextion for a discipline dispatcher
        Block mdlDispatch = block(
            loop(condition(cxt -> (double)
                value(cxt, "morpher3") < 900.0), model("cxtn1")));

        Node morphDis = rnd(
            rndFi("morpher3",
                cxtnFi("cxtn1", sig(DesignDevelopment.class, "getMorphingModel")),
                dspFi("dspt1", mdlDispatch)));

        // out is the discipline output
        Context out  = eval(morphDis);

        assertTrue(value(out, "morpher3").equals(920.0));
    }

    @Test
    public void developingDesign() throws Exception {

        Morpher dznMorpher = mfr("dznMorpher", (mgr, mFi, value) -> {
            if (mFi instanceof MorphFidelity) {
                Fidelity fi = mFi.getFidelity();
                Morpher morpher = (( MorphFidelity ) mFi).getMorpher();
                logger.info("mFi name: " + mFi.getName());
            }});

        // testing syntax for intent contexts
        Intent designIntent = dznIntent(
            dscFi(sig("morphMdl", DesignDevelopment.class, "getMorphingModel")),
            dznFi("intFis",
                intFi("discIntX",
                    dscInt("multiA",
                        cxt("myIntent1", intType("mado")),
                        cxt("myIntent2", intType("mda"))),
                    dscInt("multiB",
                        cxt("myIntent3", intType("mado")),
                        cxt("myIntent4", intType("mda"))),
                    cxt("myIntent5", intType("mado"))),
                intFi("discIntY", dscSig(DesignDevelopment.class, "getMorphingModel"))),
//            devFi("sellar", dznMorpher),
            devFi("morphDevFis", dznMorpher, // dznMorpher,
                dev("morphDev1",
                    (Discipline discipline, Context intent) -> {
                        Block mdlBlock = block(
                            loop(condition(cxt -> (double)
                                value(cxt, "morpher3") < 900.0), discipline));
                        mdlBlock = exert(mdlBlock, fi("multiply", "mFi1"));
                        return context(mdlBlock);
                    }),
                dev("morphDev2",
                    (Discipline discipline, Context cxt) -> {
                        Block mdlBlock = block(
                            loop(condition(bcxt -> (double)
                                value(bcxt, "morpher3") < 900.0), discipline));
                        mdlBlock = exert(mdlBlock, fi("add", "mFi1"));
                        return context(mdlBlock);
                    }))
        );

        Design desg = dzn(designIntent);
        setMorpher(desg, dznMorpher);
        setInMorpher(desg, dznMorpher);
//        traced(desg, true);

        Context out = dvlp(designIntent, fi("morphDev1"));
//        Context out = dvlp(desg, fi("morphDev1"));
//        assertTrue(value(out, "morpher3").equals(920.0));
    }

}
