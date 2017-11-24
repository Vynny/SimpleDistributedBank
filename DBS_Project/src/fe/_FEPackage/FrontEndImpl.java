package fe._FEPackage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.omg.CORBA.ORB;


public class FrontEndImpl extends FrontEndPOA{
	private ORB orb;
	private int port;
	public void setORB(ORB orb_val) {
		orb = orb_val; 
	}  
	public void setUDPPort(int p) {
		  port = p;
		  System.out.println("setting new udp port for the front end at: " + p);
		    Thread t = new Thread(new Runnable() {
		         @Override
		         public void run() {
		        	 UDPListener();
		         }
		});
		    t.start();
	}
	
	// not complete, need to just agree on UDP format first
	public void UDPListener() { 
		DatagramSocket aSocket = null;

		try {
			aSocket = new DatagramSocket(port);
		    InetAddress aHost = InetAddress.getLocalHost();
		    byte[] buffer = new byte[1000];
		    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
		    aSocket.receive(request);
		    String r = new String(request.getData());
		    
		    String contents[] = r.split("*");
		    String first = contents[0];
		    String second = contents[1];
		    
		    
		    String re = "Reply from FrontEnd";
		    byte [] m = re.getBytes();
		    // get it from the request
		    int sourcePort = 0;
		    DatagramPacket reply = new DatagramPacket(m, re.length(),aHost, sourcePort);
		  //    System.out.println("should send reply on port " + sourcePort);
		    DatagramSocket s = new DatagramSocket();
		    s.send(reply);
		    s.close();
		    aSocket.close();
		      
		    }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
		    
		    }catch (IOException e){System.out.println("IO: " + e.getMessage());
		    }catch (Exception e){System.out.println("General exception: " + e.getMessage());
	   	}
		UDPListener();
	}

	public String createAccountRecord(String managerID, String firstName, String lastName, String address
			   , String phone, String branch) {
		String retMessage = "sorry bro, but this aint gon work till the rest of the project is up";
		return retMessage;
	}
	public String editRecord(String managerID, String customerID, String fieldName, String newValue) {
		String retMessage = "sorry bro, but this aint gon work till the rest of the project is up";
		return retMessage;
	}
	public String getAccountCount(String managerID) {
		String retMessage = "sorry bro, but this aint gon work till the rest of the project is up";
		return retMessage;
	}
	   
	public String transferFundManager (String managerID,int amount, String sourceCustomerID,String destinationCustomerID) {
		String retMessage = "sorry bro, but this aint gon work till the rest of the project is up";
		return retMessage;
	}
	public void shutdown() {
		    orb.shutdown(false);
    }
	
	public String transferFund(String sourceCustomerID,int amount,String destinationCustomerID) {
		String retMessage = "sorry bro, but this aint gon work till the rest of the project is up";
		return retMessage;
	}
	public String deposit(String customerID,int amount) {
		String retMessage = "sorry bro, but this aint gon work till the rest of the project is up";
		return retMessage;
	}
	public String withdraw(String customerID, int amount) {
		String retMessage = "sorry bro, but this aint gon work till the rest of the project is up";
		return retMessage;
	}
	public int getBalance(String customerID) {
		   return 0;
	}
}
