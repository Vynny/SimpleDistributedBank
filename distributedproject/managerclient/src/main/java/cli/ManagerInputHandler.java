package cli;

import fe.corba.FrontEnd;
import logging.BankLogger;
import util.CORBAConnector;

public class ManagerInputHandler extends InputHandler {

    private enum Action {
        INVALID,

        //Client Actions
        DEPOSIT,
        WITHDRAW,
        GETBALANCE,

        //Manager Actions
        CREATE_RECORD,
        EDIT_RECORD,
        TRANSFER,
        GET_ACCOUNT_COUNT
    }

    public ManagerInputHandler(String userType) {
        super(userType);
    }

    @Override
    protected void parseInput(String input) {
        String[] fullCommand = input.split(" ");

        Action requestedAction = validateAction(fullCommand);
        if (requestedAction != Action.INVALID) {

            try {
                FrontEnd serverRemote = null;

                if (requestedAction != Action.GET_ACCOUNT_COUNT && requestedAction != Action.TRANSFER)
                    serverRemote = CORBAConnector.connectFrontEnd();

                if (requestedAction == Action.GET_ACCOUNT_COUNT || requestedAction == Action.TRANSFER || serverRemote != null) {

                    switch (requestedAction) {
                        case CREATE_RECORD:
                            BankLogger.logUserAction(fullCommand[1], serverRemote.createAccountRecord(fullCommand[1], fullCommand[2], fullCommand[3], fullCommand[4], fullCommand[5], fullCommand[6]));
                            break;
                        case EDIT_RECORD:
                            BankLogger.logUserAction(fullCommand[1], serverRemote.editRecord(fullCommand[1], fullCommand[2], fullCommand[3], fullCommand[4]));
                            break;
                        case TRANSFER:
                            //UDPBroadcaster.transferFund(fullCommand[1], fullCommand[2], fullCommand[3], fullCommand[4]);
                            break;
                        case GET_ACCOUNT_COUNT:
                            //UDPBroadcaster.getAccountCount(fullCommand[1]);
                            break;
                        case DEPOSIT:
                            BankLogger.logUserAction(fullCommand[1], serverRemote.deposit(fullCommand[2], Integer.parseInt(fullCommand[3])));
                            break;
                        case WITHDRAW:
                            BankLogger.logUserAction(fullCommand[1], serverRemote.withdraw(fullCommand[2], Integer.parseInt(fullCommand[3])));
                            break;
                        case GETBALANCE:
                            //BankLogger.logUserAction(fullCommand[1], serverRemote.getBalance(fullCommand[2]));
                            break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not connect to remote branch server.");
            }
        } else {
            System.out.println("\nThat is not a valid action. Please consult the help command.\n");
        }
    }

    @Override
    protected Action validateAction(String[] input) {
        String action = input[0].toLowerCase();

        switch (action) {
            case "createaccountrecord":
                if (input.length == 7)
                    return Action.CREATE_RECORD;
                break;
            case "editrecord":
                if (input.length == 5)
                    return Action.EDIT_RECORD;
                break;
            case "transfer":
                if (input.length == 5)
                    return Action.TRANSFER;
                break;
            case "getaccountcount":
                if (input.length == 2)
                    return Action.GET_ACCOUNT_COUNT;
                break;
            case "deposit":
                if (input.length == 4)
                    return Action.DEPOSIT;
                break;
            case "withdraw":
                if (input.length == 4)
                    return Action.WITHDRAW;
                break;
            case "getbalance":
                if (input.length == 3)
                    return Action.GETBALANCE;
                break;
        }
        return Action.INVALID;
    }

    @Override
    protected void printHelp() {
        System.out.println("\n----------- " + getUserType() + " Operations -----------");

        System.out.println("-- Manager Commands --");
        System.out.println("createAccountRecord managerId firstName lastName address phone branch");
        System.out.println("editRecord managerId customerId fieldName newValue");
        System.out.println("transfer managerId amount sourceCustomerId destCustomerId");
        System.out.println("getAccountCount managerId");

        System.out.println();

        System.out.println("-- Customer Commands --");
        System.out.println("deposit managerId customerId amount");
        System.out.println("withdraw managerId customerId amount");
        System.out.println("getBalance managerId customerId");

        System.out.println("-------------------------------------------\n");
    }

}
