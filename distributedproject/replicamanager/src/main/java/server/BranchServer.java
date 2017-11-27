package server;

public interface BranchServer {

    /*
     *  NOTES
     *
     *  All amounts are strings. Since you may have used doubles to represent currency you'll have to convert.
     *
     *  String -> double
     *  double doubleValue = new BigDecimal(amount).doubleValue();
     */


    /*
     * Case: Create success
     * return: "Successfully created new customer and added to customer database. Customer id NEW_CUSTOMER_ID"
     *
     * Case: Trying to create a customer for a non existent branch
     * return: "You have entered an invalid branch"
     *
     * Case: Manager not found
     * return: "Could not find manager with id managerId"
     *
     * Case: Invalid manager ID (IE QC manager trying to create MB branch customer. MB used in this example)
     * return: "Manager with id managerId is not authorized to run operations on MB's server"
     */
    String createAccountRecord(String managerId, String firstName, String lastName, String address, String phone, String branch);

    /*
     * Field names: address, phone
     *
     * Case: Change success
     * return: "Changed field fieldName to newValue for customer with id customerId"
     *
     * Case: Bad field name
     * return: "Field name must be one of (address|phone)"
     *
     * Case: Manager not found
     * return: "Could not find manager with id managerId"
     *
     * Case: Customer not found
     * return: "Could not find customer with id customerId"
     */
    String editRecord(String managerId, String customerId, String fieldName, String newValue);

    /*
     * Case: Deposit success
     * return: "Deposited $amount into your account (customerId). New Balance: $xx.xx"
     *
     * Case: amount <= 0
     * return: "Cannot deposit a negative amount or $0."
     *
     * Case: Customer not found
     * return: "Could not find customer with id customerId"
     */
    String deposit(String customerId, String amount);

    /*
     * Case: Withdraw success
     * return: "Withdrew $amount from your account (customerId). New Balance: $xx.xx"
     *
     * Case: Not enough funds
     * return: "You do not have enough funds to withdraw $amount. Your balance is $xx.xx"
     *
     * Case: amount <= 0
     * return: "Cannot withdraw a negative amount or $0."
     *
     * Case: Customer not found
     * return: "Could not find customer with id customerId"
     */
    String withdraw(String customerId, String amount);

    /*
     * Case: Customer found
     * return: "Account Balance for Customer customerId: $xx.xx"
     *
     * Case: Customer not found
     * return: "Could not find customer with id customerId"
     */
    String getBalance(String customerId);
}
