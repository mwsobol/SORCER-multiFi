package sorcer.arithmetic.provider.impl;

import java.io.Serializable;
import java.rmi.RemoteException;
import sorcer.arithmetic.provider.AntiCheat;
import sorcer.service.Context;
import sorcer.service.ContextException;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Arithmometer implements AntiCheat, Serializable {
    private static final long serialVersionUID = -82252627979263L;
	
	public static final double MAX_VELOCITY = 300.0;

	public static final String VELOCITY = "playerdata/velocity";

	public static final String CHEAT_FLAG = "playerdata/cheat_flag";

	public Context checkPlayer(Context context) throws ContextException, RemoteException {
		final double velocity = (double) context.getValue(VELOCITY);

		if (Math.abs(velocity) > MAX_VELOCITY) {
			context.putValue(CHEAT_FLAG, true);
		}

		return context;
	}
}
