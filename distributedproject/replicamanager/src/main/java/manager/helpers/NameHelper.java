package manager.helpers;

import server.Branch;

public class NameHelper {

    public final static String NAME_PREFIX = "RM";

    public static String generateId(int rmNumber, Branch branch) {
        return NAME_PREFIX + rmNumber + branch.toString();
    }

    public static Integer extractRmNumber(String rmId) {
        Integer rmNumber = null;
        try {
            rmNumber = Integer.parseInt(rmId.substring(2, 3));
        } catch (NumberFormatException e) {
            System.out.println("Failed to parse rm number from rm id.");
            System.out.println("\trmId: " + rmId);
        }
        return rmNumber;
    }
}
