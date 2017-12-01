package server.mathieu.branch;

import server.BranchServer;

import java.math.BigDecimal;
import java.util.List;

public class MathieuBranchImpl implements BranchServer {
	BranchImpl branchServer;

	public MathieuBranchImpl(String identifier) {
		this.branchServer = new BranchImpl(identifier);
	}

	@Override
	public String createAccountRecord(String managerId, String firstName, String lastName, String address, String phone,
			String branch) {
		if (!branch.equalsIgnoreCase(branchServer.ID)) {
			return "You have entered an invalid branch";
		}

		BranchImpl.logger.println(managerId,
				"ops: createAccountRecord; " + firstName + ", " + lastName + ", " + address + ", " + phone);
		String customerId = branchServer.enrollCustomer(firstName, lastName, address, phone);

		return "Successfully created new customer and added to customer database. Customer id " + customerId;
	}

	@Override
	public String editRecord(String managerId, String customerId, String fieldName, String newValue) {
		BranchImpl.logger
				.println("ops: editRecord; id: " + customerId + " field: " + fieldName + ", value: " + newValue);

		CustomerRecord record = branchServer.getCustomerRecord(customerId);
		if (record == null) {
			return "Could not find customer with id " + customerId;
		}

		if (fieldName.equalsIgnoreCase("address")) {
			record.setAddress(newValue);
		} else if (fieldName.equalsIgnoreCase("phone")) {
			record.setPhone(newValue);
		} else {
			return "Field name must be one of (address|phone)";
		}

		return "Changed field " + fieldName + " to " + newValue + " for customer with id " + customerId;
	}

	@Override
	public String deposit(String customerId, String amount) {
		double depositAmount = new BigDecimal(amount).doubleValue();
		double newBalance = Double.NaN;
		try {
			newBalance = branchServer.deposit(customerId, depositAmount);
		} catch (BranchException e) {
			return e.getMessage();
		}

		return String.format("Deposited $%s into your account (%s). New Balance: $%.2f", amount, customerId,
				newBalance);
	}

	@Override
	public String withdraw(String customerId, String amount) {
		double withdrawAmount = new BigDecimal(amount).doubleValue();
		double newBalance = Double.NaN;
		try {
			newBalance = branchServer.withdraw(customerId, withdrawAmount);
		} catch (BranchException e) {
			return e.getMessage();
		}

		return String.format("Withdrew $%.2f from your account (%s). New Balance: $%.2f", new BigDecimal(amount).doubleValue(), customerId, newBalance);
	}

	@Override
	public String getBalance(String customerId) {
		double balance = Double.NaN;
		try {
			balance = branchServer.getBalance(customerId);
		} catch (BranchException e) {
			return e.getMessage();
		}

		return String.format("Account Balance for Customer %s: $%.2f", customerId, balance);
	}

	@Override
	public String getAccountCount() {
		return branchServer.ID + ": " + branchServer.getAccountCount();
	}

	@Override
	public List<String> dumpDatabase() {
		return branchServer.saveCustomerRecords();
	}

	@Override
	public void restoreDatabase(List<String> databaseDump) {
		branchServer.loadCustomerRecords(databaseDump);
	}

}
