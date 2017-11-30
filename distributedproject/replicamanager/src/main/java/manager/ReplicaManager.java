package manager;

import com.message.Message;
import com.message.MessageHeader;
import com.reliable.ReliableUDP;
import manager.helpers.ErrorHelper;
import manager.helpers.NameHelper;
import manager.replicas.ServerImpl;
import messages.branch.BranchReplyBody;
import messages.branch.BranchRequestBody;
import server.Branch;
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
    private int byzantineCount = 0;
    private boolean didFail = false;

    //Error Trigger
    private boolean canFail;
    private boolean shouldCrash;

    /*
     * TRIGGERING BYZANTINE
     *   -Deposit 423 dollars into an account 3 times. Sylvain IMPL will byzantine.
     *
     * TRIGGERING CRASH
     *   -Deposit 42 dollars into an account. Sylvain IMPL will crash.
     */

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
                        reliableUDP.reply(header, replyBody, "");
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        } else {
            //Error restoration, waiting on db dump
            BranchReplyBody body = (BranchReplyBody) message.getBody();

            List<String> databaseDump = body.getReplyList();
            this.branchServer.restoreDatabase(databaseDump);

            resetErrorFlags();
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

                if (shouldCrash(requestMap.get("amount"))) {
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
                    if (byzantineCount == 3)
                        handleRestart();
                }
                break;
            case ERROR_CRASH:
                String crashRmId1 = requestMap.get("originID1");
                String crashRmId2 = requestMap.get("originID2");
                if (ErrorHelper.didICrash(rmNumber, crashRmId1, crashRmId2))
                    handleRestart();
                break;
            case REQUEST_DB_DUMP:
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

    private void handleRestart() {
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
        //Generate the database dump
        BranchReplyBody replyBody = new BranchReplyBody();
        replyBody.setReplyList(this.branchServer.dumpDatabase());

        //Send to crashed replica
        try {
            reliableUDP.reply(header, replyBody, "");
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void resetErrorFlags() {
        this.didFail = false;
    }

    /*
     * --------------
     * Error Triggers
     * --------------
     */

    public boolean shouldCrash(String amount) {
        if (new BigDecimal(amount).compareTo(new BigDecimal("42")) == 0)
            return true;
        return false;
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
