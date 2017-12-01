package manager.helpers;

import enums.Branch;

public class NameHelper {

    private final static String NAME_PREFIX = "RM";
    private final static String SEQ_PREFIX = "SEQ";

    public static String generateId(int rmNumber, Branch branch) {
        return NAME_PREFIX + rmNumber + branch.toString();
    }

    public static String resolveSequencer(Branch branch) {
        return SEQ_PREFIX + branch.toString();
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
