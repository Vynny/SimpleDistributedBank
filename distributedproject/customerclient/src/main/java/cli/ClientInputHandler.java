package cli;


import fe.corba.FrontEnd;
import logging.BankLogger;
import util.CORBAConnector;

public class ClientInputHandler extends InputHandler {

    private enum Action {
        INVALID,

        DEPOSIT,
        WITHDRAW,
        TRANSFER,
        GETBALANCE
    }

    public ClientInputHandler(String userType) {
        super(userType);
    }

    @Override
    protected void parseInput(String input) {
        String[] fullCommand = input.split(" ");

        Action requestedAction = validateAction(fullCommand);
        if (requestedAction != Action.INVALID) {

            String customerId = fullCommand[1];
            if (isUserIdValid(customerId)) {
                try {
                    FrontEnd serverRemote = null;

                    if (requestedAction != Action.TRANSFER)
                        serverRemote = CORBAConnector.connectFrontEnd();

                    if (requestedAction == Action.TRANSFER || serverRemote != null) {

                        switch (requestedAction) {
                            case DEPOSIT:
                                BankLogger.logUserAction(customerId, serverRemote.deposit(customerId, fullCommand[2]));
                                break;
                            case WITHDRAW:
                                BankLogger.logUserAction(customerId, serverRemote.withdraw(customerId, fullCommand[2]));
                                break;
                            case TRANSFER:
                                //UDPBroadcaster.transferFund(null, fullCommand[2], fullCommand[1], fullCommand[3]);
                                break;
                            case GETBALANCE:
                                BankLogger.logUserAction(customerId, serverRemote.getBalance(customerId));
                                break;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Could not connect to remote branch server.");
                }
            } else {
                System.out.println("\nYou have entered an invalid customer id.\n");
            }
        } else {
            System.out.println("\nThat is not a valid action. Please consult the help command.\n");
        }
    }

    @Override
    protected Action validateAction(String[] input) {
        String action = input[0].toLowerCase();

        switch (action) {
            case "deposit":
                if (input.length == 3)
                    return Action.DEPOSIT;
                break;
            case "withdraw":
                if (input.length == 3)
                    return Action.WITHDRAW;
                break;
            case "transfer":
                if (input.length == 4)
                    return Action.TRANSFER;
                break;
            case "getbalance":
                if (input.length == 2)
                    return Action.GETBALANCE;
                break;
        }
        return Action.INVALID;
    }

    @Override
    protected void printHelp() {
        System.out.println("\n----------- " + getUserType() + " Operations -----------");

        System.out.println("deposit customerId amount");
        System.out.println("withdraw customerId amount");
        System.out.println("transfer sourceCustomerId amount destCustomerId");
        System.out.println("getBalance customerId");

        System.out.println("-------------------------------------------\n");
    }

}
