package network;

import config.ConfigParser;
import config.DeviceConfig;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/// A superclass with code shared between `Switch`es and `Host`s.
///
/// @author KxtR-27 (Kat)
/// @see Host
/// @see Switch
@SuppressWarnings("unused")
abstract class NetworkDevice implements AutoCloseable {
	/// The ID provided as a command-line argument.
	final String id;

	/// The matching device config, including port, IP address, and neighbors.
	protected DeviceConfig myConfig;

	/// The socket that the device uses to send and receive messages.
	protected DatagramSocket socket;

	protected NetworkDevice(String[] args) throws SocketException {
		id = validateArgs(args);
		myConfig = validateMyConfig();
		socket = new DatagramSocket();
	}

	private String validateArgs(String[] args) {
		if (args.length != 1) {
			System.out.printf("Usage: java %s <ID>%n", this.getClass().getSimpleName());
			System.exit(-1);
		}

		return args[0];
	}

	private DeviceConfig validateMyConfig() {
		DeviceConfig config = ConfigParser.getConfigForDevice(id);

		if (config == null) {
			System.out.printf("ID not registered in config.json file.%n");
			System.exit(-1);
		}

		return config;
	}

	protected void sendMessage(MessageFrame messageFrame, DeviceConfig recipient) throws IOException {
		DatagramPacket messagePacket = messageFrame.toPacket(recipient);
		socket.send(messagePacket);
	}

	protected MessageFrame receiveMessage() throws IOException {
		DatagramPacket messagePacket = new DatagramPacket(new byte[1024], 1024);
		socket.receive(messagePacket);

		return MessageFrame.fromPacket(messagePacket);
	}

	@Override
	public void close() {
		socket.close();
	}
}
