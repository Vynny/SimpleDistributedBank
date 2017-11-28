package sequencer;

import java.io.IOException;

import com.message.Message;
import com.reliable.ReliableUDP;

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
				reliableUdp.multicast(message);
			}
		}
	}
}
