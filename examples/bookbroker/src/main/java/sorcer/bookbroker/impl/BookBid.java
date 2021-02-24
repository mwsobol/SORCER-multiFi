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
public class BookBid implements Serializable {

    private String name;
    private String bookTitle;
    private int numCopies;
    private double bidPrice;

    public BookBid() {
        this.name = "";
        this.bookTitle = "";
        this.numCopies = 0;
        this.bidPrice = 0.0;
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

    /**
     * @return   Returns the bidPrice.
     */
    public double getBidPrice() {
        return bidPrice;
    }
    /**
     * @param bidPrice   The bidPrice to setValue.
     */
    public void setBidPrice(double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public boolean equals(BookBid that) {
        return this.name.equals(that.getName());
    }

    public String toString() {
        return "BookBid(name=\"" + name
                + "\", bookTitle=\"" + bookTitle
                + "\", numCopies=" + numCopies + ")";
    }

    static public BookBid getBookBid(Context context) throws ContextException {
        BookBid bid = new BookBid();
        try {
            bid.name = (String) context.getValue("key");
            bid.bookTitle = (String) context.getValue("bookTitle");
            bid.numCopies = (int) context.getValue("numCopies");
            bid.bidPrice = (double) context.getValue("bidPrice");
        } catch (RemoteException e) {
            throw new ContextException(e);
        }
        return bid;
    }

    static public Context getContext(BookBid bid) throws ContextException {
        Context cxt = new ServiceContext();
        cxt.putValue("key", bid.getName());
        cxt.putValue("bookTitle", bid.getBookTitle());
        cxt.putValue("numCopies", bid.getNumCopies());
        cxt.putValue("bidPrice", bid.getBidPrice());
        return cxt;
    }
}
