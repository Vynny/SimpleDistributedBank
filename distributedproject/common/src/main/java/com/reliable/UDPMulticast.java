package com.reliable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.message.Message;
import com.message.MessageHeader;

public class UDPMulticast extends Thread {
	private MulticastSocket socket;
	private String groupId;
	private ConcurrentLinkedQueue<Message> messageBuffer;

	/**
	 * Origin holdBackQueues
	 */
	private Map<String, PriorityQueue<Message>> holdBackQueues;

	private long sendSequenceNumber = 0;

	/**
	 * Mapping of ClientId to integer for the last received sequence numbers for
	 * Ack.
	 */
	private Map<String, Long> lastReceivedSequenceNumbers;

	public UDPMulticast(ConcurrentLinkedQueue<Message> messageBuffer, String groupId, InetAddress group, int port)
			throws IOException {
		if (messageBuffer == null) {
			throw new IllegalArgumentException("No buffer");
		}

		try {
			socket = new MulticastSocket(port);
			socket.joinGroup(group);
		} catch (SocketException e) {
			System.err.println("Unable to start the UDP Server");
			throw e;
		}

		this.groupId = groupId;
		this.messageBuffer = messageBuffer;
		this.holdBackQueues = new HashMap<>();
	}

	public void multicast(Message message) {
		if (message == null) {
			throw new IllegalArgumentException("Message cannot be null");
		}

		if (!groupId.equals(message.getHeader().group)) {
			throw new IllegalArgumentException("The message is not destined to this group");
		}

		message.getHeader().sequenceNumber = getNextSequenceNumber();
		message.getHeader().acks = getPiggybackAcks();

		new Thread() {
			public void run() {
				DatagramPacket requestPacket = UDPHelper.buildDatagramPacket(message);

				DatagramSocket socket = null;
				try {
					socket = new DatagramSocket();
					socket.send(requestPacket);
				} catch (IOException e) {
					System.err.println("Unable to multicast request");
					e.printStackTrace();
					return;
				}
				socket.close();
			}
		}.start();
	}

	// public void totalOrderMulticast(Message message) {
	// if (message == null) {
	// throw new IllegalArgumentException("Message cannot be null");
	// }
	// message.getHeader().isTotallyOrdered = true;
	// message.getHeader().totalOrderSequenceId = getNextTotalOrderSequenceId();
	// multicast(message);
	// }

	public void run() {
		while (true) {
			byte[] requestBuffer = new byte[UDPHelper.MESSAGE_MAX_SIZE];
			DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length);
			try {
				socket.receive(requestPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Message requestMessage = UDPHelper.decodeDatagramPacket(requestPacket);
			if (requestMessage == null) {
				System.err.println("Received an invalid request Object");
				continue;
			}

			// Drop message if this isn't its destination
			if (!groupId.equals(requestMessage.getHeader().group)) {
				continue;
			} else {
				handleRequest(requestPacket.getSocketAddress(), requestMessage);
			}
		}
	}

	private void handleRequest(SocketAddress destinationAddress, Message requestMessage) {
		MessageHeader requestHeader = requestMessage.getHeader();

		long lastSequenceFromSender = getLastReceivedFrom(requestHeader.originId);
		if (requestHeader.sequenceNumber <= lastSequenceFromSender) {
			// Already delivered this message, Drop it
			return;
		} else {
			// Hold it
			addMessageToHoldBackQueue(requestMessage);
		}
		deliverFromHoldBackQueue(requestHeader.originId, lastSequenceFromSender);
		
		// Check if we're missing message from the group
		handleMissingMessageFromGroup(requestHeader);
	}

	private void deliverFromHoldBackQueue(String originId, long lastSequenceNumber) {
		PriorityQueue<Message> queue = holdBackQueues.get(originId);

		boolean hasDelivered = false;
		while (!queue.isEmpty() && queue.peek().getHeader().sequenceNumber == lastSequenceNumber + 1) {
			messageBuffer.add(queue.poll());
			hasDelivered = true;
			lastSequenceNumber++;
		}

		if (hasDelivered) {
			lastReceivedSequenceNumbers.put(originId, lastSequenceNumber);
		}
	}

	private void addMessageToHoldBackQueue(Message message) {
		PriorityQueue<Message> queue = holdBackQueues.get(message.getHeader().originId);
		if (queue == null) {
			queue = new PriorityQueue<>();
			// TODO comparator
		}
		queue.add(message);
	}

	private void handleMissingMessageFromGroup(MessageHeader requestHeader) {
		// TODO Auto-generated method stub

	}

	private long getLastReceivedFrom(String originId) {
		Long lastSeqInMap = lastReceivedSequenceNumbers.get(originId);

		if (lastSeqInMap == null) {
			return 0;
		} else {
			return lastSeqInMap;
		}
	}

	private Map<String, Long> getPiggybackAcks() {
		Map<String, Long> piggybackAcks = new TreeMap<>();
		Set<Entry<String, Long>> acksEntries = lastReceivedSequenceNumbers.entrySet();
		synchronized (lastReceivedSequenceNumbers) {
			for (Entry<String, Long> entry : acksEntries) {
				piggybackAcks.put(entry.getKey(), entry.getValue());
			}
		}
		return piggybackAcks;
	}

	private synchronized long getNextSequenceNumber() {
		return ++sendSequenceNumber;
	}
}