package com.message;

import java.io.Serializable;

public class MessageHeader implements Serializable {

	public String group;

	/**
	 * Unique Distributed System message Id.
	 */
	public String messageId;

	/**
	 * Id for total ordering
	 */
	public String sequenceId;

	/**
	 * Custom Id for meaningful replies for application specific use.
	 */
	public String customId;

	public boolean isReply;
	public boolean isAck;
	public boolean isTotallyOrdered;

	// Used to send back a reply to a different listener than the socket this
	// message was sent from
	public String originId;
	public String originAddress;
	public int originPort;

	// Used to send new messages
	public String destinationId;
	public String destinationAddress;
	public int destinationPort;

	public MessageHeader() {
		// TODO Auto-generated constructor stub
	}

}
