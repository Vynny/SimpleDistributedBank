package manager;

import com.reliable.ReliableUDP;
import manager.impl.ServerImpl;
import server.Branch;
import server.BranchServer;
import server.sylvain.BankServerRemoteImpl;

import java.net.SocketException;

public class ReplicaManager {

    public final static String NAME_PREFIX = "RM";

    private int rmId;
    private String rmName;

    private Branch branch;
    private ServerImpl serverImpl;
    private BranchServer branchServer;

    //Networking
    private ReliableUDP reliableUDP;

    public ReplicaManager(int rmId, Branch branch, ServerImpl serverImpl) {
        this.rmId = rmId;
        this.serverImpl = serverImpl;
        this.branch = branch;

        this.rmName = NAME_PREFIX + rmId + branch.toString();

        //Start Services
        startServer();
        startNetworking();
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
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
