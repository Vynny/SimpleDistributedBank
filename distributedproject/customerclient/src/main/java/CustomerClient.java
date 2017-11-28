import cli.ClientInputHandler;
import cli.InputHandler;

public class CustomerClient {

    public static void main(String[] args) {
        //Initialize Input Handler
        InputHandler clientInputHandler = new ClientInputHandler("Customer");
        clientInputHandler.gatherInput();
    }

}
