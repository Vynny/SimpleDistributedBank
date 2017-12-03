package manager;

import com.message.Message;
import com.message.MessageHeader;
import com.reliable.ReliableUDP;
import manager.helpers.ErrorHelper;
import manager.helpers.NameHelper;
import manager.replicas.ServerImpl;
import messages.branch.BranchReplyBody;
import messages.branch.BranchRequestBody;
import enums.Branch;
import server.BranchServer;
import server.mathieu.branch.MathieuBranchImpl;
import server.radu.RaduBranchImpl;
import server.sylvain.SylvainBranchImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketException;
import java.util.List;
import java.util.Map;

public class ReplicaManager {

    //Naming
    private int rmNumber;
    private String rmId;

    //Branch Implementation
    private Branch branch;
    private ServerImpl serverImpl;
    private BranchServer branchServer;

    //Networking
    private ReliableUDP reliableUDP;
    private boolean isRunning = false;

    //Errors Handle
    private final static int BYZANTINE_MAX = 3;
    private int byzantineCount;
    private boolean didFail = false;

    //Error Trigger
    private boolean canFail;
    private boolean shouldCrash;

    public ReplicaManager(int rmNumber, Branch branch, ServerImpl serverImpl) {
        this.rmNumber = rmNumber;
        this.serverImpl = serverImpl;
        this.branch = branch;

        this.rmId = NameHelper.generateId(rmNumber, branch);

        //Start Services
        startBranchServer();
        startNetworking();

        //Start handling requests
        this.isRunning = true;
        handleRequests();
    }

    /*
     * ----------------
     * Init Operations
     * ----------------
     */

    //Start the manager
    private void startBranchServer() {

        this.canFail = false;
        this.shouldCrash = false;
        this.byzantineCount = 0;

        switch (serverImpl) {
            case RADU:
                this.branchServer = new RaduBranchImpl(branch.toString());
                break;
            case SYLVAIN:
                this.canFail = true;
                this.branchServer = new SylvainBranchImpl(branch);
                break;
            case MATHIEU:
                this.branchServer = new MathieuBranchImpl(branch.toString());
                break;
        }
    }

    //Start the networking stack
    private void startNetworking() {
        try {
            this.reliableUDP = new ReliableUDP(rmId);
            this.reliableUDP.startUDPMulticast();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * ----------------
     * Request Handling
     * ----------------
     */

    //Handle requests
    private void handleRequests() {
        System.out.println("Replica manager started " + replicaName());

        while (isRunning) {
            //Incoming
            System.out.println("\n Waiting for UDP message \n");
            Message inMessage = reliableUDP.receive();
            if (inMessage != null) {
                System.out.println("Message Received, ID: " + inMessage.getHeader().messageId);
                processMessage(inMessage);
            }
        }
    }

    //Handle messages from sequencer
    private void processMessage(Message message) {
        //Get header and body
        MessageHeader header = message.getHeader();

        if (!didFail) {
            //Normal request from FE
            BranchRequestBody body = (BranchRequestBody) message.getBody();

            //Generate a reply
            BranchReplyBody replyBody = handleBranchRequest(body, header);

            //Send the reply
            try {
                if (canFail && shouldCrash) {
                    //Do nothing to simulate a crash
                } else {
                    if (!didFail)
                        if (replyBody.getReply() != null || replyBody.getReplyList() != null)
                            reliableUDP.reply(header, replyBody, "");
                        else
                            System.out.println("Empty reply body, nothing sent.");
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        } else {
            //Error restoration, waiting on db dump
            try {
                BranchReplyBody body = (BranchReplyBody) message.getBody();

                List<String> databaseDump = body.getReplyList();

                System.out.println("\nRestoring server database!");
                this.branchServer.restoreDatabase(databaseDump);

                System.out.println("QCC1000 Balance: " + this.branchServer.getBalance("QCC1000"));
                System.out.println("QCC1001 Balance: " + this.branchServer.getBalance("QCC1001"));

                resetErrorFlags();
            } catch (ClassCastException e) {
                System.out.println("Message does not contain a db dump. " + replicaName());
            }
        }
    }

    //Build a reply to a message, or take another action
    private BranchReplyBody handleBranchRequest(BranchRequestBody branchRequestBody, MessageHeader header) {
        Map<String, String> requestMap = branchRequestBody.getRequestMap();

        String replyText = null;
        BranchReplyBody replyBody = new BranchReplyBody();
        switch (branchRequestBody.getOperationType()) {
            case DEPOSIT:
                replyText = branchServer.deposit(requestMap.get("customerId"), requestMap.get("amount"));

                if (canFail && shouldCrash(requestMap.get("amount"))) {
                    System.out.println("!!! CRASH ERROR TRIGGERED !!");
                    this.shouldCrash = true;
                }

                break;
            case WITHDRAW:
                replyText = branchServer.withdraw(requestMap.get("customerId"), requestMap.get("amount"));
                break;
            case GET_BALANCE:
                replyText = branchServer.getBalance(requestMap.get("customerId"));
                break;
            case CREATE_ACCOUNT_RECORD:
                replyText = branchServer.createAccountRecord(requestMap.get("managerId"),
                        requestMap.get("firstName"),
                        requestMap.get("lastName"),
                        requestMap.get("address"),
                        requestMap.get("phone"),
                        requestMap.get("branch"));
                break;
            case EDIT_RECORD:
                replyText = branchServer.editRecord(requestMap.get("managerId"),
                        requestMap.get("customerId"),
                        requestMap.get("fieldName"),
                        requestMap.get("newValue"));
                break;
            case GET_ACCOUNT_COUNT:
                replyText = branchServer.getAccountCount();
                break;
            case ERROR_BYZANTINE:
                String byzantineRmId = requestMap.get("originID");
                if (ErrorHelper.didIByzantine(rmId, byzantineRmId)) {
                    byzantineCount++;
                    System.out.println("Byzantine error detected. Count: " + byzantineCount + ", Max: " + BYZANTINE_MAX);
                    if (byzantineCount == BYZANTINE_MAX)
                        triggerRestart();
                }
                break;
            case ERROR_CRASH:
                String crashRmId1 = requestMap.get("originID1");
                String crashRmId2 = requestMap.get("originID2");
                if (ErrorHelper.didICrash(rmNumber, crashRmId1, crashRmId2))
                    triggerRestart();
                break;
            case REQUEST_DB_DUMP:
                if (serverImpl == ServerImpl.MATHIEU)
                    provideDatabaseDump(header);
                break;
        }

        if (!didFail) {
            System.out.println("Reply Generated " + replicaName());
            System.out.println(replyText);
            replyBody.setReply(replyText);
        }
        return replyBody;
    }

    /*
     * --------------
     * Error Handling
     * --------------
     */

    private void triggerRestart() {
        System.out.println("Notified of a byzantine or crash failure! " + replicaName());
        didFail = true;

        //Restart Impl
        System.out.println("Restarting server, using MA replica.");
        this.serverImpl = ServerImpl.MATHIEU;
        startBranchServer();
        System.out.println("Branch server restarted. " + replicaName());

        //Get DB Dump
        requestDatabaseDump();
    }

    private void requestDatabaseDump() {
        System.out.println("Requesting a DB Dump");
        BranchRequestBody dbDumpRequest = new BranchRequestBody().requestDatabaseDump();
        try {
            reliableUDP.send(dbDumpRequest, "", NameHelper.resolveSequencer(branch), rmId);
            System.out.println("-DB Dump request sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void provideDatabaseDump(MessageHeader header) {
        System.out.println("Providing a database dump to " + header.originId);
        //Generate the database dump
        BranchReplyBody replyBody = new BranchReplyBody();

        List<String> dbDump = this.branchServer.dumpDatabase();
        for (String s : dbDump) {
            System.out.println("DB LINE: " + s);
        }
        replyBody.setReplyList(dbDump);

        //Send to crashed replica
        try {
            reliableUDP.reply(header, replyBody, "");
            System.out.println("-DB dump sent to " + header.originId);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void resetErrorFlags() {
        System.out.println("Error has been handled. Failure flags have been reset, functionality back to normal.");
        this.didFail = false;
    }

    /*
     * --------------
     * Error Triggers
     * --------------
     */

    private boolean shouldCrash(String amount) {
        return new BigDecimal(amount).compareTo(new BigDecimal("42")) == 0;
    }

    /*
     * ---------
     * Utilities
     * ---------
     */

    private String replicaName() {
        return "(ID: " + rmId + ", REPLICA: " + serverImpl.toString() + ")";
    }
}
