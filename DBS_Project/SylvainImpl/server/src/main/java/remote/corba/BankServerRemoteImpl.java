package remote.corba;

import logging.BankLogger;
import models.account.BankAccount;
import models.branch.Branch;
import models.user.CustomerUser;
import models.user.ManagerUser;
import org.omg.CORBA.ORB;
import remote.database.CustomerDatabase;
import remote.database.ManagerDatabase;
import remote.util.CORBAConnector;

import java.math.BigDecimal;


public class BankServerRemoteImpl extends BankServerRemotePOA {

    private ORB orb;

    private Branch thisBranch;
    private CustomerDatabase customerDatabase;
    private ManagerDatabase managerDatabase;

    public BankServerRemoteImpl(Branch thisBranch, ORB orb) {
        this.orb = orb;

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
     * Server Operations
     */
    @Override
    public String transferAccount(String firstName, String lastName, String address, String phone, String balance) {
        BankLogger.logAction("-Received transferAccount command");
        BankLogger.logAction("\tfirstName: " + firstName);
        BankLogger.logAction("\tlastName: " + lastName);
        BankLogger.logAction("\taddress: " + address);
        BankLogger.logAction("\tphone: " + phone);
        BankLogger.logAction("\tbalance : $" + balance);

        BankLogger.logAction("-Transferring customer to branch " + thisBranch);
        CustomerUser customerUser = new CustomerUser(thisBranch, firstName, lastName, address, phone, balance);
        this.customerDatabase.addCustomer(customerUser);
        BankLogger.logAction("-Customer has been transferred and now has customerId " + customerUser.getCustomerId());

        return customerUser.getCustomerId();
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
        ManagerUser managerUser = managerDatabase.getManager(managerId);
        if (managerUser != null) {
            try {
                Branch branchEnum = Branch.valueOf(branch);

                if (branchEnum.toString().equals(managerUser.getManagerId().substring(0, 2))) {
                    CustomerUser customerUser = new CustomerUser(branchEnum, firstName, lastName, address, phone);
                    this.customerDatabase.addCustomer(customerUser);
                    response = BankLogger.logAndReturn("Successfully created new customer and added to customer database. \nCustomer Data: \n\t" + customerUser.toString());
                } else {
                    response = BankLogger.logAndReturn("Error: Manager with id " + managerId + " is not authorized to run operations on " + branchEnum + "'s server");
                }
            } catch (IllegalArgumentException e) {
                response = BankLogger.logAndReturn("Error: You have entered an invalid branch");
            }

        } else {
            response = BankLogger.logAndReturn("Error: Could not find manager with id: " + managerId);
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
        ManagerUser managerUser = managerDatabase.getManager(managerId);
        if (managerUser != null) {
            CustomerUser customerUser = customerDatabase.getCustomer(customerId);
            if (customerUser != null) {
                switch (fieldName.toLowerCase()) {
                    case "address":
                        customerUser.setAddress(newValue);
                        break;
                    case "phone":
                        customerUser.setPhoneNumber(newValue);
                        break;
                    case "branch":
                        try {
                            Branch newBranch = Branch.valueOf(newValue);
                            BankServerRemote serverRemote = CORBAConnector.connectServer(newBranch);

                            if (serverRemote != null) {
                                this.customerDatabase.removeCustomer(customerUser);
                                String customerUserTransferredID = serverRemote.transferAccount(customerUser.getFirstName(), customerUser.getLastName(), customerUser.getAddress(), customerUser.getPhoneNumber(), customerUser.getBankAccount().getBalance().toString());
                                return BankLogger.logAndReturn("Transferred user with id " + customerId + " to branch " + newBranch + ". Customer now has ID " + customerUserTransferredID + " and is no longer under branch " + thisBranch + "'s control.");
                            } else {
                                return BankLogger.logAndReturn("Error: Could not connect server for branch " + newBranch + ". Operation failed.");
                            }
                        } catch (IllegalArgumentException e) {
                            return BankLogger.logAndReturn("Error: Branch value supplied for newValue (" + newValue + ") is not a valid branch");
                        } catch (Exception e) {
                            e.printStackTrace();
                            return BankLogger.logAndReturn("Error: Could not connect to server for specified branch. It might be offline.");
                        }
                    default:
                        return BankLogger.logAndReturn("Error: fieldName must be one of (address|phone|branch)");
                }

                response = BankLogger.logAndReturn("Changed field " + fieldName + " to " + newValue + " for customer with id " + customerId + "\nCustomer Data: \n\t" + customerUser.toString());
            } else {
                response = BankLogger.logAndReturn("Error: Could not find customer with id: " + customerId);
            }
        } else {
            response = BankLogger.logAndReturn("Error: Could not find manager with id: " + managerId);
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

        String response;
        CustomerUser customerUser = this.customerDatabase.getCustomer(customerId);
        if (customerUser != null) {
            BankAccount bankAccount = customerUser.getBankAccount();

            if (!bankAccount.validateAmount(new BigDecimal(amount))) {
                response = BankLogger.logAndReturn("Cannot deposit a negative amount or $0.");
            } else {
                bankAccount.deposit(new BigDecimal(amount));
                response = BankLogger.logAndReturn("Deposited $" + amount + " into your account (" + customerId + "). New Balance: $" + bankAccount.getBalance());
            }

        } else {
            response = BankLogger.logAndReturn("Error: Could not find customer with id " + customerId);
        }

        return response;
    }

    @Override
    public String withdraw(String customerId, String amount) {
        BankLogger.logAction("-Received withdraw command with parameters");
        BankLogger.logAction("\tcustomerId: " + customerId);
        BankLogger.logAction("\tamount: " + amount);

        String response;
        CustomerUser customerUser = this.customerDatabase.getCustomer(customerId);
        if (customerUser != null) {
            BankAccount bankAccount = customerUser.getBankAccount();

            if (!bankAccount.validateAmount(new BigDecimal(amount))) {
                response = BankLogger.logAndReturn("Cannot withdraw a negative amount or $0.");
            } else {
                if (bankAccount.canWithdraw(new BigDecimal(amount))) {
                    bankAccount.withdraw(new BigDecimal(amount));
                    response = BankLogger.logAndReturn("Withdrew $" + amount + " from your account. (" + customerId + "). New Balance: $" + bankAccount.getBalance());
                } else {
                    response = BankLogger.logAndReturn("You do not have enough funds to withdraw $" + amount + ". Your balance is $" + bankAccount.getBalance() + ".");
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
    public void shutdown() {
        orb.shutdown(true);
    }
}
