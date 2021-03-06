package server.radu;

import server.BranchServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RaduBranchImpl implements BranchServer {
    ServerDatabase ser;
    private int port;
    private int accIncr;
    NumberFormat formatter;

    public RaduBranchImpl(String branch) {
        ser = new ServerDatabase(branch);
        accIncr = 1;
        formatter = new DecimalFormat("$#0.00");
    }

    public void setDatabase(ServerDatabase db) {
        ser = db;
    }

    public void setUDPPort(int p) {
        port = p;
        System.out.println("setting new udp port " + p);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                UDPListener();
            }
        });
        t.start();
    }

    public String createAccountRecord(String managerID, String firstName, String lastName, String address
            , String phone, String branch) {
        String retMessage = "Failed to create a new account";
        if (!(branch.equals("QC") || branch.equals("BC") || branch.equals("MB") || branch.equals("NB"))) {
            return "You have entered an invalid branch";
        }
        List<String> s = new ArrayList<String>();
        String ID = branch + "C";
        int d = 1001 + accIncr;
        ++accIncr;
        ID += Integer.toString(d);
        /*Random rand = new Random();
        int digit1 = rand.nextInt(10);
        int digit2 = rand.nextInt(10);
        int digit3 = rand.nextInt(10);
        int digit4 = rand.nextInt(10);
        String s1 = Integer.toString(digit1);
        String s2 = Integer.toString(digit2);
        String s3 = Integer.toString(digit3);
        String s4 = Integer.toString(digit4);
        ID += s1 + s2 + s3 + s4;*/
        String balance = "0";
        s.add(ID);
        s.add(balance);
        s.add(branch);
        s.add(firstName);
        s.add(lastName);
        s.add(address);
        s.add(phone);
        boolean b = ser.addUserToDatabase(Character.toString(lastName.charAt(0)), s);
        if (b) {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
            ser.trackOperation("A new account with ID " + ID + " has been created. Date: " + timeStamp);
            retMessage = "Successfully created new customer and added to customer database. Customer id " + ID;

        }
        return retMessage;
    }

    public String editRecord(String managerID, String customerID, String fieldName, String newValue) {
        List<String> userInfo = ser.getClientList(customerID);
        String retMessage = "Failed to edit record";
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
        if (!ser.userIDExists(managerID))
            return "Could not find manager with id " + managerID;
        if (!ser.userIDExists(customerID))
            return "Could not find manager with id " + customerID;

        if (fieldName.equals("address")) {
            userInfo.set(5, newValue);
            ser.updateClientList(userInfo);
            retMessage = "Changed field + " + fieldName + " to newValue for customer with id " + customerID;
            ser.trackOperation("A new account with ID " + customerID + " has had its address edited. Date: " + timeStamp);
        } else if (fieldName.equals("phone")) {
            userInfo.set(6, newValue);
            ser.updateClientList(userInfo);
            retMessage = "Changed field + " + fieldName + " to newValue for customer with id " + customerID;
            ser.trackOperation("A new account with ID " + customerID + " has had its phone edited. Date: " + timeStamp);
        } else {
            return "Field name must be one of (address|phone)";
        }
           /*
           else if (fieldName.equals("branch") && (newValue.equals("QC") || newValue.equals("BC") ||
				   newValue.equals("MB") || newValue.equals("NB"))) {
			   userInfo.set(2,newValue);
			   ser.updateClientList(userInfo);
			   retMessage = "Updated the branch of the customer";
			   ser.trackOperation("A new account with ID " + customerID + " has had its branch edited. Date: " + timeStamp);
		   }*/
        return retMessage;
    }

    public String getAccountCount(String managerID) {
    	String ret = ser.branch + ": " + ser.getDBSize();
/*
        DatagramSocket aSocket = null;
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());

        String ret = "";
        try {
            aSocket = new DatagramSocket();
            String info = "count,5," + ser.branch + ",";
            byte[] m = info.getBytes();
            System.out.println("fs");
            InetAddress aHost = InetAddress.getLocalHost();
            int serverPort = 0;
            for (int k = 0; k < 4; ++k) {
                System.out.println("fss");
                if (k == 0) //qc
                    serverPort = 61728;
                else if (k == 1) //mb
                    serverPort = 61652;
                else if (k == 2) //bc
                    serverPort = 61499;
                else if (k == 3) //nb
                    serverPort = 61509;
                DatagramPacket request = new DatagramPacket(m, info.length(), aHost, serverPort);
                //    System.out.println("should send on port " + serverPort);
                aSocket.send(request);
                System.out.println("fsss");
                //      aSocket.close();
                byte[] buffer = new byte[1000];
                DatagramSocket s = new DatagramSocket(port + Character.getNumericValue(ser.branch.charAt(0)));
                DatagramPacket confirmation = new DatagramPacket(buffer, buffer.length);
                System.out.println("fssss");
                s.receive(confirmation);
                System.out.println("f");
                String conf = new String(confirmation.getData());
                String contents[] = conf.split(",");
                ret += contents[0] + ",";
                s.close();
            }
            aSocket.close();
            System.out.println(ret);
            ser.trackOperation("The manager with ID " + managerID + " has requested an account count accross all servers"
                    + ". Date: " + timeStamp);
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("General exception: " + e.getMessage());
        }
*/
        return ret;
    }

    public String transferFundManager(String managerID, String amount, String sourceCustomerID, String destinationCustomerID) {
        String ret = "";
        if (ser.userIDExists(managerID)) {

            ret = "The manager has initiated a transfer of funds of " + amount + " between " + sourceCustomerID + " and " + destinationCustomerID +
                    transferFund(sourceCustomerID, amount, destinationCustomerID);
            System.out.println(ret);
        } else {
            ret = "Manager ID doesn't exist.";
        }
        return ret;
    }

    //================================================================================
    // customer stuff
    // This method listens to UDP messages from other servers and sends them a reply
    public void UDPListener() {
        DatagramSocket aSocket = null;

        try {
            aSocket = new DatagramSocket(port);
            InetAddress aHost = InetAddress.getLocalHost();
            byte[] buffer = new byte[1000];
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(request);
            String r = new String(request.getData());
            String contents[] = r.split(",");
            String first = contents[0];
            String second = contents[1];

            String destBranch = contents[2];
            double amount = Double.parseDouble(second);
            String re = "Success";
            if (!first.equals("count") && ser.userIDExists(first))
                deposit(first, String.valueOf(amount));
            else if (!first.equals("count"))
                re = "Destination ID doesn't exist.,";
            else if (first.equals("count")) {
                re = ser.branch + ": " + Integer.toString(ser.getDBSize()) + ",";
            }
            byte[] m = re.getBytes();
            int sourcePort = 0;
            if (destBranch.equals("QC"))
                sourcePort = 61728;
            else if (destBranch.equals("MB"))
                sourcePort = 61652;
            else if (destBranch.equals("BC"))
                sourcePort = 61499;
            else if (destBranch.equals("NB"))
                sourcePort = 61509;
            sourcePort += Character.getNumericValue(destBranch.charAt(0));
            DatagramPacket reply = new DatagramPacket(m, re.length(), aHost, sourcePort);
            //    System.out.println("should send reply on port " + sourcePort);
            DatagramSocket s = new DatagramSocket();
            s.send(reply);
            s.close();
            aSocket.close();
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("General exception: " + e.getMessage());
        }
        UDPListener();
    }

    public String transferFund(String sourceCustomerID, String amount, String destinationCustomerID) {
        DatagramSocket aSocket = null;
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());

        String ret = "";


        String reply = withdraw(sourceCustomerID, amount);
        if (reply.equals("User ID doesn't exist.")) {

            return "User ID doesn't exist.";
        }
        List<String> userInfo = ser.getClientList(sourceCustomerID);
        if (reply.equals("Withdraw succesful")) {
            try {
                aSocket = new DatagramSocket();
                String info = destinationCustomerID + "," + amount + "," + ser.branch + ",";
                String destBranch = destinationCustomerID.substring(0, 2);

                byte[] m = info.getBytes();
                InetAddress aHost = InetAddress.getLocalHost();
                int serverPort = 0;
                if (destBranch.equals("QC"))
                    serverPort = 61728;
                else if (destBranch.equals("MB"))
                    serverPort = 61652;
                else if (destBranch.equals("BC"))
                    serverPort = 61499;
                else if (destBranch.equals("NB"))
                    serverPort = 61509;
                DatagramPacket request = new DatagramPacket(m, info.length(), aHost, serverPort);
                //    System.out.println("should send on port " + serverPort);
                aSocket.send(request);
                aSocket.close();
                byte[] buffer = new byte[1000];
                DatagramSocket s = new DatagramSocket(port + Character.getNumericValue(ser.branch.charAt(0)));
                DatagramPacket confirmation = new DatagramPacket(buffer, buffer.length);
                s.receive(confirmation);
                String conf = new String(confirmation.getData());
                String contents[] = conf.split(",");
                conf = contents[0];
                s.close();
                if (conf.contains("Success")) {
                    ret = "Transfer was succesful on both sides.";
                    System.out.println(ret);
                    ser.trackOperation("The client with ID " + sourceCustomerID + ", known as " + userInfo.get(3) + " " + userInfo.get(4) +
                            ", has transfered an amount of " + amount + " into the account " + destinationCustomerID
                            + ". Date: " + timeStamp);
                } else {
                    ret = "Transfer has failed at the destination." + "Reason: " + conf + ". Returning your money...";
                    System.out.println(ret);
                    deposit(sourceCustomerID, amount);

                    ser.trackOperation("The client with ID " + sourceCustomerID + ", known as " + userInfo.get(3) + " " + userInfo.get(4) +
                            ", has failed making a transfer of " + amount + " into the account " + destinationCustomerID
                            + ". Date: " + timeStamp);

                }
            } catch (SocketException e) {
                System.out.println("Socket: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IO: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("General exception: " + e.getMessage());
            }
        } else {
            System.out.println("Not enough funds");
            ret = "Not enough funds.";
            ser.trackOperation("The client with ID " + sourceCustomerID + ", known as " + userInfo.get(3) + " " + userInfo.get(4) +
                    ", has failed making a transfer of " + amount + " into the account " + destinationCustomerID
                    + "Reason: not enough funds. Date: " + timeStamp);
        }
        return ret;
    }

    public String deposit(String customerID, String amount) {
        if (Double.parseDouble(amount) <= 0)
            return "Cannot deposit a negative amount or $0.";
        if (ser.userIDExists(customerID)) {
            List<String> userInfo = ser.getClientList(customerID);
            System.out.println(userInfo.get(1));
            double currentBalance = Double.parseDouble(userInfo.get(1));
            currentBalance += Double.parseDouble(amount);
            userInfo.set(1, Double.toString(currentBalance));
            boolean op = ser.updateClientList(userInfo);
            if (op) {
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
                ser.trackOperation("The client with ID " + customerID + ", known as " + userInfo.get(3) + " " + userInfo.get(4) +
                        ", has made a deposit of " + amount + " into their account. Date: " + timeStamp);

                return "Deposited $" + amount + " into your account (" + customerID + ")."
                        + " New Balance: " + formatter.format(currentBalance);

            }
			  /* else {
				   String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
				   ser.trackOperation("The client with ID " + customerID + ", known as " + userInfo.get(3) + " " + userInfo.get(4) + 
						   ", has failed to make a deposit of " + amount + " into their account. Date: " + timeStamp);
				   return "Deposit failed";
			   }*/
        }
        return "Could not find customer with id " + customerID;

    }

    public String withdraw(String customerID, String amount) {
        if (Double.parseDouble(amount) <= 0)
            return "Cannot withdraw a negative amount or $0.";

        if (ser.userIDExists(customerID)) {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
            List<String> userInfo = ser.getClientList(customerID);
            double currentBalance = Double.parseDouble(userInfo.get(1));
            if (currentBalance < Double.parseDouble(amount)) {

                ser.trackOperation("The client with ID " + customerID + ", known as " + userInfo.get(3) + " " + userInfo.get(4) +
                        ", has failed to withdraw " + amount + " from their account because they don't have enough funds. Date: " + timeStamp);
                return "You do not have enough funds to withdraw " + formatter.format(Double.parseDouble(amount)) + ". "
                        + "Your balance is " + formatter.format(currentBalance);

            } else {
                currentBalance -= Double.parseDouble(amount);
                userInfo.set(1, Double.toString(currentBalance));
                boolean op = ser.updateClientList(userInfo);
                if (op) {
                    ser.trackOperation("The client with ID " + customerID + ", known as " + userInfo.get(3) + " " + userInfo.get(4) +
                            ", has made a withdrawl of " + amount + " from their account. Date: " + timeStamp);
                    return "Withdrew " + formatter.format(Double.parseDouble(amount)) + " from your account"
                            + " (" + customerID + "). New Balance: " + formatter.format(currentBalance);
                }
				   /*else {
					   ser.trackOperation("The client with ID " + customerID + ", known as " + userInfo.get(3) + " " + userInfo.get(4) + 
							   ", has failed to withdraw " + amount + " from their account. Date: " + timeStamp);
					   return "";
				   }*/
            }
        }
        return "Could not find customer with id " + customerID;
    }

    public String getBalance(String customerID) {
        if (ser.userIDExists(customerID)) {
            List<String> userInfo = ser.getClientList(customerID);

            double currentBalance = Double.parseDouble(userInfo.get(1));
            System.out.println("current balance: " + currentBalance);
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
            ser.trackOperation("The client with ID " + customerID + ", known as " + userInfo.get(3) + " " + userInfo.get(4) +
                    ", has checked their account. Date: " + timeStamp);

            return "Account Balance for Customer " + customerID + ": " + formatter.format(currentBalance);
        } else {
            return "Could not find customer with id " + customerID;
        }
    }

    @Override
    public String getAccountCount() {
        return ser.branch + ": " + ser.getDBSize();
    }

    @Override
    public List<String> dumpDatabase() {
        return null;
    }

    @Override
    public void restoreDatabase(List<String> databaseDump) {

    }
}
