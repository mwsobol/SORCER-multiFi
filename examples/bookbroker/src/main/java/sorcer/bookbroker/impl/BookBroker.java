package sorcer.bookbroker.impl;

import sorcer.bookbroker.BookBrokerService;
import sorcer.service.Context;
import sorcer.service.ContextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * TODO: implement and use in bookbroker example
 * @author   Marco
 */
@SuppressWarnings("rawtypes")
public class BookBroker implements BookBrokerService {

    private final static Logger logger = LoggerFactory.getLogger(BookBroker.class);

    public BookBroker() {}

//    /**
//     * Returns true if request is successfully processed
//     * @param request
//     * @return boolean
//     */
//    public boolean processBookRequest(BookRequest request) throws RemoteException {
//
//        return true;
//    }
//
//    /**
//     * Returns true if bid is successfully processed
//     * @param bid
//     * @return boolean
//     */
//    public boolean processBookBid(BookBid bid) throws RemoteException {
//
//        return true;
//    }

    @Override
    public Context takeBookRequest(Context context) throws RemoteException, ContextException {
//        BookRequest request = BookRequest.getBookRequest(context);
        return context;
    }

    @Override
    public Context writeBookRequest(Context context) throws RemoteException, ContextException {
        return context;
    }

    @Override
    public Context takeBookBid(Context context) throws RemoteException, ContextException {
        return context;
    }

    @Override
    public Context writeBookBid(Context context) throws RemoteException, ContextException {
        return context;
    }

}
