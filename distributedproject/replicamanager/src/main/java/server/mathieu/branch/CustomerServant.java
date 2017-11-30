package server.mathieu.branch;

@Deprecated
public class CustomerServant {
	// private ORB orb;
	private BranchImpl branch;

	public CustomerServant(BranchImpl branch) {
		if (branch == null) {
			throw new IllegalArgumentException("the branch cannot be null");
		}
		this.branch = branch;
	}

	public double deposit(String customerId, double amount) {
		return branch.deposit(customerId, amount);
	}

	public double withdraw(String customerId, double amount) {
		return branch.withdraw(customerId, amount);
	}

	public double getBalance(String customerId) {
		return branch.getBalance(customerId);
	}

//	public double transferFund(String sourceCustomerId, double amount, String destinationCustomerId) {
//		BranchServer.logger.println(sourceCustomerId, "Transfer fund to " + destinationCustomerId);
//		branch.transferFund(sourceCustomerId, amount, destinationCustomerId);
//		// TODO change to void
//		return Double.NaN;
//	}
}
