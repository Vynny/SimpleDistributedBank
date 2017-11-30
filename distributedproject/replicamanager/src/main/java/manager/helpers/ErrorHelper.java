package manager.helpers;

public class ErrorHelper {

    public static boolean didICrash(int myNumber, String origin1, String origin2) {
        Integer origin1Number = NameHelper.extractRmNumber(origin1);
        Integer origin2Number = NameHelper.extractRmNumber(origin2);

        if (origin1Number != myNumber || origin2Number != myNumber)
            return true;

        return false;
    }

    public static boolean didIByzantine(String myId, String origin) {
        return myId.equalsIgnoreCase(origin);
    }
}
