package util;

import fe.corba.FrontEnd;
import fe.corba.FrontEndHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class CORBAConnector {

    private static ORB orb;

    public static void initORB(String[] args) {
        orb = ORB.init(args, null);
    }

    public static FrontEnd connectFrontEnd() throws Exception {
        String frontEndName = "FrontEnd";
        FrontEnd frontEnd = null;

        // Get root naming context
        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

        // Resolve reference in naming services
        frontEnd = FrontEndHelper.narrow(ncRef.resolve_str(frontEndName));

        return frontEnd;
    }
}
