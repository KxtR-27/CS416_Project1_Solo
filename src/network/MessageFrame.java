package network;

import config.DeviceConfig;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class MessageFrame {
	String sourceID;
	String destinationID;
	String message;

	public static MessageFrame fromPacket(DatagramPacket messagePacket) {
		try {
			byte[] contents = Arrays.copyOf(messagePacket.getData(), messagePacket.getLength());
			String[] frameData = new String(contents).split(":");
			return new MessageFrame(frameData[0], frameData[1], frameData[2]);
		}
		catch (RuntimeException e) {
			throw new IllegalArgumentException("Attempted to convert a packet that is not a message.", e);
		}
	}

	public MessageFrame(String sourceID, String destinationID, String message) {
		this.sourceID = sourceID;
		this.destinationID = destinationID;
		this.message = message;
	}

	// recipient =/= destination
	public DatagramPacket toPacket(DeviceConfig recipient) throws UnknownHostException {
		byte[] messageBytes = this.toString().getBytes();
		int messageLength = messageBytes.length;

		InetAddress recipientAddress = InetAddress.getByName(recipient.ipAddress());

		return new DatagramPacket(
				messageBytes,
				messageLength,
				recipientAddress,
				recipient.port()
		);
	}

	@Override
	public String toString() {
		return String.format("%s:%s:%s", sourceID, destinationID, message);
	}
}
