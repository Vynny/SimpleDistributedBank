package fe;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import fe.corba.FrontEndPOA;
import org.omg.CORBA.ORB;

import com.reliable.ReliableUDP;
import messages.branch.*;
import com.message.Message;;
public class FrontEndImpl extends FrontEndPOA {
	private ORB orb;
	private int port;
	private ReliableUDP udp;
	private boolean receivedAllResults;
	private Message messages[];
	private String FEID;
	public void setORB(ORB orb_val) {
		orb = orb_val;
	}  
	public void setAttribs(ORB orb_val, String FEID) {
		this.FEID = FEID;
		try {
			udp = new ReliableUDP("remove this");
			messages = new Message[3];
		}
		
		catch(Exception e) {
			
			
		}
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
		int mInd = 0;
		while (true) {
			Message reply = udp.receive();
			if (reply != null) {
				String customID = reply.getHeader().customId;
				// then the FE received the correct reply
				if (customID.equalsIgnoreCase(FEID)) {
					messages[mInd] = reply;
					++mInd;
				}
			}
			
			if (mInd == 3) {
				// we received all the replies, we can now handle the 3 messages to produce one correct reply for the client
				try {
					
					handleReplies();
					for (int i = 0; i < messages.length; ++i)
						messages[i] = null;
				}
				catch (Exception e) {
					System.out.println("Problem in handling replies in the Front End");
				}
				mInd = 0;
			}
		}
	}
	
	// this method checks the 3 replies and produces one single correct result
	private String handleReplies() throws Exception{
		boolean problemDetected = false;
		String correctResult = "";
		int failedIndexes[] = new int[2];
		for (int i = 0; i < messages.length; ++i) {
			for (int j = 0; j < messages.length; ++j) {
				if (i != j) {
					Message replyI = messages[i];
					Message replyJ = messages[j];
					
					BranchReplyBody bodyI = (BranchReplyBody)replyI.getBody();
					BranchReplyBody bodyJ = (BranchReplyBody)replyJ.getBody();
					String rI = bodyI.getReply();
					String rJ = bodyJ.getReply();
					if (rI.equalsIgnoreCase(rJ)) {
						// then the results are the same
						correctResult = rI;
					} else if (!problemDetected){
						// we got a problem
						problemDetected = true;
						failedIndexes[0] = i;
						failedIndexes[1] = j;
					}
				}
			}
			
		}
		for (int i = 0; i < messages.length; ++i) {
			for (int j = 0; j < messages.length; ++j) {
				Message replyI = messages[i];
				Message replyJ = messages[j];
				
				BranchReplyBody bodyI = (BranchReplyBody)replyI.getBody();
				BranchReplyBody bodyJ = (BranchReplyBody)replyJ.getBody();
				String rI = bodyI.getReply();
				String rJ = bodyJ.getReply();
				if (rI.equalsIgnoreCase(correctResult)) {
					String failedRM = replyI.getHeader().originId;
					BranchRequestBody body = new BranchRequestBody();
					body.notifyError(failedRM, "Byzantine");
					udp.send(body, "notifyError", replyI.getHeader().destinationId, FEID);
				} else if (rJ.equalsIgnoreCase(correctResult)) {
					String failedRM = replyJ.getHeader().originId;
					BranchRequestBody body = new BranchRequestBody();
					body.notifyError(failedRM, "Byzantine");
					udp.send(body, "notifyError", replyJ.getHeader().destinationId, FEID);
				}
			}
			
		}
		return correctResult;
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
		try {
			BranchRequestBody body = new BranchRequestBody();
			body.deposit(customerID, String.valueOf(amount));
			udp.send(body, "deposit", customerID, FEID);
		}
		
		catch(Exception e) {
			System.out.println("Problem connecting through udp to sequencer from FE");
		}
		String retMessage = "";
		while (!receivedAllResults) {
			// check results
		}
		
		retMessage = "sorry bro, but this aint gon work till the rest of the project is up";
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
