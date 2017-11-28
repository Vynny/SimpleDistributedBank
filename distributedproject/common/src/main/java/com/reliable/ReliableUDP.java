package com.reliable;

import com.message.Message;
import com.message.MessageBody;
import com.message.MessageHeader;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ReliableUDP {
	private static final String LOCATION_DB_FILENAME = "location.csv";
	private static final String GROUPS_DB_FILENAME = "groups.csv";
	private static final String CSV_DELIMITER = ";";

	private static long nextMessageSequenceNumber = 0;

	private final String id;

	private UDPUnicastListener listener;
	private UDPMulticast udpMulticast;
	private Map<String, InetSocketAddress> locationDb;
	private String groupId;

	private ConcurrentLinkedQueue<Message> messageBuffer;

	public ReliableUDP(String id) throws IOException {
		this.id = id;
		messageBuffer = new ConcurrentLinkedQueue<Message>();

		groupId = findGroupMembership(ClassLoader.getSystemResourceAsStream(GROUPS_DB_FILENAME));
		locationDb = buildLocationDb(ClassLoader.getSystemResourceAsStream(LOCATION_DB_FILENAME));

		if (UDPHelper.ANONYMOUS_ID.equals(id)) {
			listener = new UDPUnicastListener(messageBuffer, id);
		} else {
			listener = new UDPUnicastListener(messageBuffer, id, getLocation(id).getPort());
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

	public void startUDPMulticast() throws IOException {
		if (groupId == null) {
			throw new IOException("This module does not belong to a group");
		}
		InetSocketAddress location = locationDb.get(groupId);
		udpMulticast = new UDPMulticast(messageBuffer, id, groupId, location.getAddress(), location.getPort());
		udpMulticast.start();
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
		UDPUnicastClient client = new UDPUnicastClient(message);

		client.start();
	}

	public void reply(MessageHeader originHeader, MessageBody replyBody, String action) throws SocketException {
		Message message = buildReplyMessage(originHeader, action, replyBody);
		UDPUnicastClient client = new UDPUnicastClient(message);

		client.start();
	}

	/**
	 * Forwards an existing message to a new destination.
	 *
	 * @param message
	 * @param destinationId
	 */
	public void send(Message message, String destinationId) {
		// TODO method stub
		throw new UnsupportedOperationException("Method Stub");
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
		return messageBuffer.poll();
	}

	/**
	 * Sends a new message.
	 *
	 * @param body
	 * @param action
	 * @param customId
	 */
	public void multicast(MessageBody body, String action, String customId) {
		if (udpMulticast == null) {
			throw new UnsupportedOperationException("UDP Multicast isn't started");
		}

		udpMulticast.multicast(buildMulticastMessage(action, body, customId));
	}

	/**
	 * Forwards an existing message to a new destination.
	 *
	 * @param message
	 */
	public void multicast(Message message) {
		if (udpMulticast == null) {
			throw new UnsupportedOperationException("UDP Multicast isn't started");
		}

		setForwardMulticastHeader(message.getHeader(), groupId);
		udpMulticast.multicast(message);
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

	private Message buildMulticastMessage(String action, MessageBody body, String customId) {
		MessageHeader header = buildHeader(groupId, customId);
		header.group = groupId;
		Message message = new Message(action, header, body);

		return message;
	}

	private MessageHeader buildHeader(String destinationId, String customId) {
		InetSocketAddress destinationLocation = getLocation(destinationId);
		MessageHeader header = new MessageHeader();
		header.messageId = getNextMessageId();
		header.customId = customId;

		header.senderId = id;
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

		header.senderId = id;
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

	private void setForwardMulticastHeader(MessageHeader header, String destinationId) {
		InetSocketAddress destinationLocation = getLocation(destinationId);
		header.group = groupId;
		header.senderId = id;
		header.destinationId = destinationId;
		header.destinationAddress = destinationLocation.getAddress().getHostAddress();
		header.destinationPort = destinationLocation.getPort();
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
			String[] nextEntry = sc.nextLine().split(CSV_DELIMITER);
			map.put(nextEntry[0], new InetSocketAddress(nextEntry[1], Integer.parseInt(nextEntry[2])));
		}
		sc.close();

		return map;
	}

	private String findGroupMembership(InputStream db) {
		Scanner sc = null;
		try {
			sc = new Scanner(db);
		} catch (Exception e) {
			System.err.println("Unable to open the groupDb");
			return null;
		}
		String group = null;
		boolean foundGroup = false;
		while (sc.hasNextLine() && !foundGroup) {
			String[] nextEntry = sc.nextLine().split(CSV_DELIMITER);
			group = nextEntry[0];

			for (int i = 1; i < nextEntry.length; i++) {
				if (id.equals(nextEntry[i])) {
					foundGroup = true;
					break;
				}
			}
		}
		sc.close();

		return group;
	}
}
