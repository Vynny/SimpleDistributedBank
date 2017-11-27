package com.reliable;

import com.message.Message;
import com.message.MessageHeader;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPClient extends Thread {
    private DatagramSocket socket;
    private DatagramPacket request;
    private Message initialMessage;

    public UDPClient(Message message) throws SocketException {
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
        } catch (SocketException e) {
            System.err.println("Unable to create a socket");
            e.printStackTrace();
            throw e;
        }

        this.initialMessage = message;
        this.request = UDPHelper.buildDatagramPacket(message);
    }

    public void run() {
        int retryCount = 0;
        boolean transmissionSuccess = false;
        while (!transmissionSuccess) {
            if (retryCount++ >= UDPHelper.RETRY_COUNT) {
                System.err.println("Retry count exceeded to complete transfer");
                return;
            }
            try {
                socket.send(request);
                byte[] replyBuffer = new byte[UDPHelper.MESSAGE_MAX_SIZE];
                DatagramPacket replyPacket = new DatagramPacket(replyBuffer, replyBuffer.length);
                socket.receive(replyPacket);

                Message replyMessage = UDPHelper.decodeDatagramPacket(replyPacket);
                MessageHeader replyHeader = replyMessage.getHeader();

                if (replyHeader.isAck && replyHeader.messageId.equals(initialMessage.getHeader().messageId)) {
                    DatagramPacket ackPacket = UDPHelper.buildAckDatagramPacket(replyMessage,
                            replyPacket.getSocketAddress());
                    socket.send(ackPacket);
                    transmissionSuccess = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
