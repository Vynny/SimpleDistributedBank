package fe;

import fe.corba.FrontEnd;
import fe.corba.FrontEndHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
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

        // create servant and register it with the ORB
        FrontEndImpl feImpl = new FrontEndImpl();
        feImpl.setAttribs(orb, "1234");
        // get object reference from the servant
        org.omg.CORBA.Object ref = rootpoa.servant_to_reference(feImpl);
        // and cast the reference to a CORBA reference
        FrontEnd href = FrontEndHelper.narrow(ref);

        // get the root naming context
        // NameService invokes the transient name service
        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
        // Use NamingContextExt, which is part of the
        // Interoperable Naming Service (INS) specification.
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
        // bind the Object Reference in Naming
        String name = "FrontEnd";
        NameComponent path[] = ncRef.to_name(name);

        ncRef.rebind(path, href);
        System.out.println("Server ready and waiting ...");

        // wait for invocations from clients
    }
}

