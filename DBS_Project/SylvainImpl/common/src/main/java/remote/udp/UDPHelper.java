package remote.udp;

public class UDPHelper {
    public static final int SERVER_LISTEN_PORT = 36875;
    public static final int CLIENT_LISTEN_PORT = 36876;

    public static final String DELIM = ",";

    public static final String OPERATION_GETACCOUNTCOUNT = "accountCount";

    public static final String OPERATION_TRANSFER = "transfer";

    public static final String OPERATION_TRANSFER_RELAY = "transferRelay";
    public static final String OPERATION_TRANSFER_RELAY_RESPONSE = "transferRelayResponse";

    public static String buildMessage(String listenerId, String operationId, String... args) {
        StringBuilder sb = new StringBuilder();

        sb.append(listenerId);
        sb.append(UDPHelper.DELIM);

        sb.append(operationId);
        sb.append(UDPHelper.DELIM);

        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i != args.length - 1)
                sb.append(UDPHelper.DELIM);
        }

        return sb.toString();
    }
}
