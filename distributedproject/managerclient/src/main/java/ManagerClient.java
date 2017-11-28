import cli.InputHandler;
import cli.ManagerInputHandler;

public class ManagerClient {

    public static void main (String[] args){
        //Initialize Input Handler
        InputHandler managerInputHandler = new ManagerInputHandler("Manager");
        managerInputHandler.gatherInput();
    }
}
