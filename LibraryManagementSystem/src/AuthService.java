import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class AuthService {
    private static final String AUTH_FILE = FileManager.DATA_DIR + "/admin.txt";
    private static final int MAX_ATTEMPTS = 3;

    private String username;
    private String passwordHash;

    public AuthService() {
        load();
    }

    private void load() {
        List<String> lines = FileManager.readLines(AUTH_FILE);
        if (!lines.isEmpty()) {
            String[] parts = lines.get(0).split("\\|");
            if (parts.length == 2) {
                username = parts[0];
                passwordHash = parts[1];
                return;
            }
        }
        // First run (or corrupt file): create the default administrator account
        username = "admin";
        passwordHash = hash("admin123");
        save();
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        lines.add(username + "|" + passwordHash);
        FileManager.writeLines(AUTH_FILE, lines);
    }

    public static String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toString(text.hashCode());
        }
    }

    public boolean login() {
        Display.title("LIBRARIAN LOGIN");
        System.out.println("(Default first-time login -> username: admin, password: admin123)");
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String user = InputHelper.readLine("Username: ");
            String pass = InputHelper.readLine("Password: ");
            if (user.equals(username) && hash(pass).equals(passwordHash)) {
                Display.success("Login successful. Welcome, " + username + "!");
                return true;
            }
            Display.error("Wrong username or password. Attempts left: " + (MAX_ATTEMPTS - attempt));
        }
        return false;
    }

    public void changePassword() {
        Display.title("CHANGE PASSWORD");
        String current = InputHelper.readLine("Current password: ");
        if (!hash(current).equals(passwordHash)) {
            Display.error("Current password is incorrect.");
            return;
        }
        String newPass = InputHelper.readLine("New password (min 6 characters): ");
        if (newPass.length() < 6) {
            Display.error("Password is too short.");
            return;
        }
        String confirm = InputHelper.readLine("Confirm new password: ");
        if (!newPass.equals(confirm)) {
            Display.error("Passwords do not match.");
            return;
        }
        passwordHash = hash(newPass);
        save();
        Display.success("Password changed successfully.");
    }
}
