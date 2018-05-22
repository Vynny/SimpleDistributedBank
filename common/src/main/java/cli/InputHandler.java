package cli;

import java.util.Scanner;

public abstract class InputHandler {

    private String userType;

    public InputHandler(String userType) {
        this.userType = userType;
    }

    public void gatherInput() {
        System.out.println("Awaiting input. Type help for more information. Type exit to quit. \n");

        Scanner sc = new Scanner(System.in);

        String input;
        while ((input = sc.nextLine()) != null) {
            if (validateInput(input)) {
                if (!processActionString(input))
                    parseInput(input);
            } else {
                System.out.println("Invalid command entered");
            }
        }
    }

    public boolean isUserIdValid(String userId) {
        if (userId.length() > 2) {
            String firstTwo = userId.substring(0, 2).toLowerCase();
            if (firstTwo.equals("qc") || firstTwo.equals("mb") || firstTwo.equals("nb") || firstTwo.equals("bc"))
                return true;
        }
        return false;
    }

    protected abstract void parseInput(String input);

    protected abstract Enum<?> validateAction(String[] input);

    protected abstract void printHelp();

    private boolean validateInput(String input) {
        if (input == null || input.isEmpty())
            return false;
        return true;
    }

    private boolean processActionString(String input) {
        if (input.toLowerCase().equals("help")) {
            printHelp();
            return true;
        }

        if (input.toLowerCase().equals("exit")) {
            System.out.println("Thank you for using " + userType + " Client.");
            System.exit(0);
        }

        return false;
    }

    public String getUserType() {
        return userType;
    }
}
