package com.reliable;

import java.io.IOException;

import com.message.Message;

import messages.branch.BranchReplyBody;

public class TestDriverUDP {

	public TestDriverUDP() {

	}

	public static void main(String args[]) throws IOException {
		ReliableUDP udpModule1 = new ReliableUDP("test1");
		ReliableUDP udpModule2 = new ReliableUDP("test2");

		BranchReplyBody replyTest12 = new BranchReplyBody();
		replyTest12.setReply("From test1 to Test2");
		udpModule1.send(replyTest12, "test", "test2", null);
		udpModule1.send(replyTest12, "test", "test2", null);
		udpModule1.send(replyTest12, "test", "test2", null);
		udpModule1.send(replyTest12, "test", "test2", null);

		BranchReplyBody replyTest11 = new BranchReplyBody();
		replyTest11.setReply("From test1 to Test1");
		udpModule1.send(replyTest11, "test", "test1", null);

		BranchReplyBody replyTest21 = new BranchReplyBody();
		replyTest21.setReply("From test2 to Test1");
		udpModule2.send(replyTest21, "test", "test1", null);

		while (true) {
			Message test1 = udpModule1.receive();
			if (test1 != null) {
				BranchReplyBody body = (BranchReplyBody) test1.getBody();
				System.out.println(body.getReply());
			}

			Message test2 = udpModule2.receive();
			if (test2 != null) {
				BranchReplyBody body = (BranchReplyBody) test2.getBody();
				System.out.println(body.getReply());
			}

		}

	}

}
