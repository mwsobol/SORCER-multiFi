# Documenting the creation and development of bookbroker example

---

## Description

This bookbroker example is inspired by a theoretical book-ordering system 
described in the [JavaSpaces Service Specification](https://river.apache.org/release-doc/current/specs/html/js-spec.html>).
The description is as follows:

> * A book buyer wants to buy 100 copies of a book. The buyer writes a request for bids into a particular public JavaSpaces service.
>
> * The broker runs a server that takes those requests out of the space and writes them into a JavaSpaces service for each book seller who registered with the broker for that service.
>
> * A server at each book seller removes the requests from its JavaSpaces service, presents the request to a human to prepare a bid, and writes the bid into the space specified in the book buyer's request for bids.
>
> * When the bidding period closes, the buyer takes all the bids from the space and presents them to a human to select the winning bid.

## Initial Setup

#### Directory structure

Before we start writing code, let's set up the directory structure.

First, create the example base directory:
```bash
$ mkdir examples/bookbroker
$ cd examples/bookbroker
```

Next, create the `src` directory. The example has three main components, 
the bookbroker, the bookbuyer, and the bookseller; let's make a separate 
directory for each:
```bash
$ mkdir -p src/main/java/sorcer/bookbroker
$ mkdir -p src/main/java/sorcer/bookseller
$ mkdir -p src/main/java/sorcer/bookbuyer
```

Also, we're going to need tests:
```bash
$ mkdir -p src/test/java/sorcer/bookbroker
```

#### First iteration

[comment]: <> (Next, we want to setup the minimal working example with build.gradle file and testing.)

The end goal is to follow the example in full, which means having a bookbuyer
client, bookseller service, and bookbroker service acting as the middle man
between the other two, but to keep things simple, initially we will implement
only the bookseller and test it. This way we have a starting point to work
from.

Start by creating the bookseller interface, 
`src/main/java/sorcer/bookseller/BookSeller.java`. The job of the bookseller
is to take a request for X number of copies of a book and return a bid. As
such the interface should look something like this:
```java
package sorcer.bookseller;

import sorcer.service.Context;
import sorcer.service.ContextException;

import java.rmi.RemoteException;

@SuppressWarnings({"rawtypes", "unused"})
public interface BookSeller {

    int addBook(Book book, Integer numCopies) throws RemoteException;

    int removeBook(Book book, Integer numCopies) throws RemoteException;

    Context makeBid(Context context) throws RemoteException, ContextException;
}
```
The `makeBid` method will expect a context that contains the request data,
and it will return the context modified to include the bid. The `addBook`
and `removeBook` methods are required to add and remove books from the
bookseller's inventory, although they will not be used by a bookbuyer
client.
> **_NOTE:_** Any method that a client will need to access **must** be
> declared in the interface.

While creating the bookseller interface, we realized that we would need a
`Book` class to encapsulate what a "book" means in our example package.
Our `Book` class will have to serializable so that it may be passed to,
from, and between services. It should also make it be comparable in case
we want to stick them into a HashSet or HashMap (*spoiler*: we will later).
Knowing this, let's create the `Book` class, with title and price
attributes, in `src/main/java/sorcer/bookseller/Book.java`:
```java
package sorcer.bookseller;

import java.io.Serializable;

public class Book implements Serializable, Comparable<Book>{

    private String title;
    private Double price;

    public Book(String title, double price) {
        this.title = title;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        if (title != null) {
            this.title = title;
        }
    }

    public Double getPrice() { 
        return price; 
    }
    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (!(obj instanceof Book)) return false;

        Book that = (Book) obj;
        return this.title.equals(that.title);
    }

    @Override
    public int hashCode(){
        return title.hashCode();
    }

    @Override
    public int compareTo(Book that){
        return this.title.compareTo(that.title);
    }

}
```
> **_NOTE:_** In practice, defining `serialVersionUID` attribute in a
> serializable class is imperative but since this example will only be run
> in the local machine we don't need to worry about it.

Before we can implement the bookseller service class, we need to create
a class to manage the bookseller's inventory of books; as well as classes
to represent the book requests and bids, and handle the conversion to and
from the context objects that SORCER uses as the transport for
communication between services.

The book request class, 
`src/main/java/sorcer/bookbroker/impl/BookRequest.java`:
```java
package sorcer.bookbroker.impl;

import sorcer.core.context.ServiceContext;
import sorcer.service.Context;
import sorcer.service.ContextException;

import java.io.Serializable;
import java.rmi.RemoteException;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BookRequest {

    private String name;
    private String bookTitle;
    private int numCopies;

    public BookRequest() {
        this.name = "";
        this.bookTitle = "";
        this.numCopies = 0;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        if(name != null) {
            this.name = name;
        }
    }

    public String getBookTitle() {
        return bookTitle;
    }
    public void setBookTitle(String bookTitle) {
        if (bookTitle != null) {
            this.bookTitle = bookTitle;
        }
    }

    public int getNumCopies() {
        return numCopies;
    }
    public void setNumCopies(int numCopies) {
        if (numCopies >= 0) {
            this.numCopies = numCopies;
        }
    }

    public boolean equals(BookRequest that) {
        return this.name.equals(that.getName());
    }

    public String toString() {
        return "BookRequest(name=\"" + name
                + "\", bookTitle=\"" + bookTitle
                + "\", numCopies=" + numCopies + ")";
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
```

The book bid class, `src/main/java/sorcer/bookbroker/impl/BookBid.java`:
```java
package sorcer.bookbroker.impl;

import sorcer.core.context.ServiceContext;
import sorcer.service.Context;
import sorcer.service.ContextException;

import java.io.Serializable;
import java.rmi.RemoteException;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BookBid {

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

    public String getName() {
        return name;
    }
    public void setName(String name) {
        if(name != null) {
            this.name = name;
        }
    }

    public String getBookTitle() {
        return bookTitle;
    }
    public void setBookTitle(String bookTitle) {
        if (bookTitle != null) {
            this.bookTitle = bookTitle;
        }
    }

    public int getNumCopies() {
        return numCopies;
    }
    public void setNumCopies(int numCopies) {
        if (numCopies >= 0) {
            this.numCopies = numCopies;
        }
    }

    public double getBidPrice() {
        return bidPrice;
    }
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
        } catch (NullPointerException e) {
            throw new ContextException("Bad bid context: " + context, e);
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
```

The book inventory class, `src/main/java/sorcer/bookseller/impl/BookInventory.java`:
```java
package sorcer.bookseller.impl;

import sorcer.bookseller.Book;

import java.util.HashMap;
import java.util.HashSet;

public class BookInventory {

    private HashSet<Book> books;
    private HashMap<Book, Integer> booksNumCopies;

    public BookInventory() {
        this.books = new HashSet<>();
        this.booksNumCopies = new HashMap<>();
    }

    public Book findBook(String bookTitle) {
        if (this.contains(bookTitle)) {
            for (Book book : this.books) {
                if (book.getTitle().equals(bookTitle))
                    return book;
            }
        }
        return null;
    }

    public int addBook(Book book, int numCopies) {
        if (this.books.contains(book)) {
            numCopies = this.booksNumCopies.get(book) + numCopies;
        }
        else {
            this.books.add(book);
        }
        this.booksNumCopies.put(book, numCopies);
        return numCopies;
    }

    public int removeBook(Book book, int numCopies) {
        if (this.books.contains(book)) {
            numCopies = this.booksNumCopies.get(book) - numCopies;
            if (numCopies < 0) numCopies = 0;
            this.booksNumCopies.put(book, numCopies);
        }
        else {
            numCopies = 0;
        }
        return numCopies;
    }

    public boolean contains(Book book) {
        return this.books.contains(book);
    }

    public boolean contains(String bookTitle) {
        Book queryBook = new Book(bookTitle, 0.0);
        return this.books.contains(queryBook);
    }

    public int getNumCopiesOfBook(Book book) {
        return this.books.contains(book) ? this.booksNumCopies.get(book) : 0;
    }

    public int getNumCopiesOfBook(String bookTitle) {
        Book queryBook = new Book(bookTitle, 0.0);
        return this.contains(queryBook) ? this.booksNumCopies.get(queryBook) : 0;
    }
}
```

Next, create the bookseller service class, 
`src/main/java/sorcer/bookseller/impl/BookSellerService.java`. The
bookseller service implements the bookseller interface we defined earlier.
```bash
mkdir -p src/main/java/sorcer/bookseller/impl
```
```java
package sorcer.bookseller.impl;

import sorcer.bookbroker.impl.BookBid;
import sorcer.bookbroker.impl.BookRequest;
import sorcer.bookseller.Book;
import sorcer.bookseller.BookSeller;
import sorcer.service.Context;
import sorcer.service.ContextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BookSellerService implements BookSeller {

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

                context.putValue("bid", BookBid.getContext(bid));
                if (context.getContextReturn() != null) {
                    context.setReturnValue(bidPrice);
                }
            }
        }

        return context;
    }
}
```


---

TODO/BUGS:
* publishedInterfaces failing exception ambiguous, misleading... 
  was spelling error. Instead of null pointer should have been class 
  not found.
* method not declared in interface... no exception at all... should
  say that method not found in interface
* object needs to be serializable, message should be explicit in that 
  requirement... NotSerializableException
* local vs remote service execution, message should make it obvious
* hands on config file syntax error

[comment]: <> (serial version uid... need to declare final long. java convention)

[comment]: <> (```java)

[comment]: <> (private static final long serialVersionUID = 1L;)

[comment]: <> (```)

[comment]: <> (-dl file should contain files only what client needs)

[comment]: <> (impl backend only)