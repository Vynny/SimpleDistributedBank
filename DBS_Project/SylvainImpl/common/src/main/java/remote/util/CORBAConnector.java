package remote.util;

import logging.BankLogger;
import models.branch.Branch;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import remote.corba.BankServerRemote;
import remote.corba.BankServerRemoteHelper;

public class CORBAConnector {

    private static ORB orb;

    public static void initORB(String[] args) {
        orb = ORB.init(args, null);
    }

    public static BankServerRemote connectServer(String userId) throws Exception {
        Branch branch;

        try {
            branch = Branch.valueOf(userId.substring(0, 2));
        } catch (IllegalArgumentException e) {
            BankLogger.logUserAction(userId, "ID entered does not correspond to a valid branch");
            return null;
        }

        return connectServer(branch);
    }

    public static BankServerRemote connectServer(Branch branch) throws Exception {
        String serverName = RemoteNameResolver.getServerName(Branch.valueOf(branch.toString()));
        BankServerRemote bankServerRemote = null;

        // get the root naming context
        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

        // resolve the Object Reference in Naming
        bankServerRemote = BankServerRemoteHelper.narrow(ncRef.resolve_str(serverName));

        return bankServerRemote;
    }
}
