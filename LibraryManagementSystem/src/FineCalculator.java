import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FineCalculator {
    public static final double MAX_FINE = 500.0;

    public static long overdueDays(LocalDate dueDate, LocalDate actualDate) {
        long days = ChronoUnit.DAYS.between(dueDate, actualDate);
        return Math.max(days, 0);
    }

    public static double calculate(double ratePerDay, LocalDate dueDate, LocalDate actualDate) {
        double fine = overdueDays(dueDate, actualDate) * ratePerDay;
        return Math.min(fine, MAX_FINE);
    }
}
