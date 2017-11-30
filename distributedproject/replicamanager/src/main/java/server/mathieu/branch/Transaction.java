package server.mathieu.branch;

public class Transaction {
	private static int nextTransactionId = 0;

	private String branchId;
	private TransactionAction action;
	private int transactionId;
	private boolean commited;
	private boolean cancelled;

	public Transaction(String branchId, TransactionAction action) {
		if (action == null) {
			throw new IllegalArgumentException("action cannot be null");
		}
		this.branchId = branchId.toUpperCase();
		this.action = action;
		this.transactionId = getNextTransactionId();
		this.commited = false;
		this.cancelled = false;
	}

	public boolean commit() {
		if (!commited && !cancelled) {
			commited = action.execute();
		}
		return commited;
	}

	public boolean cancel() {
		if (!commited) {
			cancelled = true;
		}
		return cancelled;
	}

	public String getTransactionId() {
		return branchId + transactionId;
	}

	private static int getNextTransactionId() {
		int nextId = nextTransactionId;
		if (nextTransactionId == Integer.MAX_VALUE) {
			nextTransactionId = 0;
		} else {
			nextTransactionId++;
		}
		return nextId;
	}

	public static interface TransactionAction {
		public boolean execute();
	}
}
