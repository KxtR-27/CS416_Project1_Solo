package network;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.Scanner;

@SuppressWarnings("unused")
public class Host extends NetworkDevice {
	Scanner consoleScanner;

	private Host(String[] args) {
		super(args);
		consoleScanner = new Scanner(System.in);
	}

	private String scanMessageFrame() {
		System.out.printf("Please enter a short message:%n>> ");
		String message = consoleScanner.nextLine();

		System.out.printf("Please enter the ID of the host to send to:%n>> ");
		String destinationID = consoleScanner.nextLine();

		return String.format("%s:%s:%s", this.id, destinationID, message);
	}

	private byte[] getMessageBytes(String messageFrame) {
		String[] frameData = messageFrame.split(":");
		String sourceID = frameData[0];
		String destinationID = frameData[1];
		String message = frameData[2];

		DatagramPacket messagePacket = new DatagramPacket(new byte[1024], 1024);
		return Arrays.copyOf(messagePacket.getData(), messagePacket.getLength());
	}

	static void main() {

	}
}
