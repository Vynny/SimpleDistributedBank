package com.reliable;

import com.message.Message;
import com.message.MessageBody;
import com.message.MessageHeader;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.*;

public class ReliableUDP {
	private static final String LOCATION_DB_FILENAME = "location.db";
	private static final String GROUPS_DB_FILENAME = "groups.db";
	private static final String LOCATION_DELIMITER = ";";

	private static long nextMessageSequenceNumber = 0;

	private final String id;

	private UDPListener listener;
	private Map<String, InetSocketAddress> locationDb;
	private Map<String, List<String>> groupDb;

	public ReliableUDP(String id) throws IOException {
		this.id = id;

		groupDb = buildGroupDb(ClassLoader.getSystemResourceAsStream(GROUPS_DB_FILENAME));
		locationDb = buildLocationDb(ClassLoader.getSystemResourceAsStream(LOCATION_DB_FILENAME));

		if (UDPHelper.ANONYMOUS_ID.equals(id)) {
			listener = new UDPListener(id);
		} else {
			listener = new UDPListener(id, getLocation(id).getPort());
		}
		listener.start();
	}

	/**
	 * Build a reliable UDP module with an Anonymous ID
	 * 
	 * @throws IOException
	 */
	public ReliableUDP() throws IOException {
		this(UDPHelper.ANONYMOUS_ID);
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

	public void reply(MessageHeader originHeader, MessageBody replyBody, String action) throws SocketException {
		Message message = buildReplyMessage(originHeader, action, replyBody);
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
	 * <p>
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

	private Message buildReplyMessage(MessageHeader origin, String action, MessageBody body) {
		MessageHeader header = buildReplyHeader(origin);
		Message message = new Message(action, header, body);

		return message;
	}

	private MessageHeader buildHeader(String destinationId, String customId) {
		InetSocketAddress destinationLocation = getLocation(destinationId);
		MessageHeader header = new MessageHeader();
		header.messageId = getNextMessageId();
		header.customId = customId;

		header.originId = id;
		header.originAddress = listener.getSocketAddress();
		header.originPort = listener.getSocketPort();

		header.destinationId = destinationId;
		header.destinationAddress = destinationLocation.getAddress().getHostAddress();
		header.destinationPort = destinationLocation.getPort();

		header.isReply = false;
		header.isAck = false;

		return header;
	}

	private MessageHeader buildReplyHeader(MessageHeader origin) {
		MessageHeader header = new MessageHeader();
		header.messageId = getNextMessageId();
		header.customId = origin.customId;

		header.originId = id;
		header.originAddress = listener.getSocketAddress();
		header.originPort = listener.getSocketPort();

		header.destinationId = origin.originId;
		header.destinationAddress = origin.originAddress;
		header.destinationPort = origin.originPort;

		header.isReply = true;
		header.isAck = false;

		return header;
	}

	private String getNextMessageId() {
		return id + nextMessageSequenceNumber++;
	}

	private InetSocketAddress getLocation(String id) {
		return locationDb.get(id);
	}

	private Map<String, InetSocketAddress> buildLocationDb(InputStream db) {
		Scanner sc = null;
		try {
			sc = new Scanner(db);
		} catch (Exception e) {
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

	private Map<String, List<String>> buildGroupDb(InputStream db) {
		Scanner sc = null;
		try {
			sc = new Scanner(db);
		} catch (Exception e) {
			System.err.println("Unable to open the groupDb");
			return null;
		}
		Map<String, List<String>> map = new HashMap<>();
		while (sc.hasNextLine()) {
			String[] nextEntry = sc.nextLine().split(LOCATION_DELIMITER);
			List<String> groupMembers = new LinkedList<String>();
			for (int i = 1; i < nextEntry.length; i++) {
				groupMembers.add(nextEntry[i]);
			}
			map.put(nextEntry[0], groupMembers);
		}
		sc.close();

		return map;
	}
}