package server.mathieu.branch;

import java.util.concurrent.ConcurrentHashMap;

import server.mathieu.branch.interbranch.BranchLocation;
import server.mathieu.branch.interbranch.InterBranchClient;
import server.mathieu.branch.interbranch.InterBranchPacket;

public class TransactionManager {
	private String branchId;

	/**
	 * Map with key branchId-tranId
	 */
	private ConcurrentHashMap<String, Transaction> transactionMap;

	public TransactionManager(String branchId) {
		transactionMap = new ConcurrentHashMap<>();
		this.branchId = branchId.toUpperCase();
	}

	public void createTransferTransaction(CustomerRecord sourceRecord, BranchLocation destination,
			String destinationCustomerId, double amount) {
		Transaction transaction = new Transaction(branchId, () -> {
			sourceRecord.withdraw(amount);
			return true;
		});
		transactionMap.put(transaction.getTransactionId(), transaction);

		InterBranchClient.transferFund(destination, transaction, destinationCustomerId, amount);
	}

	public Transaction receiveTransferTransaction(InterBranchPacket packet, CustomerRecord destinationRecord) {
		Transaction transaction = new Transaction(branchId, () -> {
			destinationRecord.deposit(packet.amount);
			BranchImpl.logger.println(
					"Transaction: " + packet.transactionId + ", Received transfer of fund into " + packet.clientId);
			return true;
		});
		transactionMap.put(transaction.getTransactionId(), transaction);
		return transaction;
	}

	public Transaction getTransferTransaction(String transactionId) {
		return transactionMap.get(transactionId);
	}

}
