package replicaManager.server;

import replicaManager.server.impl.ServerImpl;

public class ReplicaManager {

    private ServerImpl serverImpl;

    public ReplicaManager(ServerImpl serverImpl) {
        this.serverImpl = serverImpl;
    }

    private void startServer() {
        //Start the server
    }

    private void startNetworking() {
        //Start the networking stack
    }
}
