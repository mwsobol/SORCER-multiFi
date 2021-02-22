package sorcer.bookbroker;

//import sorcer.bookbroker.impl.BookRequest;
//import sorcer.bookbroker.impl.BookBid;
import sorcer.service.Context;
import sorcer.service.ContextException;

import java.rmi.RemoteException;

/**
 * Created by Marco de Lannoy Kobayashi on 2/21/21.
 */
@SuppressWarnings("rawtypes")
public interface BookBrokerService {

//    /**
//     * Returns true if request is successfully processed
//     * @param request
//     * @return boolean
//     */
//    public boolean processBookRequest(BookRequest request) throws RemoteException;
//
//    /**
//     * Returns true if bid is successfully processed
//     * @param bid
//     * @return boolean
//     */
//    public boolean processBookBid(BookBid bid) throws RemoteException;


    public Context takeBookRequest(Context context) throws RemoteException, ContextException;

    public Context writeBookRequest(Context context) throws RemoteException, ContextException;

    public Context takeBookBid(Context context) throws RemoteException, ContextException;

    public Context writeBookBid(Context context) throws RemoteException, ContextException;
}
