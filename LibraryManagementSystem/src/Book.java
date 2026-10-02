public class Book {
    private final String id;
    private String title;
    private String author;
    private String category;
    private final String isbn;
    private int year;
    private int totalCopies;
    private int availableCopies;

    public Book(String id, String title, String author, String category, String isbn,
                int year, int totalCopies, int availableCopies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.isbn = isbn;
        this.year = year;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }

    public String getIsbn() {
        return isbn;
    }

    public int getYear() {
        return year;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public int getIssuedCopies() {
        return totalCopies - availableCopies;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    public boolean issueCopy() {
        if (availableCopies <= 0) {
            return false;
        }
        availableCopies--;
        return true;
    }

    public void returnCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    public void addCopies(int count) {
        totalCopies += count;
        availableCopies += count;
    }

    public boolean matches(String keyword) {
        String k = keyword.toLowerCase();
        return title.toLowerCase().contains(k)
                || author.toLowerCase().contains(k)
                || category.toLowerCase().contains(k)
                || isbn.toLowerCase().contains(k)
                || id.toLowerCase().equals(k);
    }

    public String toFileString() {
        return id + "|" + title + "|" + author + "|" + category + "|" + isbn + "|"
                + year + "|" + totalCopies + "|" + availableCopies;
    }

    public static Book fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length != 8) {
            throw new IllegalArgumentException("Invalid book record: " + line);
        }
        return new Book(p[0], p[1], p[2], p[3], p[4], Integer.parseInt(p[5]),
                Integer.parseInt(p[6]), Integer.parseInt(p[7]));
    }

    public static void printHeader() {
        System.out.println(String.format("%-6s %-28s %-20s %-13s %-5s %-6s %-5s",
                "ID", "Title", "Author", "Category", "Year", "Total", "Avail"));
        Display.line();
    }

    public void printRow() {
        System.out.println(String.format("%-6s %-28s %-20s %-13s %-5d %-6d %-5d",
                id, Display.truncate(title, 28), Display.truncate(author, 20),
                Display.truncate(category, 13), year, totalCopies, availableCopies));
    }
}
