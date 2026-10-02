import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class BookManager {
    private final LibraryData data;

    public BookManager(LibraryData data) {
        this.data = data;
    }

    public void addBook() {
        Display.title("ADD NEW BOOK");
        String isbn = InputHelper.readText("ISBN: ");
        Book existing = data.findBookByIsbn(isbn);
        if (existing != null) {
            Display.error("A book with this ISBN already exists: " + existing.getId()
                    + " - " + existing.getTitle());
            if (InputHelper.readYesNo("Add more copies to the existing book instead? (y/n): ")) {
                addCopiesTo(existing);
            }
            return;
        }
        String title = InputHelper.readText("Title: ");
        String author = InputHelper.readText("Author: ");
        String category = InputHelper.readText("Category (e.g. Fiction, Science): ");
        int year = InputHelper.readInt("Publication year (1000-" + Year.now().getValue() + "): ",
                1000, Year.now().getValue());
        int copies = InputHelper.readInt("Number of copies (1-100): ", 1, 100);
        Book book = new Book(data.nextBookId(), title, author, category, isbn, year, copies, copies);
        data.getBooks().add(book);
        data.save();
        Display.success("Book added successfully with ID " + book.getId());
    }

    public void displayAllBooks() {
        Display.title("ALL BOOKS");
        printBookTable(data.getBooks());
    }

    private void printBookTable(List<Book> list) {
        if (list.isEmpty()) {
            Display.info("No books found.");
            return;
        }
        Book.printHeader();
        for (Book b : list) {
            b.printRow();
        }
        Display.line();
        System.out.println("Total records: " + list.size());
    }

    public void viewBookDetails() {
        Display.title("BOOK DETAILS");
        Book b = data.findBook(InputHelper.readText("Enter Book ID: "));
        if (b == null) {
            Display.error("Book not found.");
            return;
        }
        System.out.println("Book ID          : " + b.getId());
        System.out.println("Title            : " + b.getTitle());
        System.out.println("Author           : " + b.getAuthor());
        System.out.println("Category         : " + b.getCategory());
        System.out.println("ISBN             : " + b.getIsbn());
        System.out.println("Publication Year : " + b.getYear());
        System.out.println("Total Copies     : " + b.getTotalCopies());
        System.out.println("Available Copies : " + b.getAvailableCopies());
        System.out.println("Currently Issued : " + b.getIssuedCopies());
        System.out.println("Times Issued     : " + countTimesIssued(b.getId()));
        Display.line();
        System.out.println("Currently held by:");
        boolean any = false;
        for (IssueRecord r : data.getIssues()) {
            if (r.getBookId().equals(b.getId()) && !r.isReturned()) {
                System.out.println("  " + r.getMemberId() + " - " + data.memberName(r.getMemberId())
                        + " (due " + r.getDueDate() + ")");
                any = true;
            }
        }
        if (!any) {
            System.out.println("  Nobody - all copies are on the shelf.");
        }
    }

    private int countTimesIssued(String bookId) {
        int count = 0;
        for (IssueRecord r : data.getIssues()) {
            if (r.getBookId().equals(bookId)) {
                count++;
            }
        }
        return count;
    }

    public void updateBook() {
        Display.title("UPDATE BOOK");
        Book b = data.findBook(InputHelper.readText("Enter Book ID to update: "));
        if (b == null) {
            Display.error("Book not found.");
            return;
        }
        System.out.println("Press Enter to keep the current value.");
        b.setTitle(InputHelper.readOptional("Title", b.getTitle()));
        b.setAuthor(InputHelper.readOptional("Author", b.getAuthor()));
        b.setCategory(InputHelper.readOptional("Category", b.getCategory()));
        String yearText = InputHelper.readLine("Year [" + b.getYear() + "]: ");
        if (!yearText.isEmpty()) {
            try {
                int year = Integer.parseInt(yearText);
                if (year < 1000 || year > Year.now().getValue()) {
                    Display.error("Year out of range. Old year kept.");
                } else {
                    b.setYear(year);
                }
            } catch (NumberFormatException e) {
                Display.error("Invalid year. Old year kept.");
            }
        }
        data.save();
        Display.success("Book updated successfully.");
    }

    public void deleteBook() {
        Display.title("DELETE BOOK");
        Book b = data.findBook(InputHelper.readText("Enter Book ID to delete: "));
        if (b == null) {
            Display.error("Book not found.");
            return;
        }
        if (data.hasHistoryForBook(b.getId())) {
            Display.error("This book has issue history and cannot be deleted.");
            return;
        }
        if (InputHelper.readYesNo("Delete '" + b.getTitle() + "'? (y/n): ")) {
            data.getBooks().remove(b);
            data.save();
            Display.success("Book deleted.");
        } else {
            Display.info("Deletion cancelled.");
        }
    }

    public void addCopies() {
        Display.title("ADD COPIES");
        Book b = data.findBook(InputHelper.readText("Enter Book ID: "));
        if (b == null) {
            Display.error("Book not found.");
            return;
        }
        addCopiesTo(b);
    }

    private void addCopiesTo(Book b) {
        int count = InputHelper.readInt("Copies to add (1-100): ", 1, 100);
        b.addCopies(count);
        data.save();
        Display.success("Added " + count + " copies. Total is now " + b.getTotalCopies() + ".");
    }

    public void searchBooks() {
        Display.title("SEARCH BOOKS");
        System.out.println("1. Search by keyword (title / author / category / ISBN / ID)");
        System.out.println("2. Filter by category");
        System.out.println("3. Show only available books");
        int choice = InputHelper.readChoice(1, 3);
        List<Book> results = new ArrayList<>();
        if (choice == 1) {
            String keyword = InputHelper.readText("Keyword: ");
            for (Book b : data.getBooks()) {
                if (b.matches(keyword)) {
                    results.add(b);
                }
            }
        } else if (choice == 2) {
            TreeSet<String> categories = new TreeSet<>();
            for (Book b : data.getBooks()) {
                categories.add(b.getCategory());
            }
            System.out.println("Available categories: " + categories);
            String category = InputHelper.readText("Category: ");
            for (Book b : data.getBooks()) {
                if (b.getCategory().equalsIgnoreCase(category)) {
                    results.add(b);
                }
            }
        } else {
            for (Book b : data.getBooks()) {
                if (b.isAvailable()) {
                    results.add(b);
                }
            }
        }
        System.out.println();
        printBookTable(results);
    }

    public void sortBooks() {
        Display.title("SORT BOOKS");
        System.out.println("1. By title");
        System.out.println("2. By author");
        System.out.println("3. By publication year (newest first)");
        System.out.println("4. By category");
        System.out.println("5. By available copies (most first)");
        int choice = InputHelper.readChoice(1, 5);
        List<Book> sorted = new ArrayList<>(data.getBooks());
        switch (choice) {
            case 1:
                sorted.sort(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
                break;
            case 2:
                sorted.sort(Comparator.comparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER));
                break;
            case 3:
                sorted.sort(Comparator.comparingInt(Book::getYear).reversed());
                break;
            case 4:
                sorted.sort(Comparator.comparing(Book::getCategory, String.CASE_INSENSITIVE_ORDER));
                break;
            default:
                sorted.sort(Comparator.comparingInt(Book::getAvailableCopies).reversed());
                break;
        }
        System.out.println();
        printBookTable(sorted);
    }
}
