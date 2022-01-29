package computermaker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.service.Block;
import sorcer.service.Context;
import sorcer.service.Signature;
import sorcer.service.Task;
import sorcer.service.modeling.Model;
import static org.junit.Assert.assertTrue;
import static sorcer.co.operator.*;
import static sorcer.ent.operator.*;
import static sorcer.eo.operator.*;
import static sorcer.eo.operator.context;
import static sorcer.mo.operator.model;
import static sorcer.so.operator.exert;
import static sorcer.so.operator.response;

@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/computermaker")
public class ComputerMakerTest {
    private final static Logger logger = LoggerFactory.getLogger(ComputerMakerTest.class);

    @Test
    public void evaluatorEntryModel() throws Exception {

        Model mdl = model(
                ent("getProcessor", IProcessor.class, args("processor")),
                ent("getMemory", IMemory.class, args("memory")),
                ent("getHardDrive", IHardDrive.class, args("hard_drive")),
                ent("mergeParts", IComputerBuilder.class, args("getProcessor", "getMemory", "getHardDrive")),
                ent("getComputer", IComputerService.class, args("mergeParts")),
                response("getComputer"));

        Context out = response(mdl);
        logger.info("out: " + out);
        logger.info("model response: " + out);
        assertTrue(isCkpt(out));
    }

    @Test
    public void StructuredBlock() throws Exception {

        Task processor = task("getProcessor", sig("getProcessor", IProcessor.class),
                context("getProcessor", result("block/processor", Signature.Direction.IN)));

        Task memory = task("getMemory", sig("getMemory", IMemory.class),
                context("getMemory", result("block/getMemory", Signature.Direction.IN)));

        Task hard_drive = task("getHardDive", sig("getHardDive", IHardDrive.class),
                context("getHardDive", result("block/getHardDive", Signature.Direction.IN)));

        Task computerBuilder = task("buildComputer", sig("mergeParts", IComputerBuilder.class),
                context("buildComputer", result("block/buildComputer", Signature.Direction.OUT)));

        Block block = block("computer", block(processor, memory, hard_drive), computerBuilder);

        Block result = exert(block);
    }
}
