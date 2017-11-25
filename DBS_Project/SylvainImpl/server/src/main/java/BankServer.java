import logging.BankLogger;
import models.branch.Branch;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import remote.corba.BankServerRemote;
import remote.corba.BankServerRemoteHelper;
import remote.corba.BankServerRemoteImpl;
import remote.udp.UDPListener;
import remote.util.CORBAConnector;
import remote.util.RemoteNameResolver;


public class BankServer {

    public static Branch branch;

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: server <branch>");
            for (Branch branch : Branch.values()) {
                System.out.println("\t-Branch: " + branch);
            }
            System.exit(1);
        }

        branch = Branch.valueOf(args[0]);

        //Start UDP Listener
        UDPListener udpListener = new UDPListener(branch);
        udpListener.listen();

        //Initialize CORBA ORB for Remote Connector
        CORBAConnector.initORB(args);

        //Initialize CORBA Remote Objects
        String serverName = RemoteNameResolver.getServerName(branch);
        BankLogger.logAction("Starting CORBA initialization for server: " + serverName);
        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get reference to rootpoa and activate the POAManager
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            // create servant and register it with the ORB
            BankServerRemoteImpl bankServerRemote = new BankServerRemoteImpl(branch, orb);

            // get object reference from the servant
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(bankServerRemote);
            BankServerRemote href = BankServerRemoteHelper.narrow(ref);

            // get the root naming context
            org.omg.CORBA.Object objRef =
                    orb.resolve_initial_references("NameService");
            // Use NamingContextExt
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // bind the Object Reference in Naming
            NameComponent path[] = ncRef.to_name(serverName);
            ncRef.rebind(path, href);

            BankLogger.logAction(serverName + " CORBA initialization finished. Waiting for requests...");
            orb.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
