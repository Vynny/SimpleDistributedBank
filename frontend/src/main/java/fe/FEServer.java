package fe;

import fe.corba.FrontEnd;
import fe.corba.FrontEndHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;

import util.CORBAConfig;

public class FEServer extends Thread {

    public void run() {
        startServer();
    }

    public void startServer() {
        try {
            ORB orb = ORB.init(new String[0], CORBAConfig.getCorbaProperties());
            setupCorba(orb);
            orb.run();
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }

        System.out.println("Server Exiting ...");

    }

    void setupCorba(ORB orb) throws Exception {
        POA rootpoa = (POA) orb.resolve_initial_references("RootPOA");
        rootpoa.the_POAManager().activate();
        
        Policy poaPolicy[] = new Policy[2];
        poaPolicy[0] = rootpoa.create_servant_retention_policy(
            ServantRetentionPolicyValue.NON_RETAIN);
        poaPolicy[1] = rootpoa.create_request_processing_policy(
            RequestProcessingPolicyValue.USE_SERVANT_MANAGER);


        POA poa1 = rootpoa.create_POA("FrontEndPOA", null, poaPolicy);
        poa1.the_POAManager().activate();
        
        poa1.set_servant_manager(new PoaServantLocator());
        
        
        org.omg.CORBA.Object objectRef = poa1.create_reference(
                FrontEndHelper.id());
        
        // get the root naming context
        // NameService invokes the transient name service
        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
        // Use NamingContextExt, which is part of the
        // Interoperable Naming Service (INS) specification.
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
        // bind the Object Reference in Naming
        String name = "FrontEnd";
        NameComponent path[] = ncRef.to_name(name);

        ncRef.rebind(path, objectRef);
        System.out.println("Server ready and waiting ...");

        // wait for invocations from clients
    }
}

