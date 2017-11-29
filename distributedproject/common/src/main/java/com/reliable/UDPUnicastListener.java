package com.reliable;

import com.message.Message;
import com.message.MessageHeader;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class UDPUnicastListener extends Thread {
    private DatagramSocket socket;

    /**
     * MessageIds already processed and delivered.
     */
    private Set<String> replyHistory = new HashSet<>();

    private BlockingQueue<Message> messageBuffer;

    private String id;

    public UDPUnicastListener(BlockingQueue<Message> messageBuffer, String id, int port)
            throws SocketException {
        if (messageBuffer == null) {
            throw new IllegalArgumentException("No buffer");
        }

        try {
            // if (address == null) {
            if (port >= 0) {
                socket = new DatagramSocket(port);
                // socket = new DatagramSocket(address);
            } else {
                socket = new DatagramSocket();
            }
        } catch (SocketException e) {
            System.err.println("Unable to start the UDP Server");
            throw e;
        }
        this.id = id;
        this.messageBuffer = messageBuffer;
    }

    public UDPUnicastListener(BlockingQueue<Message> messageBuffer, String id) throws SocketException {
        this(messageBuffer, id, -1);
        // this(messageBuffer, id, null);
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

            // Drop message if this isn't its destination
            if (!id.equals(requestMessage.getHeader().destinationId)) {
                continue;
            } else {
                handleRequest(requestPacket.getSocketAddress(), requestMessage);
            }
        }
    }

    private void handleRequest(SocketAddress destinationAddress, Message requestMessage) {
        MessageHeader requestHeader = requestMessage.getHeader();

        // Only add to the message Queue if it is a new request
        if (!replyHistory.contains(requestHeader.messageId)) {
            messageBuffer.add(requestMessage);
        }

        DatagramPacket replyPacket = UDPHelper.buildAckDatagramPacket(requestMessage, destinationAddress);
        sendReply(requestHeader.messageId, replyPacket);
    }

    private void sendReply(String messageId, DatagramPacket replyPacket) {
        new Thread() {
            public void run() {
                int retryCount = 0;
                while (true) {
                    if (retryCount++ >= UDPHelper.RETRY_COUNT) {
                        System.err.println("Retry count exceeded to complete transfer");
                        return;
                    }

					/*
                     * Send Reply
					 */
                    DatagramSocket socket = null;
                    try {
                        socket = new DatagramSocket();
                        socket.send(replyPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                    replyHistory.add(messageId);

					/*
					 * Receive Ack
					 */
                    byte[] ackBuffer = new byte[UDPHelper.MESSAGE_MAX_SIZE];
                    DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);
                    try {
                        socket.receive(ackPacket);
                    } catch (IOException e) {
                        System.err.println("Unable to receive ack");
                        e.printStackTrace();
                        continue;
                    }

                    socket.close();

                    Message ackMessage = UDPHelper.decodeDatagramPacket(ackPacket);
                    MessageHeader ackHeader = ackMessage.getHeader();

                    if (ackHeader.isAck && ackHeader.messageId.equals(messageId)) {
                        replyHistory.remove(messageId);
                        break;
                    } else {
                        continue;
                    }
                }
            }
        }.start();
    }

    public int getSocketPort() {
        return socket.getLocalPort();
    }

    public String getSocketAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            System.err.println("Unable to retrieve local address");
            e.printStackTrace();
            return null;
        }
    }

}
