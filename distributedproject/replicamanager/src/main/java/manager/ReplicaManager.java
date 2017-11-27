package manager;

import com.message.Message;
import com.message.MessageHeader;
import com.reliable.ReliableUDP;
import manager.impl.ServerImpl;
import messages.branch.BranchReplyBody;
import messages.branch.BranchRequestBody;
import server.Branch;
import server.BranchServer;
import server.sylvain.BankServerRemoteImpl;

import java.io.IOException;
import java.net.SocketException;
import java.util.Map;

public class ReplicaManager {

    public final static String NAME_PREFIX = "RM";

    //Naming
    private int rmId;
    private String rmName;

    //Branch Implementation
    private Branch branch;
    private ServerImpl serverImpl;
    private BranchServer branchServer;

    //Networking
    private ReliableUDP reliableUDP;
    private boolean isRunning = false;

    public ReplicaManager(int rmId, Branch branch, ServerImpl serverImpl) {
        this.rmId = rmId;
        this.serverImpl = serverImpl;
        this.branch = branch;

        this.rmName = NAME_PREFIX + rmId + branch.toString();

        //Start Services
        startServer();
        startNetworking();

        //Start handling requests
        this.isRunning = true;
        handleRequests();
    }

    //Start the manager
    private void startServer() {
        switch (serverImpl) {
            case RADU:
                break;
            case SYLVAIN:
                this.branchServer = new BankServerRemoteImpl(branch);
                break;
            case MATHIEU:
                break;
        }
    }

    //Start the networking stack
    private void startNetworking() {
        try {
            this.reliableUDP = new ReliableUDP(rmName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Handle requests
    private void handleRequests() {
        System.out.println("Waiting for messages");

        while (isRunning) {

            //Incoming
            Message inMessage = reliableUDP.receive();
            if (inMessage != null) {
                System.out.println("Message Received: " + inMessage.getHeader().sequenceId);
                processMessage(inMessage);
            }

        }

    }

    private void processMessage(Message message) {
        //Get header and body
        MessageHeader header = message.getHeader();
        BranchRequestBody body = (BranchRequestBody) message.getBody();

        //Generate a reply
        BranchReplyBody replyBody = handleBranchRequest(body);

        //Send the reply
        try {
            reliableUDP.reply(header, replyBody, "");
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private BranchReplyBody handleBranchRequest(BranchRequestBody branchRequestBody) {
        Map<String, String> requestMap = branchRequestBody.getRequestMap();

        BranchReplyBody replyBody = new BranchReplyBody();
        switch (branchRequestBody.getOperationType()) {
            case DEPOSIT:
                replyBody.setReply(branchServer.deposit(requestMap.get("customerId"), requestMap.get("amount")));
                break;
            case WITHDRAW:
                replyBody.setReply(branchServer.withdraw(requestMap.get("customerId"), requestMap.get("amount")));
                break;
            case GET_BALANCE:
                replyBody.setReply(branchServer.getBalance(requestMap.get("customerId")));
                break;
            case CREATE_ACCOUNT_RECORD:
                replyBody.setReply(branchServer.createAccountRecord(requestMap.get("managerId"),
                        requestMap.get("firstName"),
                        requestMap.get("lastName"),
                        requestMap.get("address"),
                        requestMap.get("phone"),
                        requestMap.get("branch")));
                break;
            case EDIT_RECORD:
                replyBody.setReply(branchServer.editRecord(requestMap.get("managerId"),
                        requestMap.get("customerId"),
                        requestMap.get("fieldName"),
                        requestMap.get("newValue")));
                break;
            case ERROR:
                //TODO: Branch Error
                break;
        }

        return replyBody;
    }
}
