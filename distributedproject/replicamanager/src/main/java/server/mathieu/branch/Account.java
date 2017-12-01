package server.mathieu.branch;

public class Account {
    public static final int ACCOUNT_NUMBER_MAX_VALUE = 9999;
    public static final int ACCOUNT_NUMBER_MIN_VALUE = 1000;

    private int accountNumber;
    private double total;

    public Account(int accountNumber) {
        this(accountNumber, 0);
    }

    public Account(int accountNumber, double initialAmount) {
        setAccountNumber(accountNumber);
        this.total = initialAmount;
    }

    /**
     * Deposits the given amount into the account.
     * 
     * @param amount
     *            The amount to deposit
     * @return The new account total after the transaction.
     */
    synchronized double deposit(double amount) {
        if (amount <= 0) {
            throw new BranchException("Cannot deposit a negative amount or $0.");
        }
        total += amount;
        BranchImpl.logger
                .println("Deposit into " + accountNumber + " for an amount of " + amount + " new total is " + total);
        return total;
    }

    /**
     * Withdraws the given amount from the account.
     * 
     * @param amount
     *            The amount to withdraw
     * @return The new account total after the transaction.
     */
    synchronized double withdraw(double amount) {
        if (amount <= 0) {
            throw new BranchException("Cannot withdraw a negative amount or $0.");
        }
        if (total - amount < 0) {
            throw new BranchException(String
                    .format("You do not have enough funds to withdraw $%.2f. Your balance is $%.2f", amount, total));
        }
        total -= amount;
        BranchImpl.logger
                .println("Withdraw from " + accountNumber + " for an amount of " + amount + " new total is " + total);
        return total;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(int accountNumber) {
        validateAccountNumber(accountNumber);
        this.accountNumber = accountNumber;
    }

    public static void validateAccountNumber(int accountNumber) {
        if (accountNumber < ACCOUNT_NUMBER_MIN_VALUE || accountNumber > ACCOUNT_NUMBER_MAX_VALUE) {
            throw new IllegalArgumentException("account number needs to be between " + ACCOUNT_NUMBER_MIN_VALUE
                    + " and " + ACCOUNT_NUMBER_MAX_VALUE);
        }
    }

    public synchronized double getAccountTotal() {
        return total;
    }

}