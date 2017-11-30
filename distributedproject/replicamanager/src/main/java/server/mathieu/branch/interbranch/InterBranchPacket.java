package server.mathieu.branch.interbranch;

import java.io.Serializable;

public class InterBranchPacket implements Serializable {
	/**
	 * v0.1
	 */
	private static final long serialVersionUID = 7975201675385102335L;

	public String method;
	public String clientId;
	public double amount;
	public String transactionId;
	public String errMessage;

	public InterBranchPacket(String method, String transactionId, String clientId, double amount) {
		this.method = method;
		this.clientId = clientId;
		this.amount = amount;
		this.transactionId = transactionId;
	}

	public InterBranchPacket(String method, String transactionId) {
		this(method, transactionId, null, Double.NaN);
	}
}
