package computermaker;

import sorcer.service.Context;

public interface IComputer {
    Context getMemory(Context context);

    Context getProcessor(Context context);

    Context getHardDrive(Context context);
}

