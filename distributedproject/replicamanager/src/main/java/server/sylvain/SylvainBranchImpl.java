package server.sylvain;


import enums.Branch;
import server.BranchServer;
import logging.BankLogger;
import server.sylvain.common.models.account.BankAccount;
import server.sylvain.common.models.user.CustomerUser;
import server.sylvain.common.models.user.ManagerUser;
import server.sylvain.server.database.CustomerDatabase;
import server.sylvain.server.database.ManagerDatabase;

import java.math.BigDecimal;
import java.util.List;


public class SylvainBranchImpl implements BranchServer {

    private Branch thisBranch;
    private CustomerDatabase customerDatabase;
    private ManagerDatabase managerDatabase;

    public SylvainBranchImpl(Branch thisBranch) {
        this.thisBranch = thisBranch;

        //Initialize Databases
        this.customerDatabase = CustomerDatabase.getInstance();
            this.customerDatabase.addCustomer(new CustomerUser(thisBranch, "Bob", "Bobson", "2555-Test-Road", "444-444-4444"));
            this.customerDatabase.addCustomer(new CustomerUser(thisBranch, "Tim", "Timson", "2555-Testing-Street", "444-444-4444"));

        this.managerDatabase = ManagerDatabase.getInstance();
        this.managerDatabase.addManager(new ManagerUser(thisBranch));
        this.managerDatabase.addManager(new ManagerUser(thisBranch));
    }

    /*
     * Manager Operations
     */
    @Override
    public String createAccountRecord(String managerId, String firstName, String lastName, String address, String phone, String branch) {
        BankLogger.logAction("-Received createAccountRecord command with parameters");
        BankLogger.logAction("\tmanagerId: " + managerId);
        BankLogger.logAction("\tFirst Name: " + firstName);
        BankLogger.logAction("\tLast Name: " + lastName);
        BankLogger.logAction("\tAddress: " + address);
        BankLogger.logAction("\tPhone Number: " + phone);
        BankLogger.logAction("\tBranch: " + branch);

        String response;
        try {
            Branch branchEnum = Branch.valueOf(branch);

            CustomerUser customerUser = new CustomerUser(branchEnum, firstName, lastName, address, phone);
            this.customerDatabase.addCustomer(customerUser);
            response = BankLogger.logAndReturn("Successfully created new customer and added to customer database. Customer id: " + customerUser.getCustomerId());

        } catch (IllegalArgumentException e) {
            response = BankLogger.logAndReturn("You have entered an invalid branch");
        }

        return response;
    }

    @Override
    public String editRecord(String managerId, String customerId, String fieldName, String newValue) {
        BankLogger.logAction("-Received editRecord command with parameters");
        BankLogger.logAction("\tmanagerId: " + managerId);
        BankLogger.logAction("\tcustomerId: " + customerId);
        BankLogger.logAction("\tField Name: " + fieldName);
        BankLogger.logAction("\tNew Value: " + newValue);

        String response;

        CustomerUser customerUser = customerDatabase.getCustomer(customerId);
        if (customerUser != null) {
            switch (fieldName.toLowerCase()) {
                case "address":
                    customerUser.setAddress(newValue);
                    break;
                case "phone":
                    customerUser.setPhoneNumber(newValue);
                    break;
                default:
                    return BankLogger.logAndReturn("Field name must be one of (address|phone)");
            }

            response = BankLogger.logAndReturn("Changed field " + fieldName + " to " + newValue + " for customer with id " + customerId);
        } else {
            response = BankLogger.logAndReturn("Could not find customer with id: " + customerId);
        }

        return response;
    }

    /*
     * Client Operations
     */
    @Override
    public String deposit(String customerId, String amount) {
        BankLogger.logAction("-Received deposit command with parameters");
        BankLogger.logAction("\tcustomerId: " + customerId);
        BankLogger.logAction("\tamount: " + amount);

        BigDecimal moneyAmount = moneyFormat(amount);

        String response;
        CustomerUser customerUser = this.customerDatabase.getCustomer(customerId);
        if (customerUser != null) {
            BankAccount bankAccount = customerUser.getBankAccount();

            if (!bankAccount.validateAmount(moneyAmount)) {
                response = BankLogger.logAndReturn("Cannot deposit a negative amount or $0.");
            } else {

                if (moneyAmount.compareTo(new BigDecimal("423")) == 0) {
                    //BYZANTINE ERROR
                    bankAccount.deposit(moneyAmount.subtract(new BigDecimal("23")));
                } else {
                    //NORMAL CASE
                    bankAccount.deposit(moneyAmount);
                }

                response = BankLogger.logAndReturn("Deposited $" + amount + " into your account (" + customerId + "). New Balance: $" + bankAccount.getBalance());
            }

        } else {
            response = BankLogger.logAndReturn("Could not find customer with id " + customerId);
        }

        return response;
    }

    @Override
    public String withdraw(String customerId, String amount) {
        BankLogger.logAction("-Received withdraw command with parameters");
        BankLogger.logAction("\tcustomerId: " + customerId);
        BankLogger.logAction("\tamount: " + amount);

        BigDecimal moneyAmount = moneyFormat(amount);

        String response;
        CustomerUser customerUser = this.customerDatabase.getCustomer(customerId);
        if (customerUser != null) {
            BankAccount bankAccount = customerUser.getBankAccount();

            if (!bankAccount.validateAmount(moneyAmount)) {
                response = BankLogger.logAndReturn("Cannot withdraw a negative amount or $0");
            } else {
                if (bankAccount.canWithdraw(moneyAmount)) {
                    bankAccount.withdraw(moneyAmount);
                    response = BankLogger.logAndReturn("Withdrew $" + moneyAmount.toString() + " from your account. (" + customerId + "). New Balance: $" + bankAccount.getBalance());
                } else {
                    response = BankLogger.logAndReturn("You do not have enough funds to withdraw $" + moneyAmount.toString() + ". Your balance is $" + bankAccount.getBalance());
                }
            }

        } else {
            response = BankLogger.logAndReturn("Could not find customer with id " + customerId);
        }

        return response;
    }

    @Override
    public String getBalance(String customerId) {
        BankLogger.logAction("-Received getBalance command with parameters");
        BankLogger.logAction("\tcustomerId: " + customerId);

        String response;
        CustomerUser customerUser = this.customerDatabase.getCustomer(customerId);
        if (customerUser != null)
            response = BankLogger.logAndReturn("Account Balance for Customer " + customerId + ": $" + customerUser.getBankAccount().getBalance());
        else
            response = BankLogger.logAndReturn("Could not find customer with id " + customerId);

        return response;
    }

    @Override
    public String getAccountCount() {
        return thisBranch.toString() + ": " + this.customerDatabase.getCustomerCount();
    }

    @Override
    public List<String> dumpDatabase() {
        //NOT SUPPORTED
        return null;
    }

    @Override
    public void restoreDatabase(List<String> databaseDump) {
        //NOT SUPPORTED
    }

    private BigDecimal moneyFormat(String string){
        return new BigDecimal(string).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
