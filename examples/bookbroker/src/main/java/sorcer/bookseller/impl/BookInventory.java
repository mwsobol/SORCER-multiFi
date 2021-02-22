package sorcer.bookseller.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author   Marco
 */
public class BookInventory {

    private HashSet<Book> books;
    private HashMap<Book, Integer> booksNumCopies;

    public BookInventory() {
        this.books = new HashSet<>();
        this.booksNumCopies = new HashMap<>();
    }

    /**
     * Find book with given bookTitle
     * TODO: ideally, should find all matches for given book template;
     * TODO: there could be equal title but diff versions for example,
     * TODO: but for now book is only comparable by bookTitle...
     * @param bookTitle The book title
     * @return The book found, or null
     */
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
//        System.out.println("BookInventory.add size: " + this.books.size());
        return numCopies;
    }

    public int removeBook(Book book, int numCopies) {
//        System.out.println("BookInventory.remove size: " + this.books.size());
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
//        System.out.println("BookInventory.contains size: " + this.books.size());
        return this.books.contains(book);
    }

    public boolean contains(String bookTitle) {
//        System.out.println("BookInventory.contains size: " + this.books.size());
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
