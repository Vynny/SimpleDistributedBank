package dbs.branches.comm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class ReliableUDP {
	private static final String LOCATION_DB_FILENAME = "location.db";
	private static final String LOCATION_DELIMITER = ";";

	private static String address = "127.0.0.1";
	private static long nextMessageSequenceNumber = 0;

	private final String id;

	private int listenerPort;
	private UDPListener listener;
	private Set<String> groups;
	private Map<String, InetSocketAddress> locationDb;

	public ReliableUDP(String id) throws SocketException {
		this.id = id;

		groups = new HashSet<String>();
		locationDb = buildLocationDb(new File(LOCATION_DB_FILENAME));
		listenerPort = getLocation(id).getPort();
	}

	public void startUDPServer() throws IOException {
		listener = new UDPListener(id, listenerPort);
		listener.start();
	}

	public boolean registerGroup(String groupId) {
		return groups.add(groupId);
	}

	public boolean unregisterGroup(String groupId) {
		return groups.remove(groupId);
	}

	/**
	 * Sends a new message.
	 * 
	 * @param body
	 * @param action
	 * @param destinationId
	 * @param customId
	 */
	public void send(MessageBody body, String action, String destinationId, String customId) throws IOException {
		Message message = buildMessage(action, body, destinationId, customId);
		UDPClient client = new UDPClient(message);

		client.start();
	}

	/**
	 * Forwards an existing message to a new destination.
	 * 
	 * @param message
	 * @param destinationId
	 */
	public void send(Message message, String destinationId) {

	}

	/**
	 * Returns the next message received by the server.
	 * 
	 * This will follow the total order for any ordered message received by the
	 * listener.
	 * 
	 * @return The next message or null if none.
	 */
	public Message receive() {
		if (listener == null) {
			throw new IllegalStateException("No server is currently active");
		}

		Message nextMessage = listener.receive();
		return nextMessage;
	}

	/**
	 * Sends a new message.
	 * 
	 * @param body
	 * @param action
	 * @param groupId
	 * @param customId
	 */
	public void multicast(MessageBody body, String action, String groupId, String customId) {

	}

	/**
	 * Forwards an existing message to a new destination.
	 * 
	 * @param message
	 * @param groupId
	 */
	public void multicast(Message message, String groupId) {

	}

	private Message buildMessage(String action, MessageBody body, String destinationId, String customId) {
		MessageHeader header = buildHeader(destinationId, customId);
		Message message = new Message(action, header, body);

		return message;
	}

	private MessageHeader buildHeader(String destinationId, String customId) {
		InetSocketAddress destinationLocation = getLocation(destinationId);
		MessageHeader header = new MessageHeader();
		header.messageId = getNextMessageId();
		header.customId = customId;

		header.originId = id;
		header.originAddress = address;
		header.originPort = listenerPort;

		header.destinationId = destinationId;
		header.destinationAddress = destinationLocation.getAddress().getHostAddress();
		header.destinationPort = destinationLocation.getPort();

		header.isReply = false;
		header.isAck = false;

		return header;
	}

	private String getNextMessageId() {
		return id + nextMessageSequenceNumber++;
	}

	private InetSocketAddress getLocation(String id) {
		return locationDb.get(id);
	}

	private Map<String, InetSocketAddress> buildLocationDb(File db) {
		Scanner sc = null;
		try {
			sc = new Scanner(db);
		} catch (FileNotFoundException e) {
			System.err.println("Unable to open the locationDb");
			return null;
		}

		Map<String, InetSocketAddress> map = new HashMap<>();
		while (sc.hasNextLine()) {
			String[] nextEntry = sc.nextLine().split(LOCATION_DELIMITER);
			map.put(nextEntry[0], new InetSocketAddress(nextEntry[1], Integer.parseInt(nextEntry[2])));
		}
		sc.close();

		return map;
	}
}
