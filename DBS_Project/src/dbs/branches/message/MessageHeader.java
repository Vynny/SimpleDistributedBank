package dbs.branches.message;

import java.io.Serializable;

public class MessageHeader implements Serializable{
	
	public String clientId;
	public String transactionId;
	public boolean isError;
	public boolean isReply;
	public String originHost;
	public int originPort;
	public String destinationHost;
	public int destinationPort;
	
	public MessageHeader() {
		// TODO Auto-generated constructor stub
	}

}
