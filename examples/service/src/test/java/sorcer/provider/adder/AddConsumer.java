package sorcer.provider.adder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.core.consumer.ServiceConsumer;
import sorcer.requestor.adder.AdderConsumer;
import sorcer.service.Consumer;
import sorcer.service.Context;

import static org.junit.Assert.assertEquals;
import static sorcer.eo.operator.*;
import static sorcer.mo.operator.value;
import static sorcer.so.operator.exec;

/**
 * @author Mike Sobolewski
 */
@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/service")
public class AddConsumer {
	private final static Logger logger = LoggerFactory.getLogger(AddConsumer.class);

	@Test
	public void adderRequestorAsService() throws Exception {

		Consumer req = consumer(AdderConsumer.class, "routine");
//		Consumer req = consumer(AdderConsumer.class, "netlet");

		Context cxt = (Context) exec(req);

		logger.info("out context: " + cxt);
		logger.info("context @ arg/x1: " + get(cxt, "arg/x1"));
		logger.info("context @ arg/x2: " + value(cxt, "arg/x2"));
		logger.info("context @ out/y: " + value(cxt, "out/y"));

		// getValue a single context argument
		assertEquals(300.0, value(cxt, "out/y"));
	}

}
	
	
