package replicaManager;

import replicaManager.server.ReplicaManager;
import replicaManager.server.impl.ServerImpl;

public class Main {

    public static void main(String[] args) {

        //Validate Args
        if (args.length < 1) {
            System.out.println("Usage: rm <implementation (RA,SY,MA)>");
            System.exit(0);
        }

        //Set chosen server implementation
        ServerImpl serverImpl = null;

        String implChoice = args[1];
        switch (implChoice.toUpperCase()) {
            case "RA":
                serverImpl = ServerImpl.RADU;
                break;
            case "SY":
                serverImpl = ServerImpl.SYLVAIN;
                break;
            case "MA":
                serverImpl = ServerImpl.MATHIEU;
                break;
            default:
                System.out.println("Error: Server implementation must be one of (RA,SY,MA)");
                System.out.println("RA - Radu implementation" +
                        "\nSY - Sylvain implementation" +
                        "\nMA - Mathieu implementation");
                System.exit(0);
        }

        //Start replica manager
        ReplicaManager replicaManager = new ReplicaManager(serverImpl);

    }
}
