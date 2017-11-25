package models.user;

import models.account.BankAccount;
import models.branch.Branch;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomerUser implements Serializable {

    private static final String CLIENT_PREFIX = "C";

    private static AtomicInteger lastId = new AtomicInteger(1000);

    private String customerId;
    private String firstName;
    private String lastName;
    private String address;
    private String phoneNumber;
    private BankAccount bankAccount;

    public CustomerUser(Branch branch, String firstName, String lastName, String address, String phoneNumber) {
        this.customerId = generateUserId(branch);
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.bankAccount = new BankAccount();
    }

    public CustomerUser(Branch branch, String firstName, String lastName, String address, String phoneNumber, String initialBalance) {
        this.customerId = generateUserId(branch);
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.bankAccount = new BankAccount(initialBalance);
    }

    public static Branch extractBranch(String customerId) {
        Branch theBranch;
        try {
            theBranch = Branch.valueOf(customerId.substring(0, 2));
        } catch (IllegalArgumentException e) {
            theBranch = null;
        }
        return theBranch;
    }

    private String generateUserId(Branch branch) {
        return branch.toString() + CLIENT_PREFIX + lastId.getAndIncrement();
    }

    public String getFirstName() {
        return firstName;
    }

    public synchronized void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public synchronized void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public synchronized void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public synchronized void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public synchronized BankAccount getBankAccount() {
        return bankAccount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomerUser)) return false;

        CustomerUser that = (CustomerUser) o;

        if (customerId != null ? !customerId.equals(that.customerId) : that.customerId != null) return false;
        if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null) return false;
        if (lastName != null ? !lastName.equals(that.lastName) : that.lastName != null) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        return phoneNumber != null ? phoneNumber.equals(that.phoneNumber) : that.phoneNumber == null;
    }

    @Override
    public int hashCode() {
        int result = customerId != null ? customerId.hashCode() : 0;
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CustomerUser{" +
                "customerId='" + customerId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", bankAccount=" + bankAccount +
                '}';
    }
}
