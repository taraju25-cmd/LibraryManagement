import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReportManager {
    private final LibraryData data;

    public ReportManager(LibraryData data) {
        this.data = data;
    }

    public void summaryReport() {
        Display.title("LIBRARY SUMMARY REPORT  (" + LocalDate.now() + ")");
        int totalCopies = 0;
        int availableCopies = 0;
        for (Book b : data.getBooks()) {
            totalCopies += b.getTotalCopies();
            availableCopies += b.getAvailableCopies();
        }
        int activeMembers = 0;
        for (Member m : data.getMembers()) {
            if (m.isActive()) {
                activeMembers++;
            }
        }
        LocalDate today = LocalDate.now();
        int activeIssues = 0;
        int overdue = 0;
        double collected = 0;
        double outstanding = 0;
        for (IssueRecord r : data.getIssues()) {
            if (!r.isReturned()) {
                activeIssues++;
            }
            if (r.isOverdue(today)) {
                overdue++;
                Member m = data.findMember(r.getMemberId());
                outstanding += r.currentFine((m == null) ? 5.0 : m.getFinePerDay(), today);
            }
            if (r.isReturned() && r.getFine() > 0) {
                if (r.isFinePaid()) {
                    collected += r.getFine();
                } else {
                    outstanding += r.getFine();
                }
            }
        }
        System.out.println("Book titles            : " + data.getBooks().size());
        System.out.println("Total copies           : " + totalCopies);
        System.out.println("Copies on shelf        : " + availableCopies);
        System.out.println("Copies issued out      : " + (totalCopies - availableCopies));
        System.out.println("Registered members     : " + data.getMembers().size()
                + " (" + activeMembers + " active)");
        System.out.println("Total transactions     : " + data.getIssues().size());
        System.out.println("Currently issued       : " + activeIssues);
        System.out.println("Overdue books          : " + overdue);
        System.out.println("Fines collected        : " + Display.money(collected));
        System.out.println("Fines outstanding      : " + Display.money(outstanding));
    }

    public void overdueReport() {
        Display.title("OVERDUE REPORT");
        LocalDate today = LocalDate.now();
        int count = 0;
        double totalFine = 0;
        System.out.println(String.format("%-7s %-22s %-16s %-11s %-6s %10s",
                "IssueID", "Book", "Member", "Due", "Days", "Fine"));
        Display.line();
        for (IssueRecord r : data.getIssues()) {
            if (!r.isOverdue(today)) {
                continue;
            }
            Member m = data.findMember(r.getMemberId());
            double rate = (m == null) ? 5.0 : m.getFinePerDay();
            double fine = r.currentFine(rate, today);
            System.out.println(String.format("%-7s %-22s %-16s %-11s %-6d %10.2f",
                    r.getIssueId(), Display.truncate(data.bookTitle(r.getBookId()), 22),
                    Display.truncate(data.memberName(r.getMemberId()), 16), r.getDueDate(),
                    FineCalculator.overdueDays(r.getDueDate(), today), fine));
            totalFine += fine;
            count++;
        }
        Display.line();
        System.out.println("Overdue items: " + count + "    Accrued fines: " + Display.money(totalFine));
    }

    public void popularBooksReport() {
        Display.title("MOST POPULAR BOOKS (TOP 5)");
        Map<String, Integer> counts = new HashMap<>();
        for (IssueRecord r : data.getIssues()) {
            counts.merge(r.getBookId(), 1, Integer::sum);
        }
        if (counts.isEmpty()) {
            Display.info("No issue records yet.");
            return;
        }
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort((a, b) -> b.getValue() - a.getValue());
        System.out.println(String.format("%-4s %-6s %-34s %-8s", "Rank", "ID", "Title", "Issues"));
        Display.line();
        int rank = 1;
        for (Map.Entry<String, Integer> e : entries) {
            if (rank > 5) {
                break;
            }
            System.out.println(String.format("%-4d %-6s %-34s %-8d", rank, e.getKey(),
                    Display.truncate(data.bookTitle(e.getKey()), 34), e.getValue()));
            rank++;
        }
    }

    public void categoryReport() {
        Display.title("CATEGORY-WISE REPORT");
        Map<String, int[]> stats = new TreeMap<>();
        for (Book b : data.getBooks()) {
            int[] arr = stats.computeIfAbsent(b.getCategory().toLowerCase(), k -> new int[3]);
            arr[0]++;
            arr[1] += b.getTotalCopies();
            arr[2] += b.getAvailableCopies();
        }
        if (stats.isEmpty()) {
            Display.info("No books in the library.");
            return;
        }
        System.out.println(String.format("%-18s %-8s %-8s %-10s", "Category", "Titles", "Copies", "Available"));
        Display.line();
        for (Map.Entry<String, int[]> e : stats.entrySet()) {
            System.out.println(String.format("%-18s %-8d %-8d %-10d",
                    Display.truncate(e.getKey(), 18), e.getValue()[0], e.getValue()[1], e.getValue()[2]));
        }
    }

    public void memberActivityReport() {
        Display.title("MEMBER ACTIVITY REPORT");
        if (data.getMembers().isEmpty()) {
            Display.info("No members registered.");
            return;
        }
        System.out.println(String.format("%-6s %-22s %-9s %-8s %-8s %-8s",
                "ID", "Name", "Type", "Total", "Holding", "Status"));
        Display.line();
        for (Member m : data.getMembers()) {
            int total = 0;
            for (IssueRecord r : data.getIssues()) {
                if (r.getMemberId().equals(m.getId())) {
                    total++;
                }
            }
            System.out.println(String.format("%-6s %-22s %-9s %-8d %-8d %-8s",
                    m.getId(), Display.truncate(m.getName(), 22), m.getType(), total,
                    data.activeIssueCountForMember(m.getId()), m.isActive() ? "Active" : "Inactive"));
        }
    }

    public void fineReport() {
        Display.title("FINE COLLECTION REPORT");
        double collected = 0;
        double pending = 0;
        int paidCount = 0;
        int pendingCount = 0;
        System.out.println(String.format("%-7s %-16s %-22s %10s  %-8s",
                "IssueID", "Member", "Book", "Fine", "Status"));
        Display.line();
        for (IssueRecord r : data.getIssues()) {
            if (!r.isReturned() || r.getFine() <= 0) {
                continue;
            }
            String status = r.isFinePaid() ? "PAID" : "UNPAID";
            System.out.println(String.format("%-7s %-16s %-22s %10.2f  %-8s", r.getIssueId(),
                    Display.truncate(data.memberName(r.getMemberId()), 16),
                    Display.truncate(data.bookTitle(r.getBookId()), 22), r.getFine(), status));
            if (r.isFinePaid()) {
                collected += r.getFine();
                paidCount++;
            } else {
                pending += r.getFine();
                pendingCount++;
            }
        }
        Display.line();
        System.out.println("Collected : " + Display.money(collected) + " (" + paidCount + " records)");
        System.out.println("Pending   : " + Display.money(pending) + " (" + pendingCount + " records)");
    }

    public void dailyReport() {
        Display.title("DAILY CIRCULATION REPORT");
        LocalDate day = InputHelper.readDate("Date (yyyy-MM-dd) [Enter = today]: ");
        int issued = 0;
        int returned = 0;
        System.out.println("Activity on " + day + ":");
        Display.line();
        for (IssueRecord r : data.getIssues()) {
            if (r.getIssueDate().equals(day)) {
                System.out.println("  ISSUED   " + r.getIssueId() + "  "
                        + Display.truncate(data.bookTitle(r.getBookId()), 28)
                        + " -> " + data.memberName(r.getMemberId()));
                issued++;
            }
            if (r.isReturned() && r.getReturnDate().equals(day)) {
                System.out.println("  RETURNED " + r.getIssueId() + "  "
                        + Display.truncate(data.bookTitle(r.getBookId()), 28)
                        + " <- " + data.memberName(r.getMemberId()));
                returned++;
            }
        }
        Display.line();
        System.out.println("Books issued: " + issued + "    Books returned: " + returned);
    }
}
