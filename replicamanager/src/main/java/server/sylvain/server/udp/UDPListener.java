package server.sylvain.server.udp;


import enums.Branch;
import logging.BankLogger;
import server.sylvain.common.models.user.CustomerUser;
import server.sylvain.common.models.user.ManagerUser;
import server.sylvain.common.remote.udp.UDPHelper;
import server.sylvain.server.database.CustomerDatabase;
import server.sylvain.server.database.ManagerDatabase;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class UDPListener {

    private Branch branch;
    private DatagramSocket listenSocket;

    public UDPListener(Branch branch) {
        this.branch = branch;
        try {
            //Initialize listening socket
            this.listenSocket = new DatagramSocket(null);
            this.listenSocket.setReuseAddress(true);
            this.listenSocket.bind(new InetSocketAddress("127.0.0.1", UDPHelper.SERVER_LISTEN_PORT));
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        new Thread(() -> {
            while (true) {
                try {
                    //Initialize buffer
                    byte[] dataBuffer = new byte[256];
                    DatagramPacket requestPacket = new DatagramPacket(dataBuffer, dataBuffer.length);

                    //Listen for requests from manager client
                    listenSocket.receive(requestPacket);

                    //Properly convert byte buffer to String
                    String receivedMessage = new String(dataBuffer, 0, requestPacket.getLength());
                    BankLogger.logAction("Received UDP Message: " + receivedMessage);

                    String[] messageData = receivedMessage.split(UDPHelper.DELIM);
                    for (String s : messageData) {
                        BankLogger.logAction("\tMESSAGE DATA: " + s);
                    }

                    String listenerId = messageData[0];

                    if (messageData[1].equals(UDPHelper.OPERATION_GETACCOUNTCOUNT)) {
                        getAccountCount(listenerId);
                    }

                    if (messageData[1].equals(UDPHelper.OPERATION_TRANSFER)) {
                        transferFunds(listenerId, messageData);
                    }

                    if (messageData[1].equals(UDPHelper.OPERATION_TRANSFER_RELAY)) {
                        transferRelay(listenerId, messageData);
                    }

                    if (messageData[1].equals(UDPHelper.OPERATION_TRANSFER_RELAY_RESPONSE)) {
                        transferRelayResponse(messageData);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getAccountCount(String listenerId) throws IOException {
        BankLogger.logAction("Received getAccountCount request from manager client");

        //Send response to manager client containing account count on this branch's server
        String message = branch.toString() + " " + CustomerDatabase.getInstance().getCustomerCount();
        sendToClient(listenerId, message);
    }

    private void transferFunds(String listenerId, String[] messageData) throws IOException {
        BankLogger.logAction("Received manager transferFund request from manager client");

        String branchGiven = messageData[2];
        if (branchGiven.equals(branch.toString())) {
            String managerId = messageData[3];
            String amount = messageData[4];
            String sourceCustomer = messageData[5];
            String destCustomer = messageData[6];

            //Get DB References
            ManagerDatabase managerDatabase = ManagerDatabase.getInstance();
            CustomerDatabase customerDatabase = CustomerDatabase.getInstance();

            //Ensure manager exists
            if (!managerId.equals("null")) {
                ManagerUser managerUser = managerDatabase.getManager(managerId);
                if (managerUser == null) {
                    sendToClient(listenerId, "Manager with id " + managerId + " does not exist");
                    return;
                }
            }

            //Ensure source customer exists
            CustomerUser customerUser = customerDatabase.getCustomer(sourceCustomer);
            if (customerUser == null) {
                sendToClient(listenerId, "Source customer with id " + sourceCustomer + " does not exist");
                return;
            }

            //Ensure source customer has balance
            if (!customerUser.getBankAccount().canWithdraw(new BigDecimal(amount))) {
                sendToClient(listenerId, "Source customer with id " + sourceCustomer + " does not have enough balance to transfer $" + amount);
                return;
            }

            //Get the dest customer's branch
            Branch destCustomerBranch = CustomerUser.extractBranch(destCustomer);
            if (destCustomerBranch == branch) {
                System.out.println("BRANCH IS LOCAL");
                //The destination customer is local
                CustomerUser customerDest = customerDatabase.getCustomer(destCustomer);

                if (customerDest == null) {
                    sendToClient(listenerId, "Destination customer with id " + destCustomer + " does not exist");
                    return;
                }

                BigDecimal transferAmount = new BigDecimal(amount);

                //Withdraw
                customerUser.getBankAccount().withdraw(transferAmount);

                //Deposit
                customerDest.getBankAccount().deposit(transferAmount);

                sendToClient(listenerId, "Transferred $" + amount + " from " + sourceCustomer + " to " + destCustomer);
            } else {
                //The destination customer is on another server
                sendToServer(listenerId, UDPHelper.OPERATION_TRANSFER_RELAY, destCustomerBranch.toString(), amount, sourceCustomer, destCustomer);
            }
        }
    }

    private void transferRelay(String listenerId, String[] messageData) throws IOException {
        BankLogger.logAction("Received relay transferFund request from bank server");

        String branchGiven = messageData[2];
        if (branchGiven.equals(branch.toString())) {
            String amount = messageData[3];
            String sourceCustomer = messageData[4];
            String destCustomer = messageData[5];

            //Get DB References
            ManagerDatabase managerDatabase = ManagerDatabase.getInstance();
            CustomerDatabase customerDatabase = CustomerDatabase.getInstance();

            //Check dest customer exists
            CustomerUser customerUser = customerDatabase.getCustomer(destCustomer);
            if (customerUser == null) {
                sendToClient(listenerId, "Destination customer with id " + destCustomer + " does not exist");
                return;
            }

            //Since dest customer exists, we credit the amount, and tell the source branch to debit the amount from source
            customerUser.getBankAccount().deposit(new BigDecimal(amount));
            sendToServer(null, UDPHelper.OPERATION_TRANSFER_RELAY_RESPONSE, CustomerUser.extractBranch(sourceCustomer).toString(), sourceCustomer, amount);
            sendToClient(listenerId, "Transferred $" + amount + " from " + sourceCustomer + " to " + destCustomer);
        }
    }

    private void transferRelayResponse(String[] messageData) throws IOException {
        BankLogger.logAction("Received relay response transferFund request from bank server");

        String branchGiven = messageData[2];
        if (branchGiven.equals(branch.toString())) {
            String sourceCustomer = messageData[3];
            String amount = messageData[4];

            //Get DB References
            CustomerDatabase customerDatabase = CustomerDatabase.getInstance();

            CustomerUser customerUser = customerDatabase.getCustomer(sourceCustomer);
            BankLogger.logAction("Debiting customer with id " + sourceCustomer + " for amount $" + amount);
            customerUser.getBankAccount().withdraw(new BigDecimal(amount));
        }
    }

    private void sendToClient(String listenerId, String message) throws IOException {
        DatagramSocket sendSocket = new DatagramSocket();

        BankLogger.logAction("~Sending UDP Response to client~");
        BankLogger.logAction(message);

        byte[] udpMessage = UDPHelper.buildMessage(listenerId, null, message).getBytes();
        DatagramPacket responsePacket = new DatagramPacket(udpMessage, udpMessage.length, new InetSocketAddress("127.255.255.255", UDPHelper.CLIENT_LISTEN_PORT));
        sendSocket.send(responsePacket);
    }

    private void sendToServer(String listenerId, String operation, String... args) throws IOException {
        DatagramSocket sendSocket = new DatagramSocket();

        BankLogger.logAction("~Forwarding operation " + operation + " to branch server~");

        byte[] udpMessage = UDPHelper.buildMessage(listenerId, operation, args).getBytes();
        DatagramPacket responsePacket = new DatagramPacket(udpMessage, udpMessage.length, new InetSocketAddress("127.255.255.255", UDPHelper.SERVER_LISTEN_PORT));
        sendSocket.send(responsePacket);
    }
}
