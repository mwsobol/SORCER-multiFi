package sorcer.bookbroker.impl;

import sorcer.core.context.ServiceContext;
import sorcer.service.Context;
import sorcer.service.ContextException;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * @author   Marco
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class BookRequest implements Serializable {

    private String name;
    private String bookTitle;
    private int numCopies;

    public BookRequest() {
        this.name = "";
        this.bookTitle = "";
        this.numCopies = 0;
    }

    /**
     * @return   Returns the key.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name   The key to setValue.
     */
    public void setName(String name) {
        if(name != null) {
            this.name = name;
        }
    }

    /**
     * @return   Returns the bookTitle.
     */
    public String getBookTitle() {
        return bookTitle;
    }
    /**
     * @param bookTitle   The bookTitle to setValue.
     */
    public void setBookTitle(String bookTitle) {
        if (bookTitle != null) {
            this.bookTitle = bookTitle;
        }
    }

    /**
     * @return   Returns the numCopies.
     */
    public int getNumCopies() {
        return numCopies;
    }
    /**
     * @param numCopies   The numCopies to setValue.
     */
    public void setNumCopies(int numCopies) {
        if (numCopies >= 0) {
            this.numCopies = numCopies;
        }
    }

    public boolean equals(BookRequest that) {
        return this.name.equals(that.getName());
    }

    public String toString() {
        return name;
    }

    static public BookRequest getBookRequest(Context context) throws ContextException {
        BookRequest request = new BookRequest();
        try {
            request.name = (String) context.getValue("key");
            request.bookTitle = (String) context.getValue("bookTitle");
            request.numCopies = (int) context.getValue("numCopies");
        } catch (RemoteException e) {
            throw new ContextException(e);
        }
        return request;
    }

    static public Context getContext(BookRequest request) throws ContextException {
        Context cxt = new ServiceContext();
        cxt.putValue("key", request.getName());
        cxt.putValue("bookTitle", request.getBookTitle());
        cxt.putValue("numCopies", request.getNumCopies());
        return cxt;
    }
}
