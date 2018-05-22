package util;

import fe.corba.FrontEnd;
import fe.corba.FrontEndHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class CORBAConnector {

    private static ORB orb = null;

    public static FrontEnd connectFrontEnd() throws Exception {
        if (orb == null)
            initORB();

        String frontEndName = "FrontEnd";
        FrontEnd frontEnd = null;

        // Get root naming context
        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

        // Resolve reference in naming services
        frontEnd = FrontEndHelper.narrow(ncRef.resolve_str(frontEndName));

        return frontEnd;
    }

    private static void initORB() {
        orb = ORB.init(new String[0], CORBAConfig.getCorbaProperties());
    }
}
