import java.util.List;

// Holds all in-memory records and provides shared lookup helpers for the managers
public class LibraryData {
    private final List<Book> books;
    private final List<Member> members;
    private final List<IssueRecord> issues;

    public LibraryData() {
        books = FileManager.loadBooks();
        members = FileManager.loadMembers();
        issues = FileManager.loadIssues();
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<Member> getMembers() {
        return members;
    }

    public List<IssueRecord> getIssues() {
        return issues;
    }

    public Book findBook(String id) {
        for (Book b : books) {
            if (b.getId().equalsIgnoreCase(id)) {
                return b;
            }
        }
        return null;
    }

    public Book findBookByIsbn(String isbn) {
        for (Book b : books) {
            if (b.getIsbn().equalsIgnoreCase(isbn)) {
                return b;
            }
        }
        return null;
    }

    public Member findMember(String id) {
        for (Member m : members) {
            if (m.getId().equalsIgnoreCase(id)) {
                return m;
            }
        }
        return null;
    }

    public Member findMemberByEmail(String email) {
        for (Member m : members) {
            if (m.getEmail().equalsIgnoreCase(email)) {
                return m;
            }
        }
        return null;
    }

    public String bookTitle(String bookId) {
        Book b = findBook(bookId);
        return (b == null) ? "(deleted)" : b.getTitle();
    }

    public String memberName(String memberId) {
        Member m = findMember(memberId);
        return (m == null) ? "(deleted)" : m.getName();
    }

    public int activeIssueCountForMember(String memberId) {
        int count = 0;
        for (IssueRecord r : issues) {
            if (r.getMemberId().equals(memberId) && !r.isReturned()) {
                count++;
            }
        }
        return count;
    }

    public int activeIssueCountForBook(String bookId) {
        int count = 0;
        for (IssueRecord r : issues) {
            if (r.getBookId().equals(bookId) && !r.isReturned()) {
                count++;
            }
        }
        return count;
    }

    public boolean hasHistoryForBook(String bookId) {
        for (IssueRecord r : issues) {
            if (r.getBookId().equals(bookId)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasHistoryForMember(String memberId) {
        for (IssueRecord r : issues) {
            if (r.getMemberId().equals(memberId)) {
                return true;
            }
        }
        return false;
    }

    private int numericPart(String id) {
        try {
            return Integer.parseInt(id.substring(1));
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return 0;
        }
    }

    public String nextBookId() {
        int max = 0;
        for (Book b : books) {
            max = Math.max(max, numericPart(b.getId()));
        }
        return String.format("B%03d", max + 1);
    }

    public String nextMemberId() {
        int max = 0;
        for (Member m : members) {
            max = Math.max(max, numericPart(m.getId()));
        }
        return String.format("M%03d", max + 1);
    }

    public String nextIssueId() {
        int max = 0;
        for (IssueRecord r : issues) {
            max = Math.max(max, numericPart(r.getIssueId()));
        }
        return String.format("I%04d", max + 1);
    }

    public void save() {
        FileManager.saveBooks(books);
        FileManager.saveMembers(members);
        FileManager.saveIssues(issues);
    }
}
