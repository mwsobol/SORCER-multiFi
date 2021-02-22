package sorcer.bookbroker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.bookbroker.impl.BookRequest;
import sorcer.bookseller.impl.Book;
import sorcer.bookseller.impl.BookSeller;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Routine;

import static sorcer.bookbroker.impl.BookRequest.getBookRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sorcer.ent.operator.ent;
import static sorcer.eo.operator.*;
import static sorcer.mo.operator.value;
import static sorcer.so.operator.*;


/**
 * @author Marco de Lannoy Kobayashi
 */
@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/bookbroker")
@SuppressWarnings({"rawtypes", "unchecked"})
public class BookSellerServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(BookSellerServiceTest.class);

    private Book bookTLP;
    private BookRequest requestTLP;

    @Before
    public void setUp() throws Exception {

        // Setup The Little Prince book, costs $9.95
        bookTLP = new Book("The Little Prince", 9.95);

        // Add 100 copies of The Little Prince to the seller inventory
        Routine addTLP = task(
                sig("addBook", BookSeller.class),
                context(types(Book.class, Integer.class),
                        args(bookTLP, 100)));
        addTLP = exert(addTLP);
        Context ctx = context(addTLP);
        logger.info("addTLP context: " + ctx);
        assertEquals(value(ctx, "context/result"), 100);

        // Setup request from John Doe for 100 copies of The Little Prince
        requestTLP = new BookRequest();
        requestTLP.setName("John Doe");
        requestTLP.setBookTitle("The Little Prince");
        requestTLP.setNumCopies(100);
    }

    @After
    public void cleanUp() throws Exception {

        // Remove up to 100 copies of The Little Prince from the seller inventory
        Routine removeTLP = task(
                sig("removeBook", BookSeller.class),
                context(types(Book.class, Integer.class),
                        args(bookTLP, 100)));
        removeTLP = exert(removeTLP);
        logger.info("removeTLP context: " + context(removeTLP));
    }

    @Test
    public void testContextBookTitle() throws ContextException {
        Context requestContext = BookRequest.getContext(requestTLP);
        assertEquals("The Little Prince", getBookRequest(requestContext).getBookTitle());
    }

    @Test
    public void testContextNumCopies() throws ContextException {
        Context requestContext = BookRequest.getContext(requestTLP);
        assertEquals(100, getBookRequest(requestContext).getNumCopies());
    }

    @Test
    public void testMakeBidJob() throws Exception {

        // Create context for bid on request
        Context theLittlePrince = context(
                ent("key", "theLittlePrince"),
                ent("request", BookRequest.getContext(requestTLP)));

        // The seller makes the bid
        Routine tlp = task(sig("makeBid", BookSeller.class), theLittlePrince);
        tlp = exert(tlp);
        theLittlePrince = context(tlp);
        logger.info("job context:" + theLittlePrince);
        logger.info("bid:" + value(theLittlePrince, "bid/value"));
//        Context bid = (Context) value(context(tlp),"bid");
//        logger.info("bid context:" + bid);
    }
}
