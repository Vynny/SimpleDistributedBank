package com.message;

public class NACKBody implements MessageBody {

	public NACKBody(String originId, long first, long last) {
		this.originId = originId;
		this.firstMissingId = first;
		this.lastMissingId = last;
	}

	public String originId;
	public long firstMissingId;
	public long lastMissingId;
}
