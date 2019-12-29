/*
 * Copyright 2012 the original author or authors.
 * Copyright 2012 SorcerSoft.org.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sorcer.sml.transmodel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.co.tuple.ExecDependency;
import sorcer.service.Context;
import sorcer.service.Request;
import sorcer.service.Routine;
import sorcer.service.modeling.Transmodel;
import sorcer.sml.mograms.ModelMultiFidelities;
import sorcer.sml.mograms.RoutineMultiFidelities;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sorcer.co.operator.*;
import static sorcer.co.operator.setValue;
import static sorcer.co.operator.tag;
import static sorcer.co.operator.update;
import static sorcer.ent.operator.ent;
import static sorcer.ent.operator.expr;
import static sorcer.eo.operator.fi;
import static sorcer.eo.operator.*;
import static sorcer.mo.operator.*;
import static sorcer.mo.operator.value;
import static sorcer.so.operator.eval;
import static sorcer.so.operator.exec;
import static sorcer.so.operator.response;

//@Ignore
@RunWith(SorcerTestRunner.class)
@ProjectContext("vml-tutorial")
public class TransModels {

    private final static Logger logger = LoggerFactory.getLogger(TransModels.class);


    public static Transmodel getCoupledModel() throws Exception {

        Transmodel transmodel = tModel("arithmeticTransmodel",
            ent("y1", expr("x1 * x2", args("x1", "x2"))),
            ent("y2", expr("x3 + x4", args("x3", "x4"))),
            ent("y3", expr("y1 - y2", args("y1", "y2"))),

            instance("model1", sig(ModelMultiFidelities.class, "sigMultiFidelityModel")),

            instance("routine1", sig(RoutineMultiFidelities.class, "getMultiFiJob")),

            response("y1", "y2", "y3"),
            paths("model1", "routine1"));

        return transmodel;
    }

//    public static CoupledModel getMultiFiCoupledModel() throws Exception {
//
//        CoupledModel madoModel = madoModel("MADO GometryModel",
//            // global args
//            inputVars(svr("z1", 5.0), svr("z2", 10.0)),
//
//            OptimizationModel.instance("domain1", sig(GeometryModelBuilder.class, "getOptimizationModel")),
//
//            mdlFi("domain2", OptimizationModel.instance("domainFi21", sig(GeometryModelBuilder.class, "getOptimizationModel")),
//                OptimizationModel.instance("domainFi22", sig(SqrtModelBuilder.class, "getSqrtModel"))),
//
//            mdlFi("domain3", OptimizationModel.instance("domainFi31", sig(GeometryModelBuilder.class, "getOptimizationModel")),
//                OptimizationModel.instance("domainFi32", sig(SqrtModelBuilder.class, "getSqrtModel"))),
//
//            mdlFi("domain4", OptimizationModel.instance("domainFi41", sig(GeometryModelBuilder.class, "getOptimizationModel")),
//                OptimizationModel.instance("domainFi42", sig(SqrtModelBuilder.class, "getSqrtModel"))),
//
//            cplg("x", "domain1","domain2"),
//            cplg("y", "domain1", "domain2"),
//
//            cplg(tie("domain1", "x"), tie("domain2", "x`")),
//            cplg(tie("domain1", "y"), tie("domain2", "y`")),
//
//            mado("domain1", "domain2", "domain3"));
//
//        dependsOn(madoModel, domDep("domain2", fi("domainFi32"), paths("domain4")));
//
//        reconfigure(madoModel, fi("domain2", "domainFi22"), fi("domain3", "domainFi32"));
//
////		couplings(madoModel, cplg(tie("domain1", "x"), tie("domain2", "cx1")),
////				cplg(tie("domain1", "y"), tie("domain2", "cx2")));
//
//        return madoModel;
//    }
//
//    @Test
//    public void evaluateMadoModel() throws Exception {
//
//        CoupledModel model = CoupledModel.instance(sig(MiscCoupledModelBuilder.class, "getCoupledModel"));
//        Context rc = eval(model,
//                exploreContext(
//                        initialDesign(val("z1", 20.0), val("z2", 30.0), val("x1$domain2", 0.5), val("x2$domain2", 0.8)),
////                        INPUTS, OUTPUTS));
//                        ALL));
//
//        logger.info("response context: " + rc);
//
////        logger.info("value z1: " + exec(model, "z1"));
//        assertEquals(exec(model, "z1"), 20.0);
//
////        logger.info("value x2: " + exec(model, "domain2", "x2"));
//        assertEquals(exec(model, "x2$domain2"), 0.8);
//
//        rc = evaluate(model, rc);
//        SvrInfo vi = varInfo(model, "x2$domain2");
////        logger.info("value x2 info: " + vi);
//        assertEquals(field(varInfo(model, "x2$domain2"), Field.value), 5.678);
//
////        logger.info("value x2 evaluator: " + slot(info(model, "domain2", "x2"), Slot.evaluator));
//        assertEquals(field(varInfo(model, "x2$domain2"), Field.evaluator), "x2e");
//
//        ((ResponseContext) rc).queryAll(null);
//        Context ec = eval(model,
//                exploreContext(initialDesign(val("z1", 20.0), val("z2", 30.0))));
//        logger.info("evaluation context: " + ec);
//
//        // domain access
//        Object areaVal = value(ec, "area", "domain1");
//        assertEquals(areaVal, 6.0);
//        Object fVal = value(ec, "f", "domain2");
//        assertEquals(fVal, 0.8944271909999159);
//
//        // parent/global access
//        areaVal = value(ec, "area");
//        assertEquals(areaVal, 6.0);
//        fVal = value(ec, "f");
//        assertEquals(fVal, 0.8944271909999159);
//    }
//
//    @Test
//    public void analyzeMadoSellarMdfMda() throws Exception {
//
//        CoupledModel model = CoupledModel.instance(sig(SellarModelBuilder.class, "getMdfMadoSellarModel"));
//
//        Context rc = rc = analyze(model,
//            madoContext(
//                predVal("y2$DisS1", 8.0),
//                initialDesign(val("z1", 2.0), val("x1", 0.2), val("x2", 0.2)),
////                  CONSTANTS, INPUTS, OUTPUTS, CONSTRAINTS, OBJECTIVES,
//                ALL,
//                mdaFi("SellarMdaFi",
////                    mda("sigMda", sig(SellarMda.class)),
//                    mda("lambdaMda",
//                        (Request mdl, Context cxt) -> {
//                            Context ec;
//                            double y2a;
//                            double y2b;
//                            double d = 0.01;
//                            do { update(cxt, outVi("y1$DisS1"), outVi("y2$DisS2"));
//                                y2a = (double) exec(mdl, "y2$DisS1");
//                                y2a = y2a + d;
//                                update(cxt, predVi("y2$DisS1", y2a));
//                                ec = eval(mdl, cxt);
//                                y2b = (double) operator.value(ec, outVi("y2$DisS2"));
//                                logger.info("y2a:y2b=" + y2a + ":" + y2b);
//                                logger.info("delta: " + Math.abs(y2a - y2b));
//                            } while (Math.abs(y2a - y2b) > 0.01);
//                        }))));
//
//        SvrInfo vi = vi(rc, "fo");
//        logger.info("vi fo: " + vi);
//        assertEquals(operator.value(vi), 3.836430883392341);
//    }
}