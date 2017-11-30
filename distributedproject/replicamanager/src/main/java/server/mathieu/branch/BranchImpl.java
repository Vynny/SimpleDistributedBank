package server.mathieu.branch;

import java.util.Map;
import java.util.Random;
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
		CustomerRecord newCustomer = new CustomerRecord(firstName, lastName, address, phone,
				generateUniqueAccountNumber());
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

		return record.getAccountTotal();
	}
	
	public int getAccountCount() {
		return customerRecordMap.size();
	}

	// public double transferFund(String sourceCustomerId, double amount, String
	// destinationCustomerId) {
	// CustomerRecord sourceRecord = getCustomerRecord(sourceCustomerId);
	// if (sourceRecord == null) {
	// logger.println("TransferFund request for source " + sourceCustomerId
	// + " cannot be completed, source does not exist");
	// throw new BranchException("Invalid source id");
	// }
	// BranchLocation destination;
	// try {
	// destination = dbsService.getBranchLocation(destinationCustomerId);
	// } catch (ServiceException e) {
	// String errorMessage = "Unable to retrieve the branch location for destination
	// id: " + destinationCustomerId;
	// logger.println(errorMessage);
	// throw new BranchException(errorMessage);
	// }
	//
	// transactionManager.createTransferTransaction(sourceRecord, destination,
	// destinationCustomerId, amount);
	// return Double.NaN;
	// }

	// public Transaction addTransferedFund(InterBranchPacket packet) {
	// CustomerRecord record = getCustomerRecord(packet.clientId);
	// if (record == null) {
	// return null;
	// }
	// Transaction transaction =
	// transactionManager.receiveTransferTransaction(packet, record);
	// return transaction;
	// }

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

	private int generateUniqueAccountNumber() {
		Random rand = new Random();
		int candidate;
		do {
			candidate = rand.nextInt(Account.ACCOUNT_NUMBER_MAX_VALUE - Account.ACCOUNT_NUMBER_MIN_VALUE)
					+ Account.ACCOUNT_NUMBER_MIN_VALUE;
		} while (customerRecordMap.get(candidate) != null);
		return candidate;
	}

	public void loadCustomerRecord(CustomerRecord record) {
		if (record == null) {
			throw new IllegalArgumentException("Customer Record is null");
		}
		addCustomer(record);
	}

	public void loadManagerRecord(String managerId) {
		if (managerId == null) {
			throw new IllegalArgumentException("ManagerNumber cannot be null");
		}

		managerMap.put(managerId, ID);
	}

	private void loadCustomerData() {
		DataLoader.loadCustomerRecord(this, "customerData" + ID + ".csv");
	}

	private void loadManagerData() {
		DataLoader.loadManager(this, "managerData" + ID + ".csv");
	}

	/**
	 * Starts the server with argument (branchId).
	 *
	 * @param args
	 */
	public static void main(String args[]) {
		if (args.length < 1) {
			System.out.println("Invalid argument, use prog <branchId>");
			System.exit(0);
		}
		String branchId = args[0];
		logger = new Logger(branchId + ".log", true);
		logger.println("Initializing branch with id: " + branchId);

		try {
			BranchImpl branch = new BranchImpl(branchId);
			logger.println(branch.ID + " Branch ready");
		} catch (Exception e) {
			logger.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
