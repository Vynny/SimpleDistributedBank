package remote.util;

import models.branch.Branch;

public class RemoteNameResolver {

    public static final String SERVER_PREFIX = "ServerRemote";
    public static final String SEPARATOR = "-";

    public static String getServerName(Branch branch) {
        return SERVER_PREFIX + SEPARATOR + branch.toString();
    }
}
