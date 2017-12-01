package server.mathieu.branch;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BranchImpl {
    public static final String DBS_SERVICE_NAME = "DBS_Service";
    public static Logger logger;

    public final String ID;

    /**
     * Database of account Number to customerRecord
     */
    private Map<Integer, CustomerRecord> customerRecordMap;

    /**
     * Manager map of ManagerId to BranchId
     */
    private Map<String, String> managerMap;

    public BranchImpl(String identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("The Branch identifier cannot be null");
        }
        this.customerRecordMap = new ConcurrentHashMap<>();
        this.managerMap = new ConcurrentHashMap<>();
        this.ID = identifier;

        if (logger == null) {
            logger = new Logger(identifier + ".log", true);
        }

        // Dummy account for tests
        loadCustomerRecord(new CustomerRecord("Batman", "Wayne", "123 main street", "555-555-5555", 1000));
        loadCustomerRecord(new CustomerRecord("Batman", "Wayne", "123 main street", "555-555-5555", 1001));
    }

    /**
     * Retrieves and returns a customer from this branch database.
     *
     * @param accountNumber
     *            The account number associated with the customer.
     * @return The Customer found or null otherwise
     */
    public CustomerRecord getCustomerRecord(String customerId) {
        validateCustomerId(customerId);

        int accountNumber = parseAccountNumber(customerId);
        CustomerRecord record = customerRecordMap.get(accountNumber);

        return record;
    }

    /**
     * Adds a given customer to the branch.
     *
     * @param firstName
     * @param lastName
     * @param address
     * @param phone
     * @return The customer Id
     */
    public String enrollCustomer(String firstName, String lastName, String address, String phone) {
        if (lastName == null) {
            throw new IllegalArgumentException("The customer must have a last name");
        }
        CustomerRecord newCustomer = new CustomerRecord(firstName, lastName, address, phone);
        addCustomer(newCustomer);
        logger.println("Enrolled customer with details: " + newCustomer.getAccountNumber() + ", "
                + newCustomer.getFirstName() + ", " + newCustomer.getLastName() + ", " + newCustomer.getAddress() + ", "
                + newCustomer.getPhone());
        return buildCustomerId(newCustomer.getAccountNumber());
    }

    public double deposit(String customerId, double amount) {
        CustomerRecord record = getCustomerRecord(customerId);
        if (record == null) {
            throw new BranchException("Could not find customer with id " + customerId);
        }

        double total = record.deposit(amount);
        return total;
    }

    public double withdraw(String customerId, double amount) {
        CustomerRecord record = getCustomerRecord(customerId);
        if (record == null) {
            throw new BranchException("Could not find customer with id " + customerId);
        }

        double total = record.withdraw(amount);
        return total;
    }

    public double getBalance(String customerId) {
        CustomerRecord record = getCustomerRecord(customerId);
        if (record == null) {
            throw new BranchException("Could not find customer with id " + customerId);
        }

        return record.getAccountTotal();
    }

    public int getAccountCount() {
        return customerRecordMap.size();
    }

    private void validateCustomerId(String customerId) {
        if (customerId == null || !customerId.substring(0, 3).equalsIgnoreCase(ID + "C") || customerId.length() != 7) {
            throw new BranchException("Could not find customer with id " + customerId);
        }
    }

    private int parseAccountNumber(String customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId cannot be null");
        }
        return Integer.parseInt(customerId.substring(3));
    }

    private String buildCustomerId(int accountNumber) {
        Account.validateAccountNumber(accountNumber);

        String customerId = ID + "C" + accountNumber;
        return customerId;
    }

    private void addCustomer(CustomerRecord customer) {
        customerRecordMap.put(customer.getAccountNumber(), customer);
    }

    public void loadCustomerRecord(CustomerRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Customer Record is null");
        }
        addCustomer(record);
    }

    /**
     * Load List of string (; separator) with Customer Records into the branch.
     * 
     * @param branch
     * @param inputFile
     *            input string of the form (firstName;lastName;address;phone;
     *            accountNumber;accountTotal)
     * @return
     */
    public void loadCustomerRecords(List<String> input) {
        for (String recordLine : input) {
            String[] fields = recordLine.split(";");
            CustomerRecord record = new CustomerRecord(fields[0], fields[1], fields[2], fields[3],
                    Integer.parseInt(fields[4]), Double.parseDouble(fields[5]));
            loadCustomerRecord(record);
            BranchImpl.logger.println("Loading record from " + recordLine);
        }
    }

    public List<String> saveCustomerRecords() {
        List<String> db = new LinkedList<>();
        for (CustomerRecord record : customerRecordMap.values()) {
            db.add(record.toString());
        }
        return db;
    }

    public void loadManagerRecord(String managerId) {
        if (managerId == null) {
            throw new IllegalArgumentException("ManagerNumber cannot be null");
        }

        managerMap.put(managerId, ID);
    }
}
