import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    public static final String DATA_DIR = "data";
    public static final String BOOKS_FILE = DATA_DIR + File.separator + "books.txt";
    public static final String MEMBERS_FILE = DATA_DIR + File.separator + "members.txt";
    public static final String ISSUES_FILE = DATA_DIR + File.separator + "issues.txt";

    static {
        File dir = new File(DATA_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            Display.error("Could not create data directory. Data may not be saved.");
        }
    }

    public static List<String> readLines(String path) {
        List<String> lines = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            return lines;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            Display.error("Could not read " + path + ": " + e.getMessage());
        }
        return lines;
    }

    public static boolean writeLines(String path, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            Display.error("Could not write " + path + ": " + e.getMessage());
            return false;
        }
    }

    public static List<Book> loadBooks() {
        List<Book> list = new ArrayList<>();
        for (String line : readLines(BOOKS_FILE)) {
            try {
                list.add(Book.fromFileString(line));
            } catch (RuntimeException e) {
                Display.error("Skipped corrupt book record: " + line);
            }
        }
        return list;
    }

    public static List<Member> loadMembers() {
        List<Member> list = new ArrayList<>();
        for (String line : readLines(MEMBERS_FILE)) {
            try {
                list.add(Member.fromFileString(line));
            } catch (RuntimeException e) {
                Display.error("Skipped corrupt member record: " + line);
            }
        }
        return list;
    }

    public static List<IssueRecord> loadIssues() {
        List<IssueRecord> list = new ArrayList<>();
        for (String line : readLines(ISSUES_FILE)) {
            try {
                list.add(IssueRecord.fromFileString(line));
            } catch (RuntimeException e) {
                Display.error("Skipped corrupt issue record: " + line);
            }
        }
        return list;
    }

    public static void saveBooks(List<Book> books) {
        List<String> lines = new ArrayList<>();
        for (Book b : books) {
            lines.add(b.toFileString());
        }
        writeLines(BOOKS_FILE, lines);
    }

    public static void saveMembers(List<Member> members) {
        List<String> lines = new ArrayList<>();
        for (Member m : members) {
            lines.add(m.toFileString());
        }
        writeLines(MEMBERS_FILE, lines);
    }

    public static void saveIssues(List<IssueRecord> issues) {
        List<String> lines = new ArrayList<>();
        for (IssueRecord r : issues) {
            lines.add(r.toFileString());
        }
        writeLines(ISSUES_FILE, lines);
    }
}
