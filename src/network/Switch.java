package network;

import config.ConfigParser;
import config.DeviceConfig;

import java.io.IOException;
import java.net.SocketException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Switch extends NetworkDevice {
	/// A switch table that logs each source ID, virtual port, and time updated into a table form.
	private final Map<String, SwitchTableEntry> switchTable;

	/// A map of virtual switch ports for each logged ID.
	private final Map<String, String> virtualPorts;

	private Switch(String[] args) throws SocketException {
		super(args);
		switchTable = new HashMap<>();
		virtualPorts = new HashMap<>();
		configureVirtualPorts();
	}

	/// Sets up initial virtual ports for immediate neighbors
	private void configureVirtualPorts() {
		for (String neighborID : myConfig.neighbors()) {
			DeviceConfig neighbor = ConfigParser.getConfigForDevice(neighborID);

			String virtualPort = String.format(
					"%s:%s", neighbor.ipAddress(), neighbor.port());

			virtualPorts.put(neighborID, virtualPort);
		}
	}

	/// Adds/updates the source in the table,
	/// then sends the message if the table knows the destination, otherwise it floods.
	private void transferMessage(MessageFrame message) throws IOException {
		if (!inTable(message.sourceID())) {
			addTableEntry(message.sourceID());
			printSwitchTable();
		}

		if (inTable(message.destinationID()))
			sendMessage(message, ConfigParser.nextRecipient(message.destinationID(), id));
		else
			floodMessage(message);
	}

	private boolean inTable(String deviceID) {
		return switchTable.containsKey(deviceID);
	}

	private void addTableEntry(String deviceID) {
		virtualPorts.putIfAbsent(deviceID, virtualPorts.get(ConfigParser.previousRecipient(deviceID, id)));
		switchTable.put(deviceID, new SwitchTableEntry(virtualPorts.get(deviceID)));
	}

	/// Prints the formatted switch table in its current state
	private void printSwitchTable() {
		System.out.printf("Switch %s:%n", id);
		System.out.printf("%s%n", "-".repeat(52));
		System.out.printf("Device ID | %-21s | Time%n", "Virtual Port");

		switchTable.forEach((deviceID, entry) ->
				System.out.printf("%-9s | %s%n", deviceID, entry));

		System.out.printf("%n");
	}

	/// When the source of the message is not in the table,
	/// flood it to all neighbors except the neighbor who sent it
	private void floodMessage(MessageFrame message) throws IOException {
		String previousRecipient = ConfigParser.previousRecipient(message.sourceID(), id);

		for (String neighbor : myConfig.neighbors())
			if (!neighbor.equals(previousRecipient))
				sendMessage(message, neighbor);
	}

	/// Initiates the receive + transfer loop
	@Override
	protected void onOpen() throws IOException {
		// the loop is intentionally broken manually by interrupting the program
		//noinspection InfiniteLoopStatement
		while (true) {
			MessageFrame message = receiveMessage();
			transferMessage(message);
		}
	}

	/// The switch is interrupted manually, therefore closing behavior is redundant.
	@Override
	protected void onClose() {}

	/// A value for the Switch Table map containing a virtual port
	/// and the time at which it was last used
	private static class SwitchTableEntry {
		private static final DateTimeFormatter timeFormatter =
				DateTimeFormatter.ISO_LOCAL_TIME;

		private final String switchPort;
		private LocalTime time;

		public SwitchTableEntry(String switchPort) {
			this.switchPort = switchPort;
			refresh();
		}

		private void refresh() {
			time = LocalTime.now();
		}

		@Override
		public String toString() {
			return String.format("%-21s | %s", switchPort, time.format(timeFormatter));
		}
	}

	static void main(String[] args) {
		try (Switch newSwitch = new Switch(args)) {
			newSwitch.open();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}