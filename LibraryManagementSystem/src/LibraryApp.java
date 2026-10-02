public class LibraryApp {
    private final LibraryData data;
    private final AuthService auth;
    private final BookManager bookManager;
    private final MemberManager memberManager;
    private final IssueManager issueManager;
    private final ReportManager reportManager;

    public LibraryApp() {
        data = new LibraryData();
        auth = new AuthService();
        bookManager = new BookManager(data);
        memberManager = new MemberManager(data);
        issueManager = new IssueManager(data);
        reportManager = new ReportManager(data);
    }

    public static void main(String[] args) {
        System.out.println("========================================================");
        System.out.println("       LIBRARY MANAGEMENT SYSTEM  (Console MIS)");
        System.out.println("========================================================");
        LibraryApp app = new LibraryApp();
        if (!app.auth.login()) {
            Display.error("Too many failed attempts. Program closed.");
            return;
        }
        app.run();
    }

    private void run() {
        boolean running = true;
        while (running) {
            Display.title("MAIN MENU");
            System.out.println("1. Book Management");
            System.out.println("2. Member Management");
            System.out.println("3. Circulation (Issue / Return / Fines)");
            System.out.println("4. Reports");
            System.out.println("5. Settings");
            System.out.println("0. Save and Exit");
            int choice = InputHelper.readChoice(0, 5);
            switch (choice) {
                case 1:
                    bookMenu();
                    break;
                case 2:
                    memberMenu();
                    break;
                case 3:
                    circulationMenu();
                    break;
                case 4:
                    reportMenu();
                    break;
                case 5:
                    settingsMenu();
                    break;
                default:
                    data.save();
                    Display.success("All data saved. Goodbye!");
                    running = false;
                    break;
            }
        }
    }

    private void bookMenu() {
        boolean back = false;
        while (!back) {
            Display.title("BOOK MANAGEMENT");
            System.out.println("1. Add new book");
            System.out.println("2. View all books");
            System.out.println("3. View book details");
            System.out.println("4. Update book");
            System.out.println("5. Delete book");
            System.out.println("6. Search / filter books");
            System.out.println("7. Sort books");
            System.out.println("8. Add copies to a book");
            System.out.println("0. Back to main menu");
            int choice = InputHelper.readChoice(0, 8);
            switch (choice) {
                case 1:
                    bookManager.addBook();
                    break;
                case 2:
                    bookManager.displayAllBooks();
                    break;
                case 3:
                    bookManager.viewBookDetails();
                    break;
                case 4:
                    bookManager.updateBook();
                    break;
                case 5:
                    bookManager.deleteBook();
                    break;
                case 6:
                    bookManager.searchBooks();
                    break;
                case 7:
                    bookManager.sortBooks();
                    break;
                case 8:
                    bookManager.addCopies();
                    break;
                default:
                    back = true;
                    continue;
            }
            Display.pause();
        }
    }

    private void memberMenu() {
        boolean back = false;
        while (!back) {
            Display.title("MEMBER MANAGEMENT");
            System.out.println("1. Register new member");
            System.out.println("2. View all members");
            System.out.println("3. View member details");
            System.out.println("4. Update member");
            System.out.println("5. Activate / deactivate member");
            System.out.println("6. Delete member");
            System.out.println("7. Search members");
            System.out.println("0. Back to main menu");
            int choice = InputHelper.readChoice(0, 7);
            switch (choice) {
                case 1:
                    memberManager.addMember();
                    break;
                case 2:
                    memberManager.displayAllMembers();
                    break;
                case 3:
                    memberManager.viewMemberDetails();
                    break;
                case 4:
                    memberManager.updateMember();
                    break;
                case 5:
                    memberManager.toggleStatus();
                    break;
                case 6:
                    memberManager.deleteMember();
                    break;
                case 7:
                    memberManager.searchMembers();
                    break;
                default:
                    back = true;
                    continue;
            }
            Display.pause();
        }
    }

    private void circulationMenu() {
        boolean back = false;
        while (!back) {
            Display.title("CIRCULATION");
            System.out.println("1. Issue a book");
            System.out.println("2. Return a book");
            System.out.println("3. Renew a book");
            System.out.println("4. Pay fines");
            System.out.println("5. View issue / return records");
            System.out.println("6. Member borrowing history");
            System.out.println("7. Book issue history");
            System.out.println("8. Overdue books");
            System.out.println("0. Back to main menu");
            int choice = InputHelper.readChoice(0, 8);
            switch (choice) {
                case 1:
                    issueManager.issueBook();
                    break;
                case 2:
                    issueManager.returnBook();
                    break;
                case 3:
                    issueManager.renewBook();
                    break;
                case 4:
                    issueManager.payFine();
                    break;
                case 5:
                    issueManager.viewAllIssues();
                    break;
                case 6:
                    issueManager.viewMemberHistory();
                    break;
                case 7:
                    issueManager.viewBookHistory();
                    break;
                case 8:
                    issueManager.viewOverdue();
                    break;
                default:
                    back = true;
                    continue;
            }
            Display.pause();
        }
    }

    private void reportMenu() {
        boolean back = false;
        while (!back) {
            Display.title("REPORTS");
            System.out.println("1. Library summary");
            System.out.println("2. Overdue report");
            System.out.println("3. Most popular books");
            System.out.println("4. Category-wise report");
            System.out.println("5. Member activity report");
            System.out.println("6. Fine collection report");
            System.out.println("7. Daily circulation report");
            System.out.println("0. Back to main menu");
            int choice = InputHelper.readChoice(0, 7);
            switch (choice) {
                case 1:
                    reportManager.summaryReport();
                    break;
                case 2:
                    reportManager.overdueReport();
                    break;
                case 3:
                    reportManager.popularBooksReport();
                    break;
                case 4:
                    reportManager.categoryReport();
                    break;
                case 5:
                    reportManager.memberActivityReport();
                    break;
                case 6:
                    reportManager.fineReport();
                    break;
                case 7:
                    reportManager.dailyReport();
                    break;
                default:
                    back = true;
                    continue;
            }
            Display.pause();
        }
    }

    private void settingsMenu() {
        Display.title("SETTINGS");
        System.out.println("1. Change administrator password");
        System.out.println("2. Load sample data (only if library is empty)");
        System.out.println("0. Back to main menu");
        int choice = InputHelper.readChoice(0, 2);
        if (choice == 1) {
            auth.changePassword();
            Display.pause();
        } else if (choice == 2) {
            DataSeeder.seed(data);
            Display.pause();
        }
    }
}
