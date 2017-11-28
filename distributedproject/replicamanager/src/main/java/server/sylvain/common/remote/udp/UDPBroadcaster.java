package server.sylvain.common.remote.udp;

import server.Branch;
import logging.BankLogger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.UUID;

public class UDPBroadcaster {

    private static DatagramSocket broadcastSocket;
    private static DatagramSocket listenSocket;

    private static String listenerId;

    public static void bootstrapBroadcaster() {
        try {
            //Initialize broadcasting socket
            broadcastSocket = new DatagramSocket();
            broadcastSocket.setBroadcast(true);

            //Initialize listening socket
            listenSocket = new DatagramSocket(null);
            listenSocket.setReuseAddress(true);
            listenSocket.bind(new InetSocketAddress("127.0.0.1", UDPHelper.CLIENT_LISTEN_PORT));

            //Initialize listenerId
            listenerId = UUID.randomUUID().toString();

            //Begin listening on client UDP port
            listen();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static void getAccountCount(String managerId) {
        BankLogger.logUserAction(managerId, "Probing all branch servers for account count");

        //Broadcast getAccountCount message to all server listeners
        byte[] buf = UDPHelper.buildMessage(listenerId, UDPHelper.OPERATION_GETACCOUNTCOUNT).getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, new InetSocketAddress("127.255.255.255", UDPHelper.SERVER_LISTEN_PORT));

        try {
            broadcastSocket.send(packet);
        } catch (Exception e) {
            BankLogger.logAction("Sending of getAccountCount message failed.");
        }
    }

    public static void transferFund(String managerId, String amount, String sourceCustomerId, String destCustomerId) {
        String message = null;
        if (managerId != null) {
            Branch branch = null;
            try {
                branch = Branch.valueOf(managerId.substring(0, 2));
            } catch (IllegalArgumentException e) {
                BankLogger.logUserAction(managerId, "managerId entered does not correspond to a valid branch");
                return;
            }

            message =UDPHelper.buildMessage(listenerId, UDPHelper.OPERATION_TRANSFER, branch.toString(), managerId, amount, sourceCustomerId, destCustomerId);
        } else {
            Branch branch = null;
            try {
                branch = Branch.valueOf(sourceCustomerId.substring(0, 2));
            } catch (IllegalArgumentException e) {
                BankLogger.logUserAction(sourceCustomerId, "sourceCustomerId entered does not correspond to a valid branch");
                return;
            }

            message = UDPHelper.buildMessage(listenerId, UDPHelper.OPERATION_TRANSFER, branch.toString(), null, amount, sourceCustomerId, destCustomerId);
        }

        byte[] buf = message.getBytes();

        DatagramPacket packet = new DatagramPacket(buf, buf.length, new InetSocketAddress("127.255.255.255", UDPHelper.SERVER_LISTEN_PORT));

        try {
            broadcastSocket.send(packet);
        } catch (Exception e) {
            BankLogger.logAction("Sending of transferFund message failed.");
        }
    }


    private static void listen() {
        new Thread(() -> {
            while (true) {
                try {
                    //Listen for responses from branch servers containing account count data
                    byte[] dataBuffer = new byte[256];
                    DatagramPacket requestPacket = new DatagramPacket(dataBuffer, dataBuffer.length);

                    listenSocket.receive(requestPacket);

                    //Properly convert byte buffer to String and log + print
                    String udpServerReply = new String(dataBuffer, 0, requestPacket.getLength());

                    String[] messageData = udpServerReply.split(UDPHelper.DELIM);

                    String recvListenerId = messageData[0];
                    String recvMessage = messageData[2];

                    if (recvListenerId.equals(listenerId)) {
                        BankLogger.logAction(recvMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
