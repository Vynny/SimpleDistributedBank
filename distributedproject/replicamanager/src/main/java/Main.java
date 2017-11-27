import manager.ReplicaManager;
import server.Branch;
import manager.impl.ServerImpl;

public class Main {

    public static void main(String[] args) {

        //Validate Args
        if (args.length < 2) {
            System.out.println("Usage: rm <implementation (RA,SY,MA)> <branch (BC,MB,NB,QC)>");
            System.exit(0);
        }

        //Set chosen manager implementation
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

        //Set chosen branch
        Branch branch = null;

        String branchChoice = args[2];
        try {
            branch = Branch.valueOf(branchChoice);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Branch must be one of (BC,MB,NB,QC)");
            System.exit(0);
        }

        //Start replica manager
        ReplicaManager replicaManager = new ReplicaManager(branch, serverImpl);

    }
}
