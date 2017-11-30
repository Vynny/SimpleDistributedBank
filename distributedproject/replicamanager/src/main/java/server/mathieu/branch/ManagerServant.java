package server.mathieu.branch;

@Deprecated
public class ManagerServant {
	private BranchImpl branchServer;

	public ManagerServant(BranchImpl branchServer) {
		if (branchServer == null) {
			throw new IllegalArgumentException("arguments cannot be null");
		}
		this.branchServer = branchServer;
	}

	public String createAccountRecord(String managerId, String firstName, String lastName, String address, String phone,
			String branch) {
		if (!branch.equalsIgnoreCase(branchServer.ID)) {
			throw new BranchException("Create Account Sent to the wrong branch");
		}

		BranchImpl.logger.println(managerId,
				"ops: createAccountRecord; " + firstName + ", " + lastName + ", " + address + ", " + phone);
		String customerId = branchServer.enrollCustomer(firstName, lastName, address, phone);
		return customerId;
	}

	public boolean editRecord(String managerId, String customerId, String fieldName, String newValue) {
		CustomerRecord record = branchServer.getCustomerRecord(customerId);

		BranchImpl.logger
				.println("ops: editRecord; id: " + customerId + " field: " + fieldName + ", value: " + newValue);

		if (fieldName.equalsIgnoreCase("firstname")) {
			record.setFirstName(newValue);
		} else if (fieldName.equalsIgnoreCase("lastname")) {
			record.setLastName(newValue);
		} else if (fieldName.equalsIgnoreCase("address")) {
			record.setAddress(newValue);
		} else if (fieldName.equalsIgnoreCase("phone")) {
			record.setPhone(newValue);
		} else if (fieldName.equalsIgnoreCase("accountnumber")) {
			record.setAccountNumber(Integer.parseInt(newValue));
		} else {
			throw new BranchException("Invalid fieldname");
		}

		return true;
	}

	public int getAccountCount(String managerId) {
		// TODO Auto-generated method stub
		return 0;
	}

//	public double transferFund(String managerId, double amount, String sourceCustomerId, String destinationCustomerId) {
//		BranchServer.logger.println(managerId,
//				"Transfer fund of " + amount + " from " + sourceCustomerId + " to " + destinationCustomerId);
//		branchServer.transferFund(sourceCustomerId, amount, destinationCustomerId);
//		// TODO change to void
//		return Double.NaN;
//	}

}
