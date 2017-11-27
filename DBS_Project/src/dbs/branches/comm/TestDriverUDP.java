package dbs.branches.comm;

import java.io.IOException;

public class TestDriverUDP {

	public TestDriverUDP() {

	}

	public static void main(String args[]) throws IOException {
		ReliableUDP udpModule1 = new ReliableUDP("test1");
		ReliableUDP udpModule2 = new ReliableUDP("test2");

		udpModule1.startUDPServer();
		udpModule2.startUDPServer();

		udpModule1.send(new BodySingleString("From test1 to Test2"), "test", "test2", null);
		udpModule1.send(new BodySingleString("From test1 to Test2"), "test", "test2", null);
		udpModule1.send(new BodySingleString("From test1 to Test2"), "test", "test2", null);
		udpModule1.send(new BodySingleString("From test1 to Test2"), "test", "test2", null);

		udpModule1.send(new BodySingleString("From test1 to Test1"), "test", "test1", null);

		udpModule2.send(new BodySingleString("From test2 to Test1"), "test", "test1", null);

		while (true) {
			Message test1 = udpModule1.receive();
			if (test1 != null) {
				BodySingleString body = (BodySingleString) test1.getBody();
				System.out.println(body.getStr1());
			}

			Message test2 = udpModule2.receive();
			if (test2 != null) {
				BodySingleString body = (BodySingleString) test2.getBody();
				System.out.println(body.getStr1());
			}

		}

	}

}
