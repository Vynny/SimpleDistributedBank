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
		while (true) {
			Message message = reliableUdp.receive();
			if (message != null) {
				System.out.println("Received message");
				System.out.println("\t-ID: " + message.getHeader().messageId);
				System.out.println("\t-Origin ID: " + message.getHeader().originId);
				System.out.println("\t-Dest ID: " + message.getHeader().destinationId + "\n");
				reliableUdp.multicast(message);
			}
		}
	}
}
