package client;

import fe.corba.FrontEnd;
import fe.corba.FrontEndHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class Client {
    static FrontEnd fe;

    public static void main(String args[]) {
        try {

            InputStreamReader is = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(is);

            System.out.println("Enter your ID:");
            String ID = br.readLine(); // ID has the following format XXYZZZZ, where XX is the branch, Y the type of client
            // and ZZZZ a random number
            char Y = ID.charAt(2);
            String branch = ID.substring(0, 2);

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBInitialPort", "900");
            ORB orb = ORB.init(args, props);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            //create the user file
            File f = new File(ID + ".txt");
            if (!f.exists() && !f.isDirectory()) {
                PrintWriter file = new PrintWriter(ID + ".txt", "UTF-8");
                file.close();
            }
            // this is to determine to which server to connect
            String type = "";
            if (Y == 'M') {
                type = "manager";
            } else if (Y == 'C') {
                type = "customer";
            }
            String name = "FrontEnd";
            fe = FrontEndHelper.narrow(ncRef.resolve_str(name));

            System.out.println("Obtained a handle on server object: " + fe);
            while (true)
                manageClient(type, ID);


        } // end try
        catch (Exception e) {
            System.out.println("Exception in class Client: " + e.getMessage());
        }
    } //end main

    public static void manageClient(String type, String ID) throws Exception {
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);
        if (type == "customer") {
            System.out.println("As a customer, you can do the following operations:"
                    + "\n 1. Deposit a certain amount."
                    + "\n 2. Withdraw a certain amount."
                    + "\n 3. View your current balance."
                    + "\n 4. Transfer funds to another account."
                    + "\n Please choose which option you wish to proceed by entering the corresponding number.");
            int choice = Integer.parseInt(br.readLine());
            if (choice == 1) {
                System.out.println("Please enter the amount you wish to deposit.");
                int amount = Integer.parseInt(br.readLine());
                String serverResponse = fe.deposit(ID, amount);
                System.out.println(serverResponse);
                String message = "The user has made a deposit. The response from the server was " + serverResponse
                        + System.lineSeparator();
                Files.write(Paths.get(ID + ".txt"), message.getBytes(), StandardOpenOption.APPEND);
            } else if (choice == 2) {
                System.out.println("Please enter the amount you wish to withdraw.");
                int amount = Integer.parseInt(br.readLine());
                String serverResponse = fe.withdraw(ID, amount);
                System.out.println(serverResponse);
                String message = "The user has made a withdrawl. The response from the server was " + serverResponse
                        + System.lineSeparator();
                Files.write(Paths.get(ID + ".txt"), message.getBytes(), StandardOpenOption.APPEND);
            } else if (choice == 3) {
                int balance = fe.getBalance(ID);
                System.out.println("You have " + balance + " left in your account.");
                String message = "The user has checked their balance. The response from the server was " + balance +
                        System.lineSeparator();
                Files.write(Paths.get(ID + ".txt"), message.getBytes(), StandardOpenOption.APPEND);
            } else if (choice == 4) {
                int balance = fe.getBalance(ID);
                System.out.println("You have " + balance + " left in your account. Enter the amount you wish to transfer"
                        + " as well as the customer ID to which you wish to transfer.");
                int amount = Integer.parseInt(br.readLine());
                String destinationID = br.readLine();

                String serverResponse = "";
                serverResponse = fe.transferFund(ID, amount, destinationID);


                String message = "The user has tried transfering an amount. The response from the server was " + serverResponse +
                        System.lineSeparator();
                System.out.println(message);
                Files.write(Paths.get(ID + ".txt"), message.getBytes(), StandardOpenOption.APPEND);
            }

        } else if (type == "manager") {
            System.out.println("As a manager, you can do the following operations:"
                    + "\n 1. Deposit a certain amount."
                    + "\n 2. Withdraw a certain amount."
                    + "\n 3. View your current balance."
                    + "\n 4. Create an account record."
                    + "\n 5. Edit an existing account record."
                    + "\n 6. Transfer funds between 2 customers."
                    + "\n 7. Get account count accross all servers."
                    + "\n Please choose which option you wish to proceed by entering the corresponding number.");
            int choice = Integer.parseInt(br.readLine());
            if (choice == 1 || choice == 2 || choice == 3) {

                if (choice == 1) {
                    System.out.println("Please enter the amount you wish to deposit.");
                    int amount = Integer.parseInt(br.readLine());
                    String serverResponse = fe.deposit(ID, amount);
                    String message = "The user has made a deposit. The response from the server was " + serverResponse +
                            System.lineSeparator();
                    Files.write(Paths.get(ID + ".txt"), message.getBytes(), StandardOpenOption.APPEND);
                    System.out.println(serverResponse);
                } else if (choice == 2) {
                    System.out.println("Please enter the amount you wish to withdraw.");
                    int amount = Integer.parseInt(br.readLine());
                    String serverResponse = fe.withdraw(ID, amount);
                    System.out.println(serverResponse);
                    String message = "The user has made a withdrawl. The response from the server was " + serverResponse +
                            System.lineSeparator();
                    Files.write(Paths.get(ID + ".txt"), message.getBytes(), StandardOpenOption.APPEND);
                } else if (choice == 3) {
                    int balance = fe.getBalance(ID);
                    System.out.println("You have " + balance + " left in your account.");
                    String message = "The user has checked their balance. The response from the server was " + balance +
                            System.lineSeparator();
                    Files.write(Paths.get(ID + ".txt"), message.getBytes(), StandardOpenOption.APPEND);

                }
            } else if (choice == 4 || choice == 5 || choice == 6 || choice == 7) {
                if (choice == 4) {
                    System.out.println("Please enter the first name, last name, address, phone number and branch in that order"
                            + "to create a new account.");
                    String serverResponse = fe.createAccountRecord(ID, br.readLine(), br.readLine(),
                            br.readLine(), br.readLine(), br.readLine());
                    System.out.println(serverResponse);
                    String message = "The user has tried creating an account. The response from the server was " + serverResponse +
                            System.lineSeparator();
                    Files.write(Paths.get(ID + ".txt"), message.getBytes(), StandardOpenOption.APPEND);

                } else if (choice == 5) {
                    System.out.println("Please enter the customer ID of the client you wish to change, the field(address,"
                            + " phone, branch) you want"
                            + "to change and the new value of said field in that order.");
                    String serverResponse = fe.editRecord(ID, br.readLine(), br.readLine(), br.readLine());
                    System.out.println(serverResponse);
                    String message = "The user has tried editing an account. The response from the server was " + serverResponse +
                            System.lineSeparator();
                    Files.write(Paths.get(ID + ".txt"), message.getBytes(), StandardOpenOption.APPEND);
                } else if (choice == 6) {
                    //QCC7383
                    System.out.println("Enter the amount you wish to transfer"
                            + " as well as the source customer ID and destination customer ID to which you wish to transfer.");
                    int amount = Integer.parseInt(br.readLine());
                    String sourceID = br.readLine();
                    String destID = br.readLine();
                    String serverResponse = "";
                    serverResponse = fe.transferFundManager(ID, amount, sourceID, destID);
                    String message = "The manager " + ID + " has tried transfering an amount. The response from the server was: " + serverResponse +
                            System.lineSeparator();
                    System.out.println(message);
                    Files.write(Paths.get(ID + ".txt"), message.getBytes(), StandardOpenOption.APPEND);
                } else if (choice == 7) {
                    String serverResponse = "";
                    serverResponse = fe.getAccountCount(ID);
                    String message = "The manager " + ID + " has tried transfering an amount. The response from the server was: " + serverResponse +
                            System.lineSeparator();
                    System.out.println(message);
                    Files.write(Paths.get(ID + ".txt"), message.getBytes(), StandardOpenOption.APPEND);
                }
            }
        }
    }
}
