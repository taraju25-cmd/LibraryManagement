import java.time.LocalDate;

public class Member extends Person {
    public enum Type { STUDENT, FACULTY, GENERAL }

    private Type type;
    private final LocalDate joinDate;
    private boolean active;

    public Member(String id, String name, String phone, String email,
                  Type type, LocalDate joinDate, boolean active) {
        super(id, name, phone, email);
        this.type = type;
        this.joinDate = joinDate;
        this.active = active;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Borrowing rules depend on the membership type (polymorphic behaviour)
    public int getMaxBooks() {
        switch (type) {
            case FACULTY:
                return 10;
            case STUDENT:
                return 5;
            default:
                return 3;
        }
    }

    public int getLoanDays() {
        switch (type) {
            case FACULTY:
                return 30;
            case STUDENT:
                return 14;
            default:
                return 7;
        }
    }

    public double getFinePerDay() {
        switch (type) {
            case FACULTY:
                return 1.0;
            case STUDENT:
                return 2.0;
            default:
                return 5.0;
        }
    }

    @Override
    public String getRole() {
        return "Library Member - " + type;
    }

    public String toFileString() {
        return id + "|" + name + "|" + phone + "|" + email + "|" + type + "|" + joinDate + "|" + active;
    }

    public static Member fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length != 7) {
            throw new IllegalArgumentException("Invalid member record: " + line);
        }
        return new Member(p[0], p[1], p[2], p[3], Type.valueOf(p[4]),
                LocalDate.parse(p[5]), Boolean.parseBoolean(p[6]));
    }

    public static void printHeader() {
        System.out.println(String.format("%-6s %-22s %-14s %-26s %-9s %-8s",
                "ID", "Name", "Phone", "Email", "Type", "Status"));
        Display.line();
    }

    public void printRow() {
        System.out.println(String.format("%-6s %-22s %-14s %-26s %-9s %-8s",
                id, Display.truncate(name, 22), phone, Display.truncate(email, 26),
                type, active ? "Active" : "Inactive"));
    }
}
