package computermaker;

import sorcer.service.Context;

public interface IComputerBuilder {
    Context mergeParts(Context processor, Context memory, Context hardDrive);
}
