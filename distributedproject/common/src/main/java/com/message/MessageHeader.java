package com.message;

import java.io.Serializable;
import java.util.Map;

public class MessageHeader implements Serializable {

	public String group;

	/**
	 * Unique Distributed System message Id.
	 */
	public String messageId;

	/**
	 * Multicast Number
	 */
	public long sequenceNumber;

	/**
	 * Custom Id for meaningful replies for application specific use.
	 */
	public String customId;

	public boolean isReply;
	public boolean isAck;

	// Used to send back a reply to a different listener than the socket this
	// message was sent from
	public String originId;
	public String originAddress;
	public int originPort;

	// Used to send new messages
	public String destinationId;
	public String destinationAddress;
	public int destinationPort;
	
	// PiggyBack acks for Reliable Multicast
	public Map<String, Long> acks;
	
	public MessageHeader() {
		// TODO Auto-generated constructor stub
	}

}
