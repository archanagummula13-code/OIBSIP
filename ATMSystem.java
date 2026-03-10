import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ═══════════════════════════════════════════════════════════════
//                  ATM INTERFACE — JAVA MINI PROJECT
//                     (Single File Version)
// ═══════════════════════════════════════════════════════════════
//
//  HOW TO COMPILE & RUN:
//    javac ATMSystem.java
//    java ATMSystem
//
//  DEMO CREDENTIALS:
//    User ID: user001  PIN: 1234  (Balance: ₹50,000)
//    User ID: user002  PIN: 5678  (Balance: ₹30,000)
//    User ID: user003  PIN: 9999  (Balance: ₹10,000)
//
// ═══════════════════════════════════════════════════════════════


// ───────────────────────────────────────────────────────────────
//  CLASS 1 : Transaction
//  Stores a single transaction record (deposit/withdraw/transfer)
// ───────────────────────────────────────────────────────────────
class Transaction {

    // Transaction type constants
    public static final String DEPOSIT  = "DEPOSIT   ";
    public static final String WITHDRAW = "WITHDRAWAL";
    public static final String TRANSFER = "TRANSFER  ";

    private String type;           // Type of transaction
    private double amount;         // Amount involved
    private double balanceAfter;   // Balance after this transaction
    private String description;    // Extra info (e.g. recipient)
    private String timestamp;      // Date & time of transaction

    /** Creates a new transaction and auto-stamps the current time. */
    public Transaction(String type, double amount,
                       double balanceAfter, String description) {
        this.type         = type;
        this.amount       = amount;
        this.balanceAfter = balanceAfter;
        this.description  = description;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
        this.timestamp = LocalDateTime.now().format(fmt);
    }

    // Getters
    public String getType()         { return type;         }
    public double getAmount()       { return amount;       }
    public double getBalanceAfter() { return balanceAfter; }
    public String getDescription()  { return description;  }
    public String getTimestamp()    { return timestamp;    }

    /** Nicely formatted single-line summary of the transaction. */
    @Override
    public String toString() {
        return String.format(
            "[%s]  %s  Amt: %9.2f  |  Bal: %10.2f  |  %s",
            timestamp, type, amount, balanceAfter, description
        );
    }
}


// ───────────────────────────────────────────────────────────────
//  CLASS 2 : UserAccount
//  Holds account details, balance, and transaction history.
// ───────────────────────────────────────────────────────────────
class UserAccount {

    private String userId;
    private String pin;
    private String accountHolderName;
    private double balance;
    private List<Transaction> transactionHistory;

    /** Creates a new account with a starting balance. */
    public UserAccount(String userId, String pin,
                       String accountHolderName, double initialBalance) {
        this.userId             = userId;
        this.pin                = pin;
        this.accountHolderName  = accountHolderName;
        this.balance            = initialBalance;
        this.transactionHistory = new ArrayList<>();
    }

    // ── Getters ──────────────────────────────────────────────
    public String getUserId()            { return userId;            }
    public String getAccountHolderName() { return accountHolderName; }
    public double getBalance()           { return balance;           }
    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    // ── Balance helpers ───────────────────────────────────────

    /** Adds money to the account. */
    public void credit(double amount) {
        balance += amount;
    }

    /** Removes money from the account. */
    public void debit(double amount) {
        balance -= amount;
    }

    /** Appends a transaction to this account's history. */
    public void addTransaction(Transaction tx) {
        transactionHistory.add(tx);
    }

    /** Returns true if the supplied PIN matches the stored PIN. */
    public boolean validatePin(String inputPin) {
        return this.pin.equals(inputPin);
    }
}


// ───────────────────────────────────────────────────────────────
//  CLASS 3 : BankingOperations
//  Core logic: deposit, withdraw, transfer.
//  Also acts as the in-memory database of all accounts.
// ───────────────────────────────────────────────────────────────
class BankingOperations {

    // UserID → UserAccount  (acts like a small database)
    private Map<String, UserAccount> accountDatabase;

    /** Sets up the bank and loads three demo accounts. */
    public BankingOperations() {
        accountDatabase = new HashMap<>();
        loadDemoAccounts();
    }

    // ── Demo accounts ─────────────────────────────────────────
    private void loadDemoAccounts() {
        accountDatabase.put("user001",
            new UserAccount("user001", "1234", "Arjun Sharma",  50000.00));
        accountDatabase.put("user002",
            new UserAccount("user002", "5678", "Priya Mehta",   30000.00));
        accountDatabase.put("user003",
            new UserAccount("user003", "9999", "Rohan Verma",   10000.00));
    }

    /** Returns the UserAccount for the given ID, or null if not found. */
    public UserAccount getAccount(String userId) {
        return accountDatabase.get(userId);
    }

    // ── Deposit ───────────────────────────────────────────────
    /**
     * Credits money to the account and logs the transaction.
     * @return result message string
     */
    public String deposit(UserAccount account, double amount) {

        if (amount <= 0)
            return "ERROR: Deposit amount must be greater than zero.";
        if (amount > 1_000_000)
            return "ERROR: Maximum single deposit limit is ₹1,000,000.";

        account.credit(amount);

        account.addTransaction(new Transaction(
            Transaction.DEPOSIT, amount, account.getBalance(), "Cash deposit"
        ));

        return String.format(
            "SUCCESS: ₹%.2f deposited. New balance: ₹%.2f",
            amount, account.getBalance()
        );
    }

    // ── Withdraw ──────────────────────────────────────────────
    /**
     * Debits money from the account and logs the transaction.
     * @return result message string
     */
    public String withdraw(UserAccount account, double amount) {

        if (amount <= 0)
            return "ERROR: Withdrawal amount must be greater than zero.";
        if (amount > 50_000)
            return "ERROR: Maximum single withdrawal limit is ₹50,000.";
        if (amount > account.getBalance())
            return String.format(
                "ERROR: Insufficient balance. Available: ₹%.2f",
                account.getBalance()
            );

        account.debit(amount);

        account.addTransaction(new Transaction(
            Transaction.WITHDRAW, amount, account.getBalance(), "Cash withdrawal"
        ));

        return String.format(
            "SUCCESS: ₹%.2f withdrawn. Remaining balance: ₹%.2f",
            amount, account.getBalance()
        );
    }

    // ── Transfer ──────────────────────────────────────────────
    /**
     * Moves money from one account to another, logging both sides.
     * @return result message string
     */
    public String transfer(UserAccount sender, String recipientId, double amount) {

        if (sender.getUserId().equalsIgnoreCase(recipientId))
            return "ERROR: Cannot transfer to your own account.";
        if (amount <= 0)
            return "ERROR: Transfer amount must be greater than zero.";
        if (amount > sender.getBalance())
            return String.format(
                "ERROR: Insufficient balance. Available: ₹%.2f",
                sender.getBalance()
            );

        UserAccount recipient = getAccount(recipientId);
        if (recipient == null)
            return "ERROR: Recipient account '" + recipientId + "' not found.";

        // Perform the transfer
        sender.debit(amount);
        recipient.credit(amount);

        // Log for sender
        sender.addTransaction(new Transaction(
            Transaction.TRANSFER, amount, sender.getBalance(),
            "Sent TO → " + recipientId + " (" + recipient.getAccountHolderName() + ")"
        ));

        // Log for recipient
        recipient.addTransaction(new Transaction(
            Transaction.TRANSFER, amount, recipient.getBalance(),
            "Received FROM ← " + sender.getUserId() + " (" + sender.getAccountHolderName() + ")"
        ));

        return String.format(
            "SUCCESS: ₹%.2f transferred to %s (%s). Your balance: ₹%.2f",
            amount, recipientId, recipient.getAccountHolderName(), sender.getBalance()
        );
    }
}


// ───────────────────────────────────────────────────────────────
//  CLASS 4 : ATMInterface
//  All user-facing menus, prompts, and display formatting.
// ───────────────────────────────────────────────────────────────
class ATMInterface {

    private BankingOperations bank;
    private Scanner scanner;
    private UserAccount currentUser;

    private static final int MAX_LOGIN_ATTEMPTS = 3;

    public ATMInterface(BankingOperations bank) {
        this.bank    = bank;
        this.scanner = new Scanner(System.in);
    }

    // ── Entry point ───────────────────────────────────────────
    /** Starts the full ATM session (banner → login → menu → goodbye). */
    public void start() {
        printBanner();

        if (!authenticate()) {
            System.out.println("\n  ✗  Too many failed attempts. Card blocked.");
            System.out.println("     Please contact your bank. Goodbye!\n");
            scanner.close();
            return;
        }

        showMainMenu();

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║  Thank you for using the ATM. Goodbye! 👋    ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        scanner.close();
    }

    // ── Login ─────────────────────────────────────────────────
    /** Gives the user up to 3 chances to log in. */
    private boolean authenticate() {

        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {

            System.out.println("\n  ── LOGIN  (Attempt " + attempt
                               + " of " + MAX_LOGIN_ATTEMPTS + ") ──");
            System.out.print("  Enter User ID : ");
            String userId = scanner.nextLine().trim();

            System.out.print("  Enter PIN     : ");
            String pin = scanner.nextLine().trim();

            UserAccount account = bank.getAccount(userId);

            if (account != null && account.validatePin(pin)) {
                currentUser = account;
                System.out.println("\n  ✔  Welcome, "
                                   + currentUser.getAccountHolderName() + "!");
                return true;
            }
            System.out.println("  ✗  Invalid User ID or PIN. Please try again.");
        }
        return false;
    }

    // ── Main menu loop ────────────────────────────────────────
    /** Shows the ATM menu and handles choices until the user quits. */
    private void showMainMenu() {

        boolean running = true;

        while (running) {
            printMenuOptions();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": handleViewHistory(); break;
                case "2": handleWithdraw();    break;
                case "3": handleDeposit();     break;
                case "4": handleTransfer();    break;
                case "5": running = false;     break;
                default:
                    System.out.println("\n  ⚠  Invalid option. Enter 1–5.\n");
            }
        }
    }

    // ── Option 1 : Transaction History ────────────────────────
    private void handleViewHistory() {
        printSectionHeader("TRANSACTION HISTORY");

        List<Transaction> history = currentUser.getTransactionHistory();

        if (history.isEmpty()) {
            System.out.println("  No transactions found for this account.");
        } else {
            System.out.println("  Total transactions: " + history.size() + "\n");
            for (int i = 0; i < history.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + history.get(i));
            }
        }
        System.out.println();
        pause();
    }

    // ── Option 2 : Withdraw ───────────────────────────────────
    private void handleWithdraw() {
        printSectionHeader("WITHDRAW MONEY");
        System.out.printf("  Current Balance  : ₹%.2f%n", currentUser.getBalance());
        System.out.println("  Limit per txn    : ₹50,000\n");

        double amount = readAmount("  Enter withdrawal amount: ₹");
        if (amount < 0) return;

        printResult(bank.withdraw(currentUser, amount));
        pause();
    }

    // ── Option 3 : Deposit ────────────────────────────────────
    private void handleDeposit() {
        printSectionHeader("DEPOSIT MONEY");
        System.out.println("  Limit per txn    : ₹10,00,000\n");

        double amount = readAmount("  Enter deposit amount: ₹");
        if (amount < 0) return;

        printResult(bank.deposit(currentUser, amount));
        pause();
    }

    // ── Option 4 : Transfer ───────────────────────────────────
    private void handleTransfer() {
        printSectionHeader("TRANSFER MONEY");
        System.out.printf("  Current Balance  : ₹%.2f%n%n", currentUser.getBalance());

        System.out.print("  Enter Recipient User ID : ");
        String recipientId = scanner.nextLine().trim();

        if (recipientId.isEmpty()) {
            System.out.println("  ⚠  Recipient ID cannot be empty.");
            pause();
            return;
        }

        double amount = readAmount("  Enter transfer amount  : ₹");
        if (amount < 0) return;

        System.out.printf("%n  Confirm ₹%.2f to '%s'? (yes/no): ", amount, recipientId);
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("yes") && !confirm.equals("y")) {
            System.out.println("\n  Transfer cancelled.");
            pause();
            return;
        }

        printResult(bank.transfer(currentUser, recipientId, amount));
        pause();
    }

    // ── Input helper ──────────────────────────────────────────
    /**
     * Reads a positive amount. Returns -1 if user types "cancel".
     */
    private double readAmount(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("cancel")) {
                System.out.println("  Operation cancelled.");
                return -1;
            }
            try {
                double value = Double.parseDouble(input);
                if (value <= 0)
                    System.out.println("  ⚠  Amount must be > 0. (type 'cancel' to abort)");
                else
                    return value;
            } catch (NumberFormatException e) {
                System.out.println("  ⚠  Invalid input. Enter a number. (type 'cancel' to abort)");
            }
        }
    }

    // ── Display helpers ───────────────────────────────────────
    private void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║           WELCOME TO JAVA ATM SYSTEM  🏧         ║");
        System.out.println("║        Secure  •  Fast  •  Reliable              ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║  Demo Accounts:                                  ║");
        System.out.println("║   ID: user001  PIN: 1234  (Balance: ₹50,000)    ║");
        System.out.println("║   ID: user002  PIN: 5678  (Balance: ₹30,000)    ║");
        System.out.println("║   ID: user003  PIN: 9999  (Balance: ₹10,000)    ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    private void printMenuOptions() {
        System.out.println();
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.printf( "║  Account : %-27s║%n", currentUser.getAccountHolderName());
        System.out.printf( "║  Balance : ₹%-26.2f║%n", currentUser.getBalance());
        System.out.println("╠═══════════════════════════════════════╣");
        System.out.println("║   1.  View Transaction History        ║");
        System.out.println("║   2.  Withdraw Money                  ║");
        System.out.println("║   3.  Deposit Money                   ║");
        System.out.println("║   4.  Transfer Money                  ║");
        System.out.println("║   5.  Quit                            ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.print("  Select an option (1-5): ");
    }

    private void printSectionHeader(String title) {
        System.out.println();
        System.out.println("  ┌──────────────────────────────────────────┐");
        System.out.printf( "  │   %-41s│%n", title);
        System.out.println("  └──────────────────────────────────────────┘");
    }

    private void printResult(String result) {
        System.out.println();
        System.out.println(result.startsWith("SUCCESS")
            ? "  ✔  " + result
            : "  ✗  " + result);
    }

    private void pause() {
        System.out.print("\n  Press Enter to return to menu...");
        scanner.nextLine();
    }
}


// ───────────────────────────────────────────────────────────────
//  CLASS 5 : ATMSystem  (Main – program entry point)
// ───────────────────────────────────────────────────────────────
public class ATMSystem {

    public static void main(String[] args) {

        // 1. Create the banking engine (loads 3 demo accounts)
        BankingOperations bank = new BankingOperations();

        // 2. Create the ATM interface and link it to the bank
        ATMInterface atm = new ATMInterface(bank);

        // 3. Launch the ATM session
        atm.start();
    }
}
