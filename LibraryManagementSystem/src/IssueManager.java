import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IssueManager {
    private static final int MAX_RENEWALS = 2;
    private final LibraryData data;

    public IssueManager(LibraryData data) {
        this.data = data;
    }

    // ---------- helper methods ----------

    private List<IssueRecord> activeIssuesOf(String memberId) {
        List<IssueRecord> list = new ArrayList<>();
        for (IssueRecord r : data.getIssues()) {
            if (r.getMemberId().equals(memberId) && !r.isReturned()) {
                list.add(r);
            }
        }
        return list;
    }

    private double outstandingFine(Member m) {
        double total = 0;
        LocalDate today = LocalDate.now();
        for (IssueRecord r : data.getIssues()) {
            if (!r.getMemberId().equals(m.getId())) {
                continue;
            }
            if (r.hasUnpaidFine()) {
                total += r.getFine();
            } else if (r.isOverdue(today)) {
                total += r.currentFine(m.getFinePerDay(), today);
            }
        }
        return total;
    }

    private boolean memberHasBook(String memberId, String bookId) {
        for (IssueRecord r : activeIssuesOf(memberId)) {
            if (r.getBookId().equals(bookId)) {
                return true;
            }
        }
        return false;
    }

    private IssueRecord findIssue(String issueId) {
        for (IssueRecord r : data.getIssues()) {
            if (r.getIssueId().equalsIgnoreCase(issueId)) {
                return r;
            }
        }
        return null;
    }

    private void printIssueHeader() {
        System.out.println(String.format("%-7s %-22s %-14s %-11s %-11s %-11s %9s",
                "IssueID", "Book", "Member", "Issued", "Due", "Returned", "Fine"));
        Display.line();
    }

    private void printIssueRow(IssueRecord r) {
        Member m = data.findMember(r.getMemberId());
        double rate = (m == null) ? 5.0 : m.getFinePerDay();
        String returned = r.isReturned() ? r.getReturnDate().toString() : "-";
        double fine = r.currentFine(rate, LocalDate.now());
        String fineText = (fine > 0) ? String.format("%.2f", fine) : "0.00";
        System.out.println(String.format("%-7s %-22s %-14s %-11s %-11s %-11s %9s",
                r.getIssueId(), Display.truncate(data.bookTitle(r.getBookId()), 22),
                Display.truncate(data.memberName(r.getMemberId()), 14),
                r.getIssueDate(), r.getDueDate(), returned, fineText));
    }

    private void printIssueTable(List<IssueRecord> list) {
        if (list.isEmpty()) {
            Display.info("No records found.");
            return;
        }
        printIssueHeader();
        for (IssueRecord r : list) {
            printIssueRow(r);
        }
        Display.line();
        System.out.println("Total records: " + list.size());
    }

    // ---------- circulation operations ----------

    public void issueBook() {
        Display.title("ISSUE BOOK");
        Member m = data.findMember(InputHelper.readText("Member ID: "));
        if (m == null) {
            Display.error("Member not found.");
            return;
        }
        if (!m.isActive()) {
            Display.error("This membership is inactive. Books cannot be issued.");
            return;
        }
        if (data.activeIssueCountForMember(m.getId()) >= m.getMaxBooks()) {
            Display.error("Borrowing limit reached (" + m.getMaxBooks() + " books for "
                    + m.getType() + " members).");
            return;
        }
        double owed = outstandingFine(m);
        if (owed > 0) {
            Display.error("Member has unpaid/overdue fines of " + Display.money(owed)
                    + ". Clear them before borrowing.");
            return;
        }
        Book b = data.findBook(InputHelper.readText("Book ID: "));
        if (b == null) {
            Display.error("Book not found.");
            return;
        }
        if (!b.isAvailable()) {
            Display.error("No copies of '" + b.getTitle() + "' are currently available.");
            return;
        }
        if (memberHasBook(m.getId(), b.getId())) {
            Display.error("This member already has a copy of this book.");
            return;
        }
        LocalDate issueDate = InputHelper.readDate("Issue date (yyyy-MM-dd) [Enter = today]: ");
        LocalDate dueDate = issueDate.plusDays(m.getLoanDays());
        b.issueCopy();
        IssueRecord record = new IssueRecord(data.nextIssueId(), b.getId(), m.getId(),
                issueDate, dueDate, null, 0.0, false, 0);
        data.getIssues().add(record);
        data.save();
        Display.success("Book issued successfully.");
        Display.line();
        System.out.println("Issue ID : " + record.getIssueId());
        System.out.println("Book     : " + b.getTitle());
        System.out.println("Member   : " + m.getName() + " (" + m.getId() + ")");
        System.out.println("Issued   : " + issueDate);
        System.out.println("Due Date : " + dueDate);
        Display.line();
    }

    public void returnBook() {
        Display.title("RETURN BOOK");
        Member m = data.findMember(InputHelper.readText("Member ID: "));
        if (m == null) {
            Display.error("Member not found.");
            return;
        }
        List<IssueRecord> active = activeIssuesOf(m.getId());
        if (active.isEmpty()) {
            Display.info("This member has no books to return.");
            return;
        }
        printIssueTable(active);
        IssueRecord record = findIssue(InputHelper.readText("Issue ID to return: "));
        if (record == null || !active.contains(record)) {
            Display.error("That Issue ID is not an active issue for this member.");
            return;
        }
        LocalDate returnDate = InputHelper.readDate("Return date (yyyy-MM-dd) [Enter = today]: ");
        if (returnDate.isBefore(record.getIssueDate())) {
            Display.error("Return date cannot be before the issue date.");
            return;
        }
        double fine = FineCalculator.calculate(m.getFinePerDay(), record.getDueDate(), returnDate);
        long lateDays = FineCalculator.overdueDays(record.getDueDate(), returnDate);
        record.markReturned(returnDate, fine);
        Book b = data.findBook(record.getBookId());
        if (b != null) {
            b.returnCopy();
        }
        Display.success("Book returned successfully.");
        if (fine > 0) {
            System.out.println("Returned " + lateDays + " day(s) late. Fine: " + Display.money(fine));
            if (InputHelper.readYesNo("Collect the fine now? (y/n): ")) {
                record.markFinePaid();
                Display.success("Fine collected.");
            } else {
                Display.info("Fine left unpaid; it must be paid before the next issue.");
            }
        }
        data.save();
    }

    public void renewBook() {
        Display.title("RENEW BOOK");
        IssueRecord r = findIssue(InputHelper.readText("Issue ID: "));
        if (r == null || r.isReturned()) {
            Display.error("No active issue found with that ID.");
            return;
        }
        Member m = data.findMember(r.getMemberId());
        if (m == null) {
            Display.error("Member record missing.");
            return;
        }
        if (r.isOverdue(LocalDate.now())) {
            Display.error("Overdue books cannot be renewed. Please return the book.");
            return;
        }
        if (r.getRenewCount() >= MAX_RENEWALS) {
            Display.error("Maximum number of renewals (" + MAX_RENEWALS + ") reached.");
            return;
        }
        r.extendDueDate(m.getLoanDays());
        data.save();
        Display.success("Renewed. New due date is " + r.getDueDate()
                + " (renewals used: " + r.getRenewCount() + "/" + MAX_RENEWALS + ").");
    }

    public void payFine() {
        Display.title("PAY FINE");
        Member m = data.findMember(InputHelper.readText("Member ID: "));
        if (m == null) {
            Display.error("Member not found.");
            return;
        }
        List<IssueRecord> unpaid = new ArrayList<>();
        for (IssueRecord r : data.getIssues()) {
            if (r.getMemberId().equals(m.getId()) && r.hasUnpaidFine()) {
                unpaid.add(r);
            }
        }
        if (unpaid.isEmpty()) {
            Display.info("No unpaid fines for this member on returned books.");
            return;
        }
        printIssueTable(unpaid);
        double total = 0;
        for (IssueRecord r : unpaid) {
            total += r.getFine();
        }
        System.out.println("Total unpaid: " + Display.money(total));
        if (InputHelper.readYesNo("Collect all unpaid fines now? (y/n): ")) {
            for (IssueRecord r : unpaid) {
                r.markFinePaid();
            }
            data.save();
            Display.success("Payment of " + Display.money(total) + " recorded. Receipt printed.");
        } else {
            Display.info("No payment recorded.");
        }
    }

    // ---------- viewing records ----------

    public void viewAllIssues() {
        Display.title("ISSUE / RETURN RECORDS");
        System.out.println("1. All records");
        System.out.println("2. Currently issued only");
        System.out.println("3. Returned only");
        int choice = InputHelper.readChoice(1, 3);
        List<IssueRecord> list = new ArrayList<>();
        for (IssueRecord r : data.getIssues()) {
            if (choice == 1 || (choice == 2 && !r.isReturned()) || (choice == 3 && r.isReturned())) {
                list.add(r);
            }
        }
        System.out.println();
        printIssueTable(list);
    }

    public void viewMemberHistory() {
        Display.title("MEMBER BORROWING HISTORY");
        Member m = data.findMember(InputHelper.readText("Member ID: "));
        if (m == null) {
            Display.error("Member not found.");
            return;
        }
        System.out.println("History for " + m.getName() + " (" + m.getId() + ")");
        List<IssueRecord> list = new ArrayList<>();
        for (IssueRecord r : data.getIssues()) {
            if (r.getMemberId().equals(m.getId())) {
                list.add(r);
            }
        }
        printIssueTable(list);
    }

    public void viewBookHistory() {
        Display.title("BOOK ISSUE HISTORY");
        Book b = data.findBook(InputHelper.readText("Book ID: "));
        if (b == null) {
            Display.error("Book not found.");
            return;
        }
        System.out.println("History for '" + b.getTitle() + "' (" + b.getId() + ")");
        List<IssueRecord> list = new ArrayList<>();
        for (IssueRecord r : data.getIssues()) {
            if (r.getBookId().equals(b.getId())) {
                list.add(r);
            }
        }
        printIssueTable(list);
    }

    public void viewOverdue() {
        Display.title("OVERDUE BOOKS");
        LocalDate today = LocalDate.now();
        List<IssueRecord> list = new ArrayList<>();
        for (IssueRecord r : data.getIssues()) {
            if (r.isOverdue(today)) {
                list.add(r);
            }
        }
        printIssueTable(list);
    }
}
