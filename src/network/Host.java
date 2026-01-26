package network;

import config.ConfigParser;
import config.DeviceConfig;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

@SuppressWarnings("unused")
public class Host extends NetworkDevice {
	private final Scanner consoleScanner;
	private final DeviceConfig neighboringSwitch;

	private Host(String[] args) throws SocketException {
		super(args);
		consoleScanner = new Scanner(System.in);
		neighboringSwitch = ConfigParser.getConfigForDevice(myConfig.neighbors()[0]);
	}

	private MessageFrame scanMessage() {
		System.out.printf("Please enter a short message:%n>> ");
		String message = consoleScanner.nextLine();

		System.out.printf("Please enter the ID of the host to send to:%n>> ");
		String destinationID = consoleScanner.nextLine();

		return new MessageFrame(this.id, destinationID, message);
	}

	private void sendMessage(MessageFrame messageFrame) throws IOException {
		byte[] messageBytes = messageFrame.toString().getBytes();
		int messageLength = messageBytes.length;

		InetAddress destinationIP = InetAddress.getByName(neighboringSwitch.ipAddress());

		DatagramPacket messagePacket = new DatagramPacket(
				messageBytes,
				messageLength,
				destinationIP,
				neighboringSwitch.port()
		);
		socket.send(messagePacket);
	}

	private static class MessageFrame {
		String sourceID;
		String destinationID;
		String message;

		public MessageFrame(String sourceID, String destinationID, String message) {
			this.sourceID = sourceID;
			this.destinationID = destinationID;
			this.message = message;
		}

		@Override
		public String toString() {
			return String.format("%s:%s:%s", sourceID, destinationID, message);
		}
	}

	static void main() {

	}
}
