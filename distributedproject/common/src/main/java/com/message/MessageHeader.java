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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destinationId == null) ? 0 : destinationId.hashCode());
		result = prime * result + ((messageId == null) ? 0 : messageId.hashCode());
		result = prime * result + ((originId == null) ? 0 : originId.hashCode());
		result = prime * result + (int) (sequenceNumber ^ (sequenceNumber >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageHeader other = (MessageHeader) obj;
		if (destinationId == null) {
			if (other.destinationId != null)
				return false;
		} else if (!destinationId.equals(other.destinationId))
			return false;
		if (messageId == null) {
			if (other.messageId != null)
				return false;
		} else if (!messageId.equals(other.messageId))
			return false;
		if (originId == null) {
			if (other.originId != null)
				return false;
		} else if (!originId.equals(other.originId))
			return false;
		if (sequenceNumber != other.sequenceNumber)
			return false;
		return true;
	}

}
