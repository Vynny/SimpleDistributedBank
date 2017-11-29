package com.reliable;

import com.message.Message;
import com.message.MessageHeader;
import com.message.NACKBody;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;

public class UDPMulticast extends Thread {
    private static final Comparator<Message> MESSAGE_SEQUENCE_COMPARATOR = new Comparator<Message>() {
        @Override
        public int compare(Message m1, Message m2) {
            MessageHeader m1Header = m1.getHeader();
            MessageHeader m2Header = m2.getHeader();
            if (m1Header.sequenceNumber < m2Header.sequenceNumber) {
                return -1;
            } else if (m1Header.sequenceNumber == m2Header.sequenceNumber) {
                return 0;
            } else {
                return 1;
            }
        }
    };

    private MulticastSocket socket;
    private String groupId;
    private String clientId;
    private InetAddress group;
    private int groupPort;
    private BlockingQueue<Message> messageBuffer;

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

    public UDPMulticast(BlockingQueue<Message> messageBuffer, String clientId, String groupId,
                        InetAddress group, int port) throws IOException {
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

        this.clientId = clientId;
        this.groupId = groupId;
        this.groupPort = port;
        this.group = group;
        this.messageBuffer = messageBuffer;
        this.holdBackQueues = new HashMap<>();
        this.lastReceivedSequenceNumbers = Collections.synchronizedMap(new HashMap<>());
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

            // Drop message if this isn't its destination or its our own message
            if (!groupId.equals(requestMessage.getHeader().group)
                    || clientId.equals(requestMessage.getHeader().senderId)) {
                continue;
            } else {
                handleRequest(requestMessage);
            }
        }
    }

    private void handleRequest(Message requestMessage) {
        MessageHeader requestHeader = requestMessage.getHeader();

        long lastSequenceFromSender = getLastReceivedFrom(requestHeader.senderId);
        if (requestHeader.sequenceNumber <= lastSequenceFromSender) {
            // Already delivered this message, Drop it
            return;
        } else {
            // Hold it
            addMessageToHoldBackQueue(requestMessage);
        }

        deliverFromHoldBackQueue(requestHeader.senderId, lastSequenceFromSender);

        // Check if we're missing message from the group
        handleMissingMessageFromGroup(requestHeader);
    }

    private void deliverFromHoldBackQueue(String senderId, long lastSequenceNumber) {
        PriorityQueue<Message> queue = holdBackQueues.get(senderId);

        boolean hasDelivered = false;
        while (!queue.isEmpty()) {
            long nextSequenceNumberInQueue = queue.peek().getHeader().sequenceNumber;
            if (nextSequenceNumberInQueue > lastSequenceNumber + 1) {
                // Missing messages
                requestMissingMessages(senderId, lastSequenceNumber + 1, nextSequenceNumberInQueue - 1);
                break;
            } else {
                messageBuffer.add(queue.poll());
                hasDelivered = true;
                lastSequenceNumber++;
            }
        }

        if (hasDelivered) {
            lastReceivedSequenceNumbers.put(senderId, lastSequenceNumber);

        }
    }

    private void requestMissingMessages(String senderId, long first, long last) {
        new Thread() {
            public void run() {
                Message message = buildNackMessage(senderId, first, last);
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

    private Message buildNackMessage(String senderId, long first, long last) {
        MessageHeader header = new MessageHeader();
        header.senderId = senderId;
        header.messageId = "NACK";
        header.destinationId = groupId;
        header.destinationAddress = group.getHostAddress();
        header.destinationPort = groupPort;

        NACKBody body = new NACKBody(senderId, first, last);
        Message message = new Message(UDPHelper.NACK_ACTION, header, body);

        return message;
    }

    private void addMessageToHoldBackQueue(Message message) {
        PriorityQueue<Message> queue = holdBackQueues.get(message.getHeader().senderId);
        if (queue == null) {
            queue = new PriorityQueue<Message>(MESSAGE_SEQUENCE_COMPARATOR);
            holdBackQueues.put(message.getHeader().senderId, queue);
        }
        if (!queue.contains(message)) {
            queue.add(message);
        }
    }

    private void handleMissingMessageFromGroup(MessageHeader requestHeader) {
        // TODO If necessary, only if members other than sequencer need to multicast

    }

    private long getLastReceivedFrom(String senderId) {
        Long lastSeqInMap = lastReceivedSequenceNumbers.get(senderId);

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
