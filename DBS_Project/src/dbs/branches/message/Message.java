package dbs.branches.message;

import java.io.Serializable;

public class Message implements Serializable {

	private String action;
	private MessageHeader header;
	private MessageBody body;
	private MessageFault fault;

	public Message(String action, MessageHeader header, MessageBody body) {
		this(action, header, body, null);
	}

	public Message(String action, MessageHeader header, MessageBody body, MessageFault fault) {
		if (action == null || header == null || body == null) {
			throw new IllegalArgumentException("Action, header and body must not be null");
		}
		this.action = action;
		this.header = header;
		this.body = body;
		this.fault = fault;
	}
}
