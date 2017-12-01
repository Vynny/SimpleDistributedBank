package server.mathieu.branch;

public class CustomerRecord {
    private CustomerDetails details;
    private Account account;

    public CustomerRecord(String firstName, String lastName, String address, String phone, int accountNumber) {
        this(firstName, lastName, address, phone, accountNumber, 0);
    }

    public CustomerRecord(String firstName, String lastName, String address, String phone, int accountNumber,
            double amount) {
        details = new CustomerDetails(firstName, lastName, address, phone);
        account = new Account(accountNumber, amount);
    }

    /**
     * Deposits the given amount into the account.
     * 
     * @param amount
     *            The amount to deposit
     * @return The new account total after the transaction.
     */
    public double deposit(double amount) {
        return account.deposit(amount);
    }

    /**
     * Withdraws the given amount from the account.
     * 
     * @param amount
     *            The amount to withdraw
     * @return The new account total after the transaction.
     */
    public double withdraw(double amount) {
        return account.withdraw(amount);
    }

    public String getFirstName() {
        return details.getFirstName();
    }

    public void setFirstName(String firstName) {
        this.details.setFirstName(firstName);
    }

    public String getLastName() {
        return details.getLastName();
    }

    public void setLastName(String lastName) {
        this.details.setLastName(lastName);
    }

    public String getAddress() {
        return details.getAddress();
    }

    public void setAddress(String address) {
        this.details.setAddress(address);
    }

    public String getPhone() {
        return details.getPhone();
    }

    public void setPhone(String phone) {
        this.details.setPhone(phone);
    }

    public int getAccountNumber() {
        return account.getAccountNumber();
    }

    public void setAccountNumber(int accountNumber) {
        account.setAccountNumber(accountNumber);
    }

    public double getAccountTotal() {
        return account.getAccountTotal();
    }

    /**
     * Returns the record as a String.
     * 
     * @return entry as format firstName;lastName;address;phone;
     *         accountNumber;accountTotal
     */
    @Override
    public String toString() {
        return getFirstName() + ";" + getLastName() + ";" + getAddress() + ";" + getPhone() + ";" + getAccountNumber()
                + ";" + getAccountTotal();
    }
}
