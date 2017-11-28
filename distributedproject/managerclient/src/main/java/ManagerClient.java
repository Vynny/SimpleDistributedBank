import cli.InputHandler;
import cli.ManagerInputHandler;
import util.CORBAConnector;

public class ManagerClient {

    public static void main (String[]args){
        //Initialize ORB
        CORBAConnector.initORB(args);

        //Initialize Input Handler
        InputHandler managerInputHandler = new ManagerInputHandler("Manager");
        managerInputHandler.gatherInput();
    }
}
