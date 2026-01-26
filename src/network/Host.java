package network;

import config.ConfigParser;
import config.DeviceConfig;

import java.net.SocketException;
import java.util.Scanner;

@SuppressWarnings("unused")
public class Host extends NetworkDevice {
	private final Scanner consoleScanner;

	@SuppressWarnings("FieldCanBeLocal")
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

	@Override
	public void close() {
		super.close();
		consoleScanner.close();
	}
}
