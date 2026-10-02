import java.time.LocalDate;

// Loads demonstration data so the program can be tried out immediately
public class DataSeeder {
    public static void seed(LibraryData data) {
        if (!data.getBooks().isEmpty() || !data.getMembers().isEmpty()) {
            Display.error("Sample data can only be loaded into an empty library.");
            return;
        }
        addBook(data, "9780132350884", "Clean Code", "Robert C. Martin", "Technology", 2008, 3);
        addBook(data, "9780596007126", "Head First Design Patterns", "Eric Freeman", "Technology", 2004, 2);
        addBook(data, "9780134685991", "Effective Java", "Joshua Bloch", "Technology", 2018, 4);
        addBook(data, "9780743273565", "The Great Gatsby", "F. Scott Fitzgerald", "Fiction", 1925, 3);
        addBook(data, "9780061120084", "To Kill a Mockingbird", "Harper Lee", "Fiction", 1960, 2);
        addBook(data, "9780451524935", "Nineteen Eighty-Four", "George Orwell", "Fiction", 1949, 3);
        addBook(data, "9780553380163", "A Brief History of Time", "Stephen Hawking", "Science", 1988, 2);
        addBook(data, "9780062316097", "Sapiens", "Yuval Noah Harari", "History", 2011, 3);
        addBook(data, "9780141439518", "Pride and Prejudice", "Jane Austen", "Classic", 1813, 2);
        addBook(data, "9780316769488", "The Catcher in the Rye", "J. D. Salinger", "Fiction", 1951, 1);

        addMember(data, "Anita Sharma", "9841000001", "anita.sharma@example.com", Member.Type.STUDENT);
        addMember(data, "Bikash Thapa", "9841000002", "bikash.thapa@example.com", Member.Type.STUDENT);
        addMember(data, "Dr. Ramesh Koirala", "9841000003", "ramesh.k@example.com", Member.Type.FACULTY);
        addMember(data, "Sita Gurung", "9841000004", "sita.gurung@example.com", Member.Type.GENERAL);

        LocalDate today = LocalDate.now();
        addIssue(data, "M001", "B001", today.minusDays(3), null);
        addIssue(data, "M003", "B003", today.minusDays(10), null);
        addIssue(data, "M004", "B006", today.minusDays(12), null);
        addIssue(data, "M002", "B004", today.minusDays(30), today.minusDays(10));

        data.save();
        Display.success("Sample data loaded: 10 books, 4 members and 4 issue records.");
    }

    private static void addBook(LibraryData data, String isbn, String title, String author,
                                String category, int year, int copies) {
        data.getBooks().add(new Book(data.nextBookId(), title, author, category, isbn,
                year, copies, copies));
    }

    private static void addMember(LibraryData data, String name, String phone, String email,
                                  Member.Type type) {
        data.getMembers().add(new Member(data.nextMemberId(), name, phone, email, type,
                LocalDate.now().minusMonths(2), true));
    }

    // returnDate == null means the book is still issued; the due date comes from the member's loan period
    private static void addIssue(LibraryData data, String memberId, String bookId,
                                 LocalDate issueDate, LocalDate returnDate) {
        Member m = data.findMember(memberId);
        Book b = data.findBook(bookId);
        LocalDate due = issueDate.plusDays(m.getLoanDays());
        b.issueCopy();
        IssueRecord r = new IssueRecord(data.nextIssueId(), bookId, memberId, issueDate, due,
                null, 0.0, false, 0);
        if (returnDate != null) {
            double fine = FineCalculator.calculate(m.getFinePerDay(), due, returnDate);
            r.markReturned(returnDate, fine);
            b.returnCopy();
        }
        data.getIssues().add(r);
    }
}
