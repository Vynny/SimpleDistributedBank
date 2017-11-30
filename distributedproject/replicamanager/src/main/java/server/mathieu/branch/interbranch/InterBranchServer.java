package server.mathieu.branch.interbranch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import server.mathieu.branch.BranchImpl;

public class InterBranchServer extends Thread {
	private static final String MESSAGE_METHOD_ERROR = "ERROR";
	private static final String MESSAGE_METHOD_NAME_TRANSFER = "transfer";
	private static final String MESSAGE_METHOD_TRANSFER_ACK = "transferack";
	private static final int MESSAGE_MAX_SIZE = 10240; // 10kbytes

	private DatagramSocket socket;
	private BranchImpl branchServer;

	private InterBranchServer(BranchImpl branchServer, DatagramSocket socket) {
		this.socket = socket;
		this.branchServer = branchServer;
	}

	public static BranchLocation startInterBranchServer(BranchImpl branchServer) {
		DatagramSocket socket;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			BranchImpl.logger.println("Unable to start the UDP Server");
			return null;
		}
		String hostName = "localhost";
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			BranchImpl.logger.println("Unable to retrieve hostName, reverting to localhost");
		}
		BranchLocation location = new BranchLocation(branchServer.ID, hostName, socket.getLocalPort());

		InterBranchServer server = new InterBranchServer(branchServer, socket);
		server.start();
		return location;
	}

	private static InterBranchPacket decodeDatagramPacket(DatagramPacket packet) {
		InterBranchPacket packetObject = null;
		try {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData());
			ObjectInputStream objInput = new ObjectInputStream(new BufferedInputStream(byteStream));
			packetObject = (InterBranchPacket) objInput.readObject();
			objInput.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return packetObject;
	}

	private static DatagramPacket buildDatagramPacket(SocketAddress address, InterBranchPacket packetObject) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objOutput;
		try {
			objOutput = new ObjectOutputStream(new BufferedOutputStream(byteStream));
			objOutput.writeObject(packetObject);
			objOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		byte[] byteMessage = byteStream.toByteArray();

		DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, address);
		return packet;
	}

	// public void run() {
	// while (true) {
	// byte[] requestBuffer = new byte[MESSAGE_MAX_SIZE];
	// DatagramPacket request = new DatagramPacket(requestBuffer,
	// requestBuffer.length);
	// try {
	// socket.receive(request);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// InterBranchPacket requestObject = decodeDatagramPacket(request);
	// if (requestObject == null) {
	// BranchImpl.logger.println("Received an invalid request Object");
	// continue;
	// }
	//
	// if (requestObject.method.equalsIgnoreCase(MESSAGE_METHOD_NAME_TRANSFER)) {
	// Transaction transaction = branchServer.addTransferedFund(requestObject);
	// if (transaction == null) {
	// requestObject.method = MESSAGE_METHOD_ERROR;
	// requestObject.errMessage = "Could not find destination Account";
	// BranchImpl.logger.println("Transaction: " + requestObject.transactionId
	// + ", Unable to transfer fund to non existant account: " +
	// requestObject.clientId);
	// }
	// new Thread() {
	// public void run() {
	// DatagramPacket reply = buildDatagramPacket(request.getSocketAddress(),
	// requestObject);
	// while (true) {
	// try {
	// DatagramSocket socket = new DatagramSocket();
	// socket.send(reply);
	// } catch (SocketException e) {
	// e.printStackTrace();
	// continue;
	// } catch (IOException e) {
	// e.printStackTrace();
	// continue;
	// }
	// if (transaction != null) {
	// transaction.commit();
	// }
	// break;
	// }
	// }
	// }.start();
	//
	// } else if
	// (requestObject.method.equalsIgnoreCase(MESSAGE_METHOD_TRANSFER_ACK)) {
	// // TODO remove transaction from map
	// }
	//
	// }
	// }

}
