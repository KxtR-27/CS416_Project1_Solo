package network;

import config.ConfigParser;
import config.DeviceConfig;

import java.io.IOException;
import java.net.*;

/// A superclass with code shared between `Switch`es and `Host`s.
/// Sends and receives UDP packets across a VLAN topology as specified in `config.json`.
///
/// @author KxtR-27 (Kat)
/// @see Host
/// @see Switch
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

		socket = new DatagramSocket(myConfig.port());
	}

	/// Validates command-line argument(s)
	private String validateArgs(String[] args) {
		if (args.length != 1) {
			System.out.printf("Usage: java %s <ID>%n", this.getClass().getSimpleName());
			System.exit(-1);
		}

		return args[0];
	}

	/// Verifies that a configuration exists for a device with this ID
	private DeviceConfig validateMyConfig() {
		DeviceConfig config = ConfigParser.getConfigForDevice(id);

		if (config == null) {
			System.out.printf("ID not registered in config.json file.%n");
			System.exit(-1);
		}

		return config;
	}

	/// Sends a given messageFrame packet to a given recipient
	protected void sendMessage(MessageFrame messageFrame, String recipientID) throws IOException {
		DeviceConfig recipient = ConfigParser.getConfigForDevice(recipientID);
		DatagramPacket messagePacket = messageFrame.toPacketFor(recipient);
		socket.send(messagePacket);
	}

	/// Waits and receives a packet, then converts it to an actual messageFrame
	protected MessageFrame receiveMessage() throws IOException {
		DatagramPacket messagePacket = new DatagramPacket(new byte[1024], 1024);
		socket.receive(messagePacket);

		return MessageFrame.fromPacket(messagePacket);
	}

	/// Subclasses must specify opening behavior
	protected abstract void onOpen() throws IOException;

	/// Subclasses must specify closing behavior
	/// (even though it's not currently implemented)
	protected abstract void onClose();

	/// Invokes onOpen() so that subclasses don't have to
	public void open() throws IOException {
		onOpen();
	}

	/// Invokes onClose() (and closes the socket)
	/// so that subclasses don't have to
	@Override
	public void close() {
		socket.close();
		onClose();
	}

}
