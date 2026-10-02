import java.time.LocalDate;

public class IssueRecord {
    private final String issueId;
    private final String bookId;
    private final String memberId;
    private final LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double fine;
    private boolean finePaid;
    private int renewCount;

    public IssueRecord(String issueId, String bookId, String memberId, LocalDate issueDate,
                       LocalDate dueDate, LocalDate returnDate, double fine,
                       boolean finePaid, int renewCount) {
        this.issueId = issueId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fine = fine;
        this.finePaid = finePaid;
        this.renewCount = renewCount;
    }

    public String getIssueId() {
        return issueId;
    }

    public String getBookId() {
        return bookId;
    }

    public String getMemberId() {
        return memberId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public double getFine() {
        return fine;
    }

    public boolean isFinePaid() {
        return finePaid;
    }

    public int getRenewCount() {
        return renewCount;
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    public boolean isOverdue(LocalDate today) {
        return !isReturned() && today.isAfter(dueDate);
    }

    public boolean hasUnpaidFine() {
        return isReturned() && fine > 0 && !finePaid;
    }

    public double currentFine(double ratePerDay, LocalDate today) {
        if (isReturned()) {
            return fine;
        }
        return FineCalculator.calculate(ratePerDay, dueDate, today);
    }

    public void markReturned(LocalDate date, double fineAmount) {
        this.returnDate = date;
        this.fine = fineAmount;
        this.finePaid = (fineAmount == 0);
    }

    public void markFinePaid() {
        this.finePaid = true;
    }

    public void extendDueDate(int days) {
        this.dueDate = dueDate.plusDays(days);
        this.renewCount++;
    }

    public String toFileString() {
        String ret = (returnDate == null) ? "-" : returnDate.toString();
        return issueId + "|" + bookId + "|" + memberId + "|" + issueDate + "|" + dueDate + "|"
                + ret + "|" + fine + "|" + finePaid + "|" + renewCount;
    }

    public static IssueRecord fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length != 9) {
            throw new IllegalArgumentException("Invalid issue record: " + line);
        }
        LocalDate ret = p[5].equals("-") ? null : LocalDate.parse(p[5]);
        return new IssueRecord(p[0], p[1], p[2], LocalDate.parse(p[3]), LocalDate.parse(p[4]),
                ret, Double.parseDouble(p[6]), Boolean.parseBoolean(p[7]), Integer.parseInt(p[8]));
    }
}
