package sequencer;

import com.message.Message;
import com.reliable.ReliableUDP;

import java.io.IOException;

public class Sequencer {
	private static final String ID_PREFIX = "SEQ";
	private ReliableUDP reliableUdp;

	public Sequencer(String branchId) throws IOException {
		reliableUdp = new ReliableUDP(ID_PREFIX + branchId);
		reliableUdp.startUDPMulticast();
	}

	public void start() {
		System.out.println("Sequencer listening for requests.");
		while (true) {
			Message message = reliableUdp.receive();
			if (message != null) {
				System.out.println("Received message");
				System.out.println("\t-ID: " + message.getHeader().messageId);
				System.out.println("\t-Origin ID: " + message.getHeader().originId);
				System.out.println("\t-Origin Address: " + message.getHeader().originAddress);
				System.out.println("\t-Dest ID: " + message.getHeader().destinationId);
				System.out.println("\t-Dest Address: " + message.getHeader().destinationAddress + "\n");
				reliableUdp.multicast(message);
			}
		}
	}
}
