package sorcer.bookseller.impl;

import sorcer.bookseller.BookSellerService;
import sorcer.service.Context;
import sorcer.service.ContextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

/**
 * @author   Marco
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class BookSeller implements BookSellerService{

    private final static Logger logger = LoggerFactory.getLogger(BookSeller.class);

    private BookInventory bookInventory;

    public BookSeller() {
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

        String requestKey = (String) context.getValue("key");
        Context requestContext = (Context) context.getValue("request");

        if (requestContext != null) {

            String bookTitle = (String) requestContext.getValue("bookTitle");
            int numCopies = (int) requestContext.getValue("numCopies");
            logger.info("Processing request for " + numCopies + " copies of book titled \"" + bookTitle + "\"");

            if (this.bookInventory.contains(bookTitle)) {

                int numAvailCopies = this.bookInventory.getNumCopiesOfBook(bookTitle);
                double bidPrice = calculateBidPrice(bookTitle, numCopies);

                logger.info("Making $" + bidPrice + " bid for the title \"" + bookTitle + "\"");
                context.putValue("bid/price", bidPrice);
                context.putValue("bid/numCopies", numAvailCopies);
                if (context.getContextReturn() != null) {
                    context.setReturnValue(bidPrice);
                }
            }
        }

        return context;
    }
}
