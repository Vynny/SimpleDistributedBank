import cli.ClientInputHandler;
import cli.InputHandler;
import util.CORBAConnector;

public class CustomerClient {

    public static void main(String[] args) {
        //Initialize ORB
        CORBAConnector.initORB(args);

        //Initialize Input Handler
        InputHandler clientInputHandler = new ClientInputHandler("Customer");
        clientInputHandler.gatherInput();
    }

}
