package dbs.branches.comm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class UDPHelper {
	public static final int MESSAGE_MAX_SIZE = 10240; // 10kbytes
	public static final int RETRY_COUNT = 10;
	public static final String REPLY_ACTION = "EmptyReply";
	public static final String ACK_ACTION = "ACK";

	private UDPHelper() {

	}

	public static DatagramPacket buildDatagramPacket(Message message) {
		InetSocketAddress address = new InetSocketAddress(message.getHeader().destinationAddress,
				message.getHeader().destinationPort);
		DatagramPacket packet = buildDatagramPacket(message, address);
		return packet;
	}

	private static DatagramPacket buildDatagramPacket(Message message, SocketAddress address) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objOutput;
		try {
			objOutput = new ObjectOutputStream(new BufferedOutputStream(byteStream));
			objOutput.writeObject(message);
			objOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		byte[] byteMessage = byteStream.toByteArray();

		DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, address);
		return packet;
	}

	public static Message decodeDatagramPacket(DatagramPacket packet) {
		Message messageObject = null;
		try {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData());
			ObjectInputStream objInput = new ObjectInputStream(new BufferedInputStream(byteStream));
			messageObject = (Message) objInput.readObject();
			objInput.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return messageObject;
	}

	public static DatagramPacket buildReplyPacket(Message originMessage, SocketAddress destination) {
		MessageHeader originHeader = originMessage.getHeader();

		MessageHeader replyHeader = new MessageHeader();
		replyHeader.messageId = originHeader.messageId;
		replyHeader.isReply = true;
		replyHeader.isAck = false;

		Message ackMessage = new Message(REPLY_ACTION, replyHeader, null);

		return buildDatagramPacket(ackMessage, destination);
	}

	public static DatagramPacket buildAckDatagramPacket(Message originMessage, SocketAddress destination) {
		MessageHeader originHeader = originMessage.getHeader();

		MessageHeader replyHeader = new MessageHeader();
		replyHeader.messageId = originHeader.messageId;
		replyHeader.isReply = false;
		replyHeader.isAck = true;

		Message ackMessage = new Message(ACK_ACTION, replyHeader, null);

		return buildDatagramPacket(ackMessage, destination);
	}

}
