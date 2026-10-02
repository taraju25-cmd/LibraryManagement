import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class InputHelper {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-]{7,15}$");

    public static String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.out.println("Input stream closed. Exiting program.");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    // The pipe character is used as the file separator, so it is replaced in user input
    private static String sanitize(String s) {
        return s.replace('|', '/');
    }

    public static String readText(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (s.isEmpty()) {
                Display.error("This field cannot be empty.");
                continue;
            }
            return sanitize(s);
        }
    }

    public static String readOptional(String prompt, String current) {
        String s = readLine(prompt + " [" + current + "]: ");
        if (s.isEmpty()) {
            return current;
        }
        return sanitize(s);
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            String s = readLine(prompt);
            try {
                int value = Integer.parseInt(s);
                if (value < min || value > max) {
                    Display.error("Please enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                Display.error("Invalid number. Please try again.");
            }
        }
    }

    public static int readChoice(int min, int max) {
        return readInt("Enter your choice: ", min, max);
    }

    public static String readPhone(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (PHONE_PATTERN.matcher(s).matches()) {
                return s;
            }
            Display.error("Phone must contain 7-15 digits (digits, + and - allowed).");
        }
    }

    public static String readEmail(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (EMAIL_PATTERN.matcher(s).matches()) {
                return s.toLowerCase();
            }
            Display.error("Invalid email format (example: name@example.com).");
        }
    }

    public static LocalDate readDate(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (s.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException e) {
                Display.error("Invalid date. Use the format yyyy-MM-dd (e.g. 2026-10-02).");
            }
        }
    }

    public static boolean readYesNo(String prompt) {
        while (true) {
            String s = readLine(prompt).toLowerCase();
            if (s.equals("y") || s.equals("yes")) {
                return true;
            }
            if (s.equals("n") || s.equals("no")) {
                return false;
            }
            Display.error("Please answer y or n.");
        }
    }
}
