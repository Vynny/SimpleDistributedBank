package replicaManager.server;

import remote.BranchServer;
import replicaManager.server.impl.Branch;
import replicaManager.server.impl.ServerImpl;

public class ReplicaManager {

    private Branch branch;
    private ServerImpl serverImpl;
    private BranchServer branchServer;

    public ReplicaManager(Branch branch, ServerImpl serverImpl) {
        this.serverImpl = serverImpl;
        this.branch = branch;
    }

    //Start the server
    private void startServer() {
        switch (serverImpl) {
            case RADU:
                break;
            case SYLVAIN:
                //this.branchServer = new BankServerRemoteImpl(branch);
                break;
            case MATHIEU:
                break;
        }
    }

    //Start the networking stack
    private void startNetworking() {

    }
}
