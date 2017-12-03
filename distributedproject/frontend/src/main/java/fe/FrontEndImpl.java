package fe;

import com.message.Message;
import com.reliable.ReliableUDP;
import enums.Branch;
import fe.corba.FrontEndPOA;
import messages.branch.BranchReplyBody;
import messages.branch.BranchRequestBody;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FrontEndImpl extends FrontEndPOA {

    private static final int ERROR_TIMEOUT = 3000;

    public enum OperationType {
        NORMAL,
        GET_ACCOUNT_COUNT,
        TRANSFER_FUND
    }

    private String FEID;
    private String branch;

    private ReliableUDP udp;

    private Message messages[];
    private boolean receivedAllResults;
    private boolean receivedFirstReply;

    private boolean success;
    private String finalResult;

    //Operation Specific
    private String transferSource;
    private String transferDest;
    private String transferAmount;

    public void setAttributes(String FEID) {
        this.FEID = FEID;

        finalResult = "";
        receivedAllResults = false;
        success = false;

        try {
            messages = new Message[12];
            udp = new ReliableUDP();
        } catch (Exception e) {
            System.out.println("Could not start reliable UDP listener!");
            System.exit(0);
        }
    }

    /*
     *
     * Handle UDP Replies
     *
     */

    private void organizeReplies(OperationType operationType) {
        int mInd = 0;
        while (!receivedAllResults) {
            if (success)
                return;

            Message reply = udp.receiveTimeout(100);
            if (reply != null) {
                printReply(reply);

                receivedFirstReply = true;
                //Check that this is indeed the first and only reply
                for (int i = 0; i < messages.length; ++i) {
                    if (messages[i] != null)
                        receivedFirstReply = false;
                }
                if (receivedFirstReply && operationType == OperationType.NORMAL) {
                    startHandlingPotentialRMCrash();
                }
                messages[mInd] = reply;
                ++mInd;
            }

            switch (operationType) {
                case NORMAL:
                    if (mInd == 3)
                        handleNormalOperation();
                    break;
                case GET_ACCOUNT_COUNT:
                    if (mInd == 12)
                        handleGetAccountCount();
                    break;
                case TRANSFER_FUND:
                    handleTransferFund(mInd);
                    break;
            }
        }
    }

    /*
     *
     * Handle various OperationTypes
     *
     */

    private void handleNormalOperation() {
        //Handle the 3 messages to produce one correct reply for the client
        try {
            receivedAllResults = true;
            //Handle Replies
            finalResult = handleReplies(false);

        } catch (Exception e) {
            System.out.println("Problem in handling replies in the Front End");
        }
    }

    private void handleGetAccountCount() {
        //Handle the 12 messages to produce one correct reply for the client
        try {
            receivedAllResults = true;
            finalResult = handleReplies(true);
        } catch (Exception e) {
            System.out.println("Problem in handling replies in the Front End");
        }
    }

    private void handleTransferFund(int messageCount) {
        //Setup Regex
        Pattern enoughFundsPattern = Pattern.compile("enough\\sfunds");
        Pattern accountExistsPattern = Pattern.compile("Could\\snot\\sfind");

        //Logic
        if (messageCount == 3) { //Check if the withdraw successful
            BranchReplyBody withdrawBody = (BranchReplyBody) messages[0].getBody();
            String withdrawReply = withdrawBody.getReply();

            //Check for enough funds and if account exists
            Matcher fundMatcher = enoughFundsPattern.matcher(withdrawReply);
            Matcher accountMatcher = accountExistsPattern.matcher(withdrawReply);

            if (fundMatcher.find()) {
                success = true;
                finalResult = "Transfer failed. Source account " + transferSource + " does not have enough funds to transfer $" + transferAmount + ".";
            } else if (accountMatcher.find()) {
                success = true;
                finalResult = "Transfer failed. Source account " + transferSource + " does not exist.";
            } else {
                //Everything passes, send a deposit request to the destination user
                System.out.println("Transfer Dest: " + transferDest);
                try {
                    BranchRequestBody body = new BranchRequestBody().deposit(transferDest, transferAmount);
                    udp.send(body, "deposit", resolveSequencerId(transferDest), FEID);
                    System.out.println("Sent deposit request for transfer fund operation.");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Problem connecting through udp to sequencer from FE");
                }
            }

        } else if (messageCount == 6) { //Check if the deposit was successful
            BranchReplyBody depositBody = (BranchReplyBody) messages[3].getBody();
            String depositReply = depositBody.getReply();

            //Check if account exists
            Matcher accountMatcher = accountExistsPattern.matcher(depositReply);
            if (accountMatcher.find()) {
                success = true;
                finalResult = "Transfer failed. Destination account " + transferDest + " does not exist.";

                //Roll back the withdraw
                try {
                    BranchRequestBody body = new BranchRequestBody().deposit(transferSource, transferAmount);
                    udp.send(body, "deposit", resolveSequencerId(transferSource), FEID);
                    System.out.println("Transfer could not be completed. Rolling back withdrawal from " + transferSource + " for $" + transferAmount);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Problem connecting through udp to sequencer from FE");
                }
            } else {
                success = true;
                finalResult = "Transferred $" + transferAmount + " from " + transferSource + " to " + transferDest + ".";
            }
        }
    }

    /*
     *
     * Handle Replies
     *
     */

    //This method checks the 3 replies and produces one single correct result
    private String handleReplies(boolean getCount) throws Exception {
        boolean problemDetected = false;
        System.out.print("Going to handle:\n");
        for (int i = 0; i < messages.length; ++i) {
            Message rep = messages[i];
            if (rep != null) {
                BranchReplyBody body = (BranchReplyBody) rep.getBody();
                System.out.println(i + "." + body.getReply() + "\n");
            }
        }

        String correctResult = "";
        int failedIndexes[] = new int[2];
        if (getCount) {
            Set<String> filterSet = new HashSet<>();
            for (Message reply : messages) {
                BranchReplyBody bodyI = (BranchReplyBody) reply.getBody();
                filterSet.add(bodyI.getReply().trim());
            }
            for (String uniqueReply : filterSet)
                correctResult += uniqueReply + "\n";

        } else {
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
                            //Results are the same
                            correctResult = rI;
                        } else if (!problemDetected) {
                            //We got a problem
                            System.out.println("Detected a byzantine error.");
                            problemDetected = true;
                            failedIndexes[0] = i;
                            failedIndexes[1] = j;
                        }

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
                        System.out.println("FailedRM: " + failedRM);
                        BranchRequestBody body = new BranchRequestBody().notifyByzantineError(failedRM);
                        udp.send(body, "notifyByzantineError", "SEQ" + branch, FEID);
                        foundIt = true;
                    } else if (rJ.equalsIgnoreCase(correctResult)) {
                        correctResult = rI;
                        failedRM = replyJ.getHeader().originId;
                        System.out.println("FailedRM: " + failedRM);
                        BranchRequestBody body = new BranchRequestBody().notifyByzantineError(failedRM);
                        udp.send(body, "notifyByzantineError", "SEQ" + branch, FEID);
                        foundIt = true;
                    }
                    System.err.println("The error occurred in RM: " + failedRM);
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

        success = true;
        return correctResult;
    }

    /*
     *
     * Potential Error Handling
     *
     */

    private void startHandlingPotentialRMCrash() {
        System.out.println("Started watching out for potential RM crashes");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
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

    /*
     *
     * Client Methods
     *
     */

    public String createAccountRecord(String managerID, String firstName, String lastName, String address, String phone, String branch) {
        branch = resolveBranch(managerID);
        try {
            BranchRequestBody body = new BranchRequestBody().createAccountRecord(managerID, firstName, lastName, address, phone, branch);
            udp.send(body, "createAccountRecord", resolveSequencerId(managerID), FEID);
        } catch (Exception e) {
            System.out.println("Problem connecting through udp to sequencer from FE");
        }

        while (finalResult.isEmpty()) {
            organizeReplies(OperationType.NORMAL);
        }

        return finalResult;
    }

    public String editRecord(String managerID, String customerID, String fieldName, String newValue) {
        branch = resolveBranch(customerID);
        try {
            BranchRequestBody body = new BranchRequestBody().editRecord(managerID, customerID, fieldName, newValue);
            udp.send(body, "editRecord", resolveSequencerId(customerID), FEID);
        } catch (Exception e) {
            System.out.println("Problem connecting through udp to sequencer from FE");
        }

        while (finalResult.isEmpty()) {
            organizeReplies(OperationType.NORMAL);
        }

        return finalResult;
    }

    public String getAccountCount(String managerID) {
        branch = resolveBranch(managerID);
        try {
            BranchRequestBody body = new BranchRequestBody().getAccountCount(managerID);
            for (Branch branch : Branch.values()) {
                udp.send(body, "getAccountCount", "SEQ" + branch.toString(), FEID);
            }
            System.out.println("Sent messages to sequencer for getAccountCount.");
        } catch (Exception e) {
            System.out.println("Problem connecting through udp to sequencer from FE");
        }

        while (finalResult.isEmpty()) {
            organizeReplies(OperationType.GET_ACCOUNT_COUNT);
        }

        return finalResult;
    }

    public String transferFundManager(String managerID, String amount, String sourceCustomerID, String destinationCustomerID) {
        //Unused
        return null;
    }

    public String transferFund(String sourceCustomerID, String amount, String destinationCustomerID) {
        branch = resolveBranch(sourceCustomerID);

        //Set transaction specific variables
        transferSource = sourceCustomerID;
        transferDest = destinationCustomerID;
        transferAmount = amount;

        //Send withdraw request
        try {
            BranchRequestBody body = new BranchRequestBody().withdraw(sourceCustomerID, amount);
            udp.send(body, "withdraw", resolveSequencerId(sourceCustomerID), FEID);
            System.out.println("Sent a request to the sequencer for a transferFund.");
        } catch (Exception e) {
            System.out.println("Problem connecting through udp to sequencer from FE");
        }

        while (finalResult.isEmpty()) {
            organizeReplies(OperationType.TRANSFER_FUND);
        }

        return finalResult;
    }

    public String deposit(String customerID, String amount) {
        branch = resolveBranch(customerID);
        try {
            BranchRequestBody body = new BranchRequestBody().deposit(customerID, amount);
            udp.send(body, "deposit", resolveSequencerId(customerID), FEID);
            System.out.println("Sent a request to the sequencer for a deposit.");
        } catch (Exception e) {
            System.out.println("Problem connecting through udp to sequencer from FE");
        }

        while (finalResult.isEmpty()) {
            organizeReplies(OperationType.NORMAL);
        }

        return finalResult;
    }

    public String withdraw(String customerID, String amount) {
        branch = resolveBranch(customerID);
        try {
            BranchRequestBody body = new BranchRequestBody().withdraw(customerID, amount);
            udp.send(body, "withdraw", resolveSequencerId(customerID), FEID);
        } catch (Exception e) {
            System.out.println("Problem connecting through udp to sequencer from FE");
        }

        while (finalResult.isEmpty()) {
            organizeReplies(OperationType.NORMAL);
        }

        return finalResult;
    }

    public String getBalance(String customerID) {
        branch = resolveBranch(customerID);
        try {
            BranchRequestBody body = new BranchRequestBody().getBalance(customerID);
            udp.send(body, "getBalance", resolveSequencerId(customerID), FEID);
        } catch (Exception e) {
            System.out.println("Problem connecting through udp to sequencer from FE");
        }

        while (finalResult.isEmpty()) {
            organizeReplies(OperationType.NORMAL);
        }

        return finalResult;
    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub
    }

    /*
     *
     * Utilities
     *
     */

    private String resolveSequencerId(String bankUserId) {
        return "SEQ" + resolveBranch(bankUserId);
    }

    private String resolveBranch(String bankUserId) {
        return bankUserId.substring(0, 2);
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
