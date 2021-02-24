package sorcer.bookseller.impl;

import sorcer.bookbroker.impl.BookBid;
import sorcer.bookbroker.impl.BookRequest;
import sorcer.bookseller.Book;
import sorcer.service.Context;
import sorcer.service.ContextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

/**
 * @author   Marco
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class BookSellerService implements sorcer.bookseller.BookSeller {

    private final static Logger logger = LoggerFactory.getLogger(BookSellerService.class);

    private BookInventory bookInventory;

    public BookSellerService() {
        this.bookInventory = new BookInventory();
    }

    /**
     * Add numCopies of book
     * @param book The book template to add
     * @param numCopies Number of copies to add
     * @return Number of copies after adding
     */
    public int addBook(Book book, Integer numCopies) {
        logger.info("Adding " + numCopies + " copies of book titled \"" + book.getTitle() + "\"");
        return this.bookInventory.addBook(book, numCopies);
    }

    /**
     * Remove numCopies of book
     * @param book The book template to remove
     * @param numCopies Number of copies to remove
     * @return Number of copies after removing
     */
    public int removeBook(Book book, Integer numCopies) {
        logger.info("Removing " + numCopies + " copies of book titled \"" + book.getTitle() + "\"");
        return this.bookInventory.removeBook(book, numCopies);
    }

    /**
     * Calculate bid price for numCopies of book with bookTitle (assumes book is in inventory)
     * @param bookTitle The book title
     * @param numCopies Number of copies
     * @return The bid price
     */
    public double calculateBidPrice(String bookTitle, int numCopies) {
        double bookPrice = this.bookInventory.findBook(bookTitle).getPrice();
        return numCopies * bookPrice;
    }

    /**
     * Make a bid
     * @param context The context in
     * @return The context out
     * @throws RemoteException
     * @throws ContextException
     */
    public Context makeBid(Context context) throws RemoteException, ContextException {

//        String key = (String) context.getValue("key");
        Context requestContext = (Context) context.getValue("request");

        if (requestContext != null) {

            BookRequest request = BookRequest.getBookRequest(requestContext);
            String bookTitle = request.getBookTitle();
            int numCopies = request.getNumCopies();
            logger.info("Processing request for " + numCopies + " copies of book titled \"" + bookTitle + "\"");

            if (this.bookInventory.contains(bookTitle)) {

                int numAvailCopies = this.bookInventory.getNumCopiesOfBook(bookTitle);
                if (numAvailCopies < numCopies) {
                    numCopies = numAvailCopies;
                }
                double bidPrice = calculateBidPrice(bookTitle, numCopies);

                BookBid bid = new BookBid();
                bid.setName(request.getName());
                bid.setBookTitle(bookTitle);
                bid.setNumCopies(numCopies);
                bid.setBidPrice(bidPrice);
                logger.info("Making $" + bidPrice + " bid for " + numCopies + "copies of \"" + bookTitle + "\"");

//                context.putValue("bid/price", bidPrice);
//                context.putValue("bid/numCopies", numAvailCopies);
                context.putValue("bid", BookBid.getContext(bid));
                if (context.getContextReturn() != null) {
                    context.setReturnValue(bidPrice);
                }
            }
        }

        return context;
    }
}
