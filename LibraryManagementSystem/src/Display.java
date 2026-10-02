public class Display {
    public static final String CURRENCY = "Rs. ";
    private static final int WIDTH = 92;

    public static void line() {
        System.out.println("-".repeat(WIDTH));
    }

    public static void doubleLine() {
        System.out.println("=".repeat(WIDTH));
    }

    public static void title(String text) {
        System.out.println();
        doubleLine();
        System.out.println("  " + text);
        doubleLine();
    }

    public static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max - 2) + "..";
    }

    public static String money(double amount) {
        return String.format("%s%.2f", CURRENCY, amount);
    }

    public static void success(String message) {
        System.out.println("[OK] " + message);
    }

    public static void error(String message) {
        System.out.println("[ERROR] " + message);
    }

    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void pause() {
        InputHelper.readLine("\nPress Enter to continue...");
    }
}
