import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MemberManager {
    private final LibraryData data;

    public MemberManager(LibraryData data) {
        this.data = data;
    }

    public void addMember() {
        Display.title("REGISTER NEW MEMBER");
        String name = InputHelper.readText("Full name: ");
        String phone = InputHelper.readPhone("Phone: ");
        String email;
        while (true) {
            email = InputHelper.readEmail("Email: ");
            if (data.findMemberByEmail(email) == null) {
                break;
            }
            Display.error("This email is already registered. Use a different email.");
        }
        Member.Type type = chooseType();
        Member member = new Member(data.nextMemberId(), name, phone, email, type, LocalDate.now(), true);
        data.getMembers().add(member);
        data.save();
        Display.success("Member registered with ID " + member.getId());
        System.out.println("Borrowing limit: " + member.getMaxBooks() + " books, "
                + member.getLoanDays() + " days, fine " + Display.money(member.getFinePerDay()) + "/day");
    }

    private Member.Type chooseType() {
        System.out.println("Membership type:");
        System.out.println("  1. Student  (5 books, 14 days, Rs. 2/day fine)");
        System.out.println("  2. Faculty  (10 books, 30 days, Rs. 1/day fine)");
        System.out.println("  3. General  (3 books, 7 days, Rs. 5/day fine)");
        int choice = InputHelper.readChoice(1, 3);
        if (choice == 1) {
            return Member.Type.STUDENT;
        }
        if (choice == 2) {
            return Member.Type.FACULTY;
        }
        return Member.Type.GENERAL;
    }

    public void displayAllMembers() {
        Display.title("ALL MEMBERS");
        printMemberTable(data.getMembers());
    }

    private void printMemberTable(List<Member> list) {
        if (list.isEmpty()) {
            Display.info("No members found.");
            return;
        }
        Member.printHeader();
        for (Member m : list) {
            m.printRow();
        }
        Display.line();
        System.out.println("Total records: " + list.size());
    }

    public void viewMemberDetails() {
        Display.title("MEMBER DETAILS");
        Member m = data.findMember(InputHelper.readText("Enter Member ID: "));
        if (m == null) {
            Display.error("Member not found.");
            return;
        }
        System.out.println("Member ID    : " + m.getId());
        System.out.println("Name         : " + m.getName());
        System.out.println("Phone        : " + m.getPhone());
        System.out.println("Email        : " + m.getEmail());
        System.out.println("Type         : " + m.getType());
        System.out.println("Joined       : " + m.getJoinDate());
        System.out.println("Status       : " + (m.isActive() ? "Active" : "Inactive"));
        System.out.println("Books Held   : " + data.activeIssueCountForMember(m.getId())
                + " of " + m.getMaxBooks());
        System.out.println("Unpaid Fines : " + Display.money(unpaidFines(m)));
        Display.line();
        System.out.println("Books currently borrowed:");
        boolean any = false;
        for (IssueRecord r : data.getIssues()) {
            if (r.getMemberId().equals(m.getId()) && !r.isReturned()) {
                String flag = r.isOverdue(LocalDate.now()) ? "  ** OVERDUE **" : "";
                System.out.println("  " + r.getIssueId() + "  " + data.bookTitle(r.getBookId())
                        + " (due " + r.getDueDate() + ")" + flag);
                any = true;
            }
        }
        if (!any) {
            System.out.println("  None.");
        }
    }

    private double unpaidFines(Member m) {
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

    public void updateMember() {
        Display.title("UPDATE MEMBER");
        Member m = data.findMember(InputHelper.readText("Enter Member ID to update: "));
        if (m == null) {
            Display.error("Member not found.");
            return;
        }
        System.out.println("Press Enter to keep the current value.");
        m.setName(InputHelper.readOptional("Name", m.getName()));
        String phone = InputHelper.readLine("Phone [" + m.getPhone() + "]: ");
        if (!phone.isEmpty()) {
            if (phone.matches("^[0-9+\\-]{7,15}$")) {
                m.setPhone(phone);
            } else {
                Display.error("Invalid phone. Old phone kept.");
            }
        }
        String email = InputHelper.readLine("Email [" + m.getEmail() + "]: ");
        if (!email.isEmpty()) {
            Member other = data.findMemberByEmail(email);
            if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                Display.error("Invalid email. Old email kept.");
            } else if (other != null && other != m) {
                Display.error("Email already used by another member. Old email kept.");
            } else {
                m.setEmail(email.toLowerCase());
            }
        }
        if (InputHelper.readYesNo("Change membership type? (y/n): ")) {
            m.setType(chooseType());
        }
        data.save();
        Display.success("Member updated successfully.");
    }

    public void toggleStatus() {
        Display.title("ACTIVATE / DEACTIVATE MEMBER");
        Member m = data.findMember(InputHelper.readText("Enter Member ID: "));
        if (m == null) {
            Display.error("Member not found.");
            return;
        }
        if (m.isActive() && data.activeIssueCountForMember(m.getId()) > 0) {
            Display.error("Member still holds books. Return them before deactivating.");
            return;
        }
        m.setActive(!m.isActive());
        data.save();
        Display.success("Member " + m.getId() + " is now " + (m.isActive() ? "ACTIVE." : "INACTIVE."));
    }

    public void deleteMember() {
        Display.title("DELETE MEMBER");
        Member m = data.findMember(InputHelper.readText("Enter Member ID to delete: "));
        if (m == null) {
            Display.error("Member not found.");
            return;
        }
        if (data.hasHistoryForMember(m.getId())) {
            Display.error("Member has borrowing history and cannot be deleted. Deactivate instead.");
            return;
        }
        if (InputHelper.readYesNo("Delete member '" + m.getName() + "'? (y/n): ")) {
            data.getMembers().remove(m);
            data.save();
            Display.success("Member deleted.");
        } else {
            Display.info("Deletion cancelled.");
        }
    }

    public void searchMembers() {
        Display.title("SEARCH MEMBERS");
        String keyword = InputHelper.readText("Enter name, ID, phone or email: ").toLowerCase();
        List<Member> results = new ArrayList<>();
        for (Member m : data.getMembers()) {
            if (m.getName().toLowerCase().contains(keyword)
                    || m.getId().toLowerCase().equals(keyword)
                    || m.getPhone().contains(keyword)
                    || m.getEmail().toLowerCase().contains(keyword)) {
                results.add(m);
            }
        }
        System.out.println();
        printMemberTable(results);
    }
}
