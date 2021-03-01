package sorcer.bookseller;

import sorcer.service.Context;
import sorcer.service.ContextException;

import java.rmi.RemoteException;

/**
 * Created by Marco de Lannoy Kobayashi on 2/21/21.
 */
@SuppressWarnings({"rawtypes", "unused"})
public interface BookSeller {

    int addBook(Book book, Integer numCopies) throws RemoteException;

    int removeBook(Book book, Integer numCopies) throws RemoteException;

    Context makeBid(Context context) throws RemoteException, ContextException;
}
