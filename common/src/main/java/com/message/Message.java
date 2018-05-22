package com.message;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((header == null) ? 0 : header.hashCode());
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
		Message other = (Message) obj;
		if (header == null) {
			if (other.header != null)
				return false;
		} else if (!header.equals(other.header))
			return false;
		return true;
	}
}
