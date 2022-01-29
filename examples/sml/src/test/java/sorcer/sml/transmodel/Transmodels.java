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

import sml.builder.CollabBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.core.context.model.Transmodel;
import sorcer.core.context.model.ent.Entry;
import sorcer.core.context.model.req.ExploreModel;
import sorcer.service.Context;
import sorcer.service.Evaluator;
import sorcer.service.Request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sorcer.co.operator.*;
import static sorcer.ent.operator.ent;
import static sorcer.ent.operator.expr;
import static sorcer.eo.operator.fi;
import static sorcer.eo.operator.*;
import static sorcer.mo.operator.*;
import static sorcer.so.operator.eval;
import static sorcer.so.operator.exec;
import static sorcer.so.operator.response;

//@Ignore
@RunWith(SorcerTestRunner.class)
@ProjectContext("vml-tutorial")
public class Transmodels {

    private final static Logger logger = LoggerFactory.getLogger(Transmodels.class);


    public static Transmodel getArithmeticTransmodel() throws Exception {

        Transmodel transmodel = tModel("arithmeticTransmodel",
            ent("z1", expr("y3 + result", args("y3", "result"))),
            ent("z2", expr("y1 - y2", args("y1", "y2"))),

            instance("model1", sig(CollabBuilder.class, "getEntryModel")),

            instance("routine1", sig(CollabBuilder.class, "getArithmeticBlock")),

            response("z1", "z2"),
            paths("model1", "routine1"));

        return transmodel;
    }

    public static Transmodel getArithmeticTransmodeMdal() throws Exception {

        Transmodel transmodel = tModel("arithmeticTransmodel",
            ent("z1", expr("y3 + result", args("y3", "result"))),
            ent("z2", expr("y1 - y2", args("y1", "y2"))),

            instance("model1", sig(CollabBuilder.class, "getEntryModel")),

            instance("routine1", sig(CollabBuilder.class, "getArithmeticBlock")),

            response("z1", "z2"),
            paths("model1", "routine1"));

        return transmodel;
    }

    @Test
    public void testArithmeticTransmodel() throws Exception {

        ExploreModel mdl = (ExploreModel)instance(sig(Transmodels.class, "getArithmeticTransmodel"));

        logger.info("y1: " + get(mdl, "z1"));
        Entry z1 = (Entry) get(mdl, "z1");
        assertTrue(asis(z1) instanceof Evaluator);

        logger.info("x3$model1: " + get(mdl, "x3$model1"));
        assertEquals(20.0,  exec(mdl, "x3$model1"));

        logger.info("t5$routine1: " + get(mdl, "t5$routine1"));
        assertEquals(name(get(mdl, "t5$routine1")), "t5");
    }

    @Test
    public void evalArithmeticTransmodel() throws Exception {

        ExploreModel mdl = (ExploreModel) instance(sig(Transmodels.class, "getArithmeticTransmodel"));

        Context rc = eval(mdl);

        logger.info("response context: " + rc);
        assertEquals(800.0, value(rc, "z1"));
        assertEquals(400.0, value(rc, "z2"));
        assertEquals(500.0, value(rc, "y1$model1"));
        assertEquals(100.0, value(rc, "y2$model1"));
        assertEquals(400.0, value(rc, "y3$model1"));
        assertEquals(400.0, value(rc, "result$routine1"));
    }

    @Test
    public void evalArithmeticTransmodelMda() throws Exception {

        // the explicit input context with MDA
        Context mdaCxt = context(mdaFi("arithmeticMdaFi",
            mda("analyzer",
                (Request req, Context cxt) -> {
                    double x1, x2, x3;
                    String dmnName = dmnName(cxt);
                    if (dmnName.equals("model1")) {
                        x1 = (double)value(cxt, "y1");
                        x2 = (double)value(cxt, "y2");
                        setValue(cxt, "ma1", x1/x2);
                    } else if (dmnName.equals("routine1")) {
                        setValue(cxt, "ra1", value(cxt, "result"));
                    } else if (dmnName.equals(name(req))) {
                        x1 = (double)value(cxt, "ma1$model1");
                        x2 = (double)value(cxt, "ra1$routine1");
                        x3 = (double)value(cxt, "arg/x1");
                        setValue(cxt, "tm1", x3/(x1 * x1));
                    }
                }))
        );

        Transmodel mdl = (ExploreModel) instance(sig(Transmodels.class, "getArithmeticTransmodel"));

        Context rc = eval(mdl, mdaCxt);

        logger.info("response context: " + rc);
        // transmodel transformations
        assertEquals(0.4, value(rc, "tm1"));
        assertEquals(5.0, value(rc, "ma1$model1"));
        assertEquals(400.0, value(rc, "ra1$routine1"));

        // domain transformations
        assertEquals(800.0, value(rc, "z1"));
        assertEquals(400.0, value(rc, "z2"));
        assertEquals(500.0, value(rc, "y1$model1"));
        assertEquals(100.0, value(rc, "y2$model1"));
        assertEquals(400.0, value(rc, "y3$model1"));
        assertEquals(400.0, value(rc, "result$routine1"));
    }

}