package fe;

import com.message.Message;
import com.reliable.ReliableUDP;
import fe.corba.FrontEndPOA;
import messages.branch.BranchReplyBody;
import messages.branch.BranchRequestBody;
import org.omg.CORBA.ORB;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class FrontEndImpl extends FrontEndPOA {

    private static final int ERROR_TIMEOUT = 3000;

    private ORB orb;
    private String FEID;
    private String branch;

    private ReliableUDP udp;

    private Message messages[];
    private boolean receivedAllResults;
    private boolean receivedFirstReply;

    private boolean success;
    private String finalResult;

    public void setAttributes(ORB orb, String FEID) {
        this.orb = orb;
        this.FEID = FEID;

        finalResult = "";
        receivedAllResults = false;

        try {
            udp = new ReliableUDP();
            messages = new Message[12];
            //	startFEUDP();
        } catch (Exception e) {
            System.out.println("Could not start reliable UDP listener!");
            System.exit(0);
        }
    }

    public void startFEUDP() {
        System.out.println("Starting udp listener for the FE in a new thread");
        Thread t = new Thread(this::UDPListener);
        t.start();
    }

    private void organizeReplies(boolean count) {
        int mInd = 0;
        while (!receivedAllResults) {
            if (success)
                return;
            Message reply = udp.receiveTimeout(100);
            if (reply != null) {
                printReply(reply);

                //String customID = reply.getHeader().customId;
                // then the FE received the correct reply
                //if (customID.equalsIgnoreCase(FEID)) {
                receivedFirstReply = true;
                // just to check that this is indeed the first and only reply
                for (int i = 0; i < messages.length; ++i) {
                    if (messages[i] != null)
                        receivedFirstReply = false;
                }
                if (receivedFirstReply && count == false) {
                    startHandlingPotentialRMCrash();
                }
                messages[mInd] = reply;
                ++mInd;
                //	}
            }
            //	System.out.println(mInd);
            if ((mInd == 3 && count == false)) {
                // we received all the replies, we can now handle the 3 messages to produce one correct reply for the client
                try {
                    receivedAllResults = true;
                    //		System.out.println("trying to handle all replies now");
                    finalResult = handleReplies(false);
                    //		System.out.println("final result should be: " + finalResult);

                } catch (Exception e) {
                    System.out.println("Problem in handling replies in the Front End");
                }
                mInd = 0;
            } else if ((mInd == 12 && count == true)) {
                // we received all the replies, we can now handle the 3 messages to produce one correct reply for the client
                try {
                    receivedAllResults = true;
                    finalResult = handleReplies(true);

                } catch (Exception e) {
                    System.out.println("Problem in handling replies in the Front End");
                }
                mInd = 0;
            }
        }
    }

    private void UDPListener() {
        //	organizeReplies();
    }

    private void startHandlingPotentialRMCrash() {
        System.out.println("Started watching out for potential RM crashes");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // so after 3 seconds, we execute this code

                if (!success) {
                    System.err.println("It's been " + ERROR_TIMEOUT + "ms and the Front End hasn't received all the needed replies");
                    BranchRequestBody body = new BranchRequestBody().notifyCrashError(
                            messages[0].getHeader().originId,
                            messages[1].getHeader().originId);
                    try {
                        udp.send(body, "notifyCrashError", "SEQ" + branch, FEID);
                        finalResult = handleReplies(false);
                    } catch (Exception e) {
                        System.out.println("Failed to notify the sequencer about a crash error");
                    }

                }
            }
        }, ERROR_TIMEOUT);

    }

    //This method checks the 3 replies and produces one single correct result
    private String handleReplies(boolean getCount) throws Exception {
        boolean problemDetected = false;
        System.out.print("Going to handle:\n");
        for (int i = 0; i < messages.length; ++i) {
            Message rep = messages[i];
            if (rep != null) {
                BranchReplyBody b;
                String r = "";
                b = (BranchReplyBody) rep.getBody();
                r = b.getReply();
                System.out.println(i + "." + r + "\n");
            }
        }

        String correctResult = "";
        int failedIndexes[] = new int[2];
        if (getCount == true) {
            Set<String> filterSet = new HashSet<>();
            for (Message reply : messages) {
                BranchReplyBody bodyI = (BranchReplyBody) reply.getBody();
                filterSet.add(bodyI.getReply().trim());
            }
            for (String uniqueReply : filterSet)
                correctResult += uniqueReply + "\n";

        } else if (getCount == false)
            for (int i = 0; i < messages.length; ++i) {
                for (int j = 0; j < messages.length; ++j) {
                    if (i != j && messages[i] != null && messages[j] != null) {
                        Message replyI = messages[i];
                        Message replyJ = messages[j];

                        BranchReplyBody bodyI = (BranchReplyBody) replyI.getBody();
                        BranchReplyBody bodyJ = (BranchReplyBody) replyJ.getBody();
                        String rI = bodyI.getReply();
                        String rJ = bodyJ.getReply();

                        if (rI.equalsIgnoreCase(rJ)) {
                            // then the results are the same

                            correctResult = rI;
                            //			System.out.println("correct result1:" + correctResult);
                        } else if (!problemDetected) {
                            //	System.out.println(rI + " vs " + rJ +  " IN: " + replyI.getHeader().originAddress);
                            // we got a problem
                            System.out.println("Detected a byzantine error.");
                            problemDetected = true;
                            failedIndexes[0] = i;
                            failedIndexes[1] = j;
                        }

                    }
                }
            }

        boolean foundIt = false;
        if (problemDetected)
            for (int i = 0; i < messages.length; ++i) {
                for (int j = 0; j < messages.length; ++j) {
                    Message replyI = messages[i];
                    Message replyJ = messages[j];

                    BranchReplyBody bodyI = (BranchReplyBody) replyI.getBody();
                    BranchReplyBody bodyJ = (BranchReplyBody) replyJ.getBody();
                    String rI = bodyI.getReply();
                    String rJ = bodyJ.getReply();
                    String failedRM = "";
                    if (rI.equalsIgnoreCase(correctResult)) {
                        correctResult = rJ;
                        failedRM = replyI.getHeader().originId;
                        BranchRequestBody body = new BranchRequestBody().notifyByzantineError(failedRM);
                        udp.send(body, "notifyByzantineError", "SEQ" + branch, FEID);
                        foundIt = true;
                    } else if (rJ.equalsIgnoreCase(correctResult)) {
                        correctResult = rI;
                        failedRM = replyJ.getHeader().originId;
                        BranchRequestBody body = new BranchRequestBody().notifyByzantineError(failedRM);
                        udp.send(body, "notifyByzantineError", "SEQ" + branch, FEID);
                        foundIt = true;
                    }
                    System.err.println("The error occured in RM: " + failedRM);
                    if (foundIt)
                        break;
                }
                if (foundIt)
                    break;
            }

        for (int i = 0; i < messages.length; ++i)
            messages[i] = null;
        int newFEID = Integer.parseInt(FEID);
        newFEID++;
        FEID = String.valueOf(newFEID);
        //System.out.println("correct result:" + correctResult);
        success = true;
        return correctResult;
    }

    public String createAccountRecord(String managerID, String firstName, String lastName, String address, String phone, String branch) {
        branch = managerID.substring(0, 2);
        success = false;
        try {
            BranchRequestBody body = new BranchRequestBody().createAccountRecord(managerID, firstName,
                    lastName, address, phone, branch);
            udp.send(body, "createAccountRecord", resolveSequencerId(managerID), FEID);
        } catch (Exception e) {
            System.out.println("Problem connecting through udp to sequencer from FE");
        }
        String retMessage = "";

        while (finalResult == "") {
            // wait for the system to compute the result
        }
        retMessage = finalResult;
        finalizeOp();
        return retMessage;
    }

    public String editRecord(String managerID, String customerID, String fieldName, String newValue) {
        success = false;
        branch = customerID.substring(0, 2);
        try {
            BranchRequestBody body = new BranchRequestBody().editRecord(managerID, customerID, fieldName, newValue);
            udp.send(body, "editRecord", resolveSequencerId(customerID), FEID);
        } catch (Exception e) {
            System.out.println("Problem connecting through udp to sequencer from FE");
        }
        String retMessage = "";

        while (finalResult == "") {
            // wait for the system to compute the result
            organizeReplies(false);
        }
        retMessage = finalResult;
        finalizeOp();
        return retMessage;
    }

    public String getAccountCount(String managerID) {
        success = false;
        branch = managerID.substring(0, 2);
        try {
            BranchRequestBody body = new BranchRequestBody().getAccountCount(managerID);
            udp.send(body, "getAccountCount", "SEQQC", FEID);
            int feid = Integer.valueOf(FEID);
            feid++;
            String newFEID = String.valueOf(feid);
            udp.send(body, "getAccountCount", "SEQMB", newFEID);
            feid++;
            newFEID = String.valueOf(feid);
            udp.send(body, "getAccountCount", "SEQNB", newFEID);
            feid++;
            newFEID = String.valueOf(feid);
            udp.send(body, "getAccountCount", "SEQBC", newFEID);
            System.out.println("Sent a multicast to the sequencer for getAccountCount.");
        } catch (Exception e) {
            System.out.println("Problem connecting through udp to sequencer from FE");
        }
        String retMessage = "";

        while (finalResult == "") {
            // wait for the system to compute the result
            organizeReplies(true);
        }
        retMessage = finalResult;
        finalizeOp();
        return retMessage;
    }

    public String transferFundManager(String managerID, String amount, String sourceCustomerID, String destinationCustomerID) {
        success = false;
        branch = sourceCustomerID.substring(0, 2);
        String retMessage = "sorry bro, but this aint gon work till the rest of the project is up";
        return retMessage;
    }

    public String transferFund(String sourceCustomerID, String amount, String destinationCustomerID) {
        success = false;
        branch = sourceCustomerID.substring(0, 2);
        String retMessage = "sorry bro, but this aint gon work till the rest of the project is up";
        return retMessage;
    }

    public String deposit(String customerID, String amount) {
        success = false;
        branch = customerID.substring(0, 2);
        try {
            BranchRequestBody body = new BranchRequestBody().deposit(customerID, amount);
            udp.send(body, "deposit", resolveSequencerId(customerID), FEID);
            System.out.println("Sent a request to the sequencer for a deposit.");
        } catch (Exception e) {
            System.out.println("Problem connecting through udp to sequencer from FE");
        }
        String retMessage = "";

        while (finalResult == "") {
            // wait for the system to compute the result
            organizeReplies(false);
        }
        retMessage = finalResult;
        finalizeOp();
        return retMessage;
    }

    public String withdraw(String customerID, String amount) {
        success = false;
        branch = customerID.substring(0, 2);
        try {
            BranchRequestBody body = new BranchRequestBody().withdraw(customerID, amount);
            udp.send(body, "withdraw", resolveSequencerId(customerID), FEID);
        } catch (Exception e) {
            System.out.println("Problem connecting through udp to sequencer from FE");
        }
        String retMessage = "";

        while (finalResult == "") {
            // wait for the system to compute the result
            organizeReplies(false);
        }
        retMessage = finalResult;
        finalizeOp();
        return retMessage;
    }

    public String getBalance(String customerID) {
        success = false;
        branch = customerID.substring(0, 2);
        try {
            BranchRequestBody body = new BranchRequestBody().getBalance(customerID);
            System.out.println("SENDING");
            udp.send(body, "getBalance", resolveSequencerId(customerID), FEID);
        } catch (Exception e) {
            System.out.println("Problem connecting through udp to sequencer from FE");
        }
        String retMessage = "";
        while (finalResult == "") {
            // wait for the system to compute the result
            organizeReplies(false);
        }
        retMessage = finalResult;
        finalizeOp();
        return retMessage;
    }

    public void shutdown() {
        orb.shutdown(false);
    }

    private void finalizeOp() {
        finalResult = "";
        receivedAllResults = false;
    }

    private String resolveSequencerId(String bankUserId) {
        return "SEQ" + bankUserId.substring(0, 2);
    }

    private void printReply(Message message) {
        System.out.println("Received message");
        System.out.println("\t-ID: " + message.getHeader().messageId);
        System.out.println("\t-Origin ID: " + message.getHeader().originId);
        System.out.println("\t-Origin Address: " + message.getHeader().originAddress);
        System.out.println("\t-Dest ID: " + message.getHeader().destinationId);
        System.out.println("\t-Dest Address: " + message.getHeader().destinationAddress + "\n");
    }
}
