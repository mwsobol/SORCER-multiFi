package sorcer.bookseller;

//import sorcer.bookbroker.impl.BookRequest;
//import sorcer.bookbroker.impl.BookBid;
import sorcer.service.Context;
import sorcer.service.ContextException;

import java.rmi.RemoteException;

/**
 * Created by Marco de Lannoy Kobayashi on 2/21/21.
 */
@SuppressWarnings("rawtypes")
public interface BookSellerService {

    public Context makeBid(Context context) throws RemoteException, ContextException;
}
