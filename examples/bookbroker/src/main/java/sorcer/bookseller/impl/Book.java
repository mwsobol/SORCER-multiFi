package sorcer.bookseller.impl;

/**
 * @author   Marco
 */
public class Book implements Comparable<Book>{

    private String title;
    private Double price;

    public Book(String title, double price) {
        this.title = title;
        this.price = price;
    }

    /**
     * @return   Returns the title.
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title   The title to set value.
     */
    public void setTitle(String title) {
        if (title != null) {
            this.title = title;
        }
    }

    /**
     * @return   Returns the price
     */
    public Double getPrice() { return price; }

    /**
     * @param price   The price to set value.
     */
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
