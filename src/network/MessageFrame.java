package network;

import config.DeviceConfig;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/// Stores the three values for a frame: the source, the destination, and the message.
/// Capable of converting to and from a datagram packet.
public record MessageFrame(
		String sourceID,
		String destinationID,
		String message
) {
	/// Attempts to reconstruct a packet into a MessageFrame.<br>
	/// **Will not work with packets that don't match the format.**
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

	/// A message converts itself into a packet.
	/// Since DatagramPackets need to specify a target,
	/// a recipient is passed into the converter for this purpose.
	///
	/// @param nextRecipient The target for the message.
	///                      Its IP and port are used to construct the packet.
	public DatagramPacket toPacketFor(DeviceConfig nextRecipient) throws UnknownHostException {
		byte[] messageBytes = this.toString().getBytes();
		int messageLength = messageBytes.length;

		InetAddress recipientAddress = InetAddress.getByName(nextRecipient.ipAddress());

		return new DatagramPacket(
				messageBytes,
				messageLength,
				recipientAddress,
				nextRecipient.port()
		);
	}

	@Override
	public String toString() {
		return String.format("%s:%s:%s", sourceID, destinationID, message);
	}
}
