package dbs.branches.comm;

import java.io.Serializable;

public class Message implements Serializable {

	private String action;
	private MessageHeader header;
	private MessageBody body;

	public Message(String action, MessageHeader header, MessageBody body) {
		if (action == null || header == null) {
			throw new IllegalArgumentException("Action and header must not be null");
		}
		this.action = action;
		this.header = header;
		this.body = body;
	}

	public synchronized String getAction() {
		return action;
	}

	public synchronized void setAction(String action) {
		this.action = action;
	}

	public synchronized MessageHeader getHeader() {
		return header;
	}

	public synchronized void setHeader(MessageHeader header) {
		this.header = header;
	}

	public synchronized MessageBody getBody() {
		return body;
	}

	public synchronized void setBody(MessageBody body) {
		this.body = body;
	}
}
