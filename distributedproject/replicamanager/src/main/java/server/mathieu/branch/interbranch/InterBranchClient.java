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
import java.net.InetSocketAddress;
import java.net.SocketException;

import server.mathieu.branch.BranchImpl;
import server.mathieu.branch.Transaction;

public class InterBranchClient extends Thread {
	private static final String MESSAGE_METHOD_ERROR = "ERROR";
	private static final String MESSAGE_METHOD_NAME_TRANSFER = "transfer";
	private static final String MESSAGE_METHOD_TRANSFER_ACK = "transferack";
	private static final int MESSAGE_MAX_SIZE = 10240; // 10kbytes

	private static int RETRY_COUNT = 5;

	private DatagramSocket socket;
	private DatagramPacket request;
	private Transaction transaction;
	private BranchLocation location;

	private InterBranchClient(DatagramSocket socket, DatagramPacket request, Transaction transaction,
			BranchLocation destination) {
		this.socket = socket;
		this.request = request;
		this.transaction = transaction;
		this.location = destination;
	}

	public static void transferFund(BranchLocation destination, Transaction localTransaction,
			String destinationClientId, double amount) {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			BranchImpl.logger.println("Unable to create a socket to transfer funds");
			e.printStackTrace();
			return;
		}
		DatagramPacket requestPacket = buildTransferPacket(destination, localTransaction.getTransactionId(),
				destinationClientId, amount);

		InterBranchClient client = new InterBranchClient(socket, requestPacket, localTransaction, destination);

		client.start();
	}

	private static DatagramPacket buildTransferPacket(BranchLocation destination, String transactionId, String clientId,
			double amount) {
		InterBranchPacket packetObject = new InterBranchPacket(MESSAGE_METHOD_NAME_TRANSFER, transactionId, clientId,
				amount);

		DatagramPacket packet = buildDatagramPacket(destination, packetObject);
		return packet;
	}

	private static DatagramPacket buildAckPacket(BranchLocation destination, String transactionId) {
		InterBranchPacket packetObject = new InterBranchPacket(MESSAGE_METHOD_TRANSFER_ACK, transactionId);

		DatagramPacket packet = buildDatagramPacket(destination, packetObject);
		return packet;
	}

	private static DatagramPacket buildDatagramPacket(BranchLocation destination, InterBranchPacket packetObject) {
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

		DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length,
				new InetSocketAddress(destination.location, destination.port));
		return packet;
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

	public void run() {
		int retryCount = 0;
		boolean transmissionSuccess = false;
		boolean transmissionCancelled = false;
		while (!transmissionSuccess && !transmissionCancelled) {
			if (retryCount++ >= 5) {
				BranchImpl.logger
						.println("Retry count exceeded to complete transaction " + transaction.getTransactionId());
				return;
			}
			try {
				socket.send(request);
				byte[] replyBuffer = new byte[MESSAGE_MAX_SIZE];
				DatagramPacket reply = new DatagramPacket(replyBuffer, replyBuffer.length);
				socket.receive(reply);

				InterBranchPacket replyPacketObject = decodeDatagramPacket(reply);

				if (replyPacketObject.transactionId.equalsIgnoreCase(transaction.getTransactionId())) {
					DatagramPacket ackPacket = buildAckPacket(location, replyPacketObject.transactionId);
					socket.send(ackPacket);
					if (replyPacketObject.method.equalsIgnoreCase(MESSAGE_METHOD_ERROR)) {
						BranchImpl.logger.println("Transaction: " + replyPacketObject.transactionId
								+ ", Error transferring fund, aborting: " + replyPacketObject.errMessage);
						transmissionCancelled = transaction.cancel();
					} else {
						transmissionSuccess = transaction.commit();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
