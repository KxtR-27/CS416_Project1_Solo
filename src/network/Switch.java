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
	private final Map<String, SwitchTableEntry> switchTable;
	private final Map<String, String> virtualPorts;

	private Switch(String[] args) throws SocketException {
		super(args);
		switchTable = new HashMap<>();
		virtualPorts = new HashMap<>();
		formatVirtualPorts();
	}

	private void formatVirtualPorts() {
		for (String neighborID : myConfig.neighbors()) {
			DeviceConfig neighbor = ConfigParser.getConfigForDevice(neighborID);

			String virtualPort = String.format(
					"%s:%s", neighbor.ipAddress(), neighbor.port());

			virtualPorts.put(neighborID, virtualPort);
		}
	}

	private void transferMessage(MessageFrame message) throws IOException {
		if (!inTable(message.sourceID)) {
			addTableEntry(message.sourceID);
			printSwitchTable();
		}

		if (inTable(message.destinationID))
			sendMessage(message, message.destinationID);
		else
			floodMessage(message);
	}

	private boolean inTable(String deviceID) {
		return switchTable.containsKey(deviceID);
	}

	private void addTableEntry(String deviceID) {
		switchTable.put(deviceID, new SwitchTableEntry(virtualPorts.get(deviceID)));
	}

	private void printSwitchTable() {
		System.out.printf("Switch %s:%n", id);
		System.out.printf("Device ID | %-15s | Time%n", "Virtual Port");

		switchTable.forEach((deviceID, entry) ->
				System.out.printf("%-9s | %s%n", deviceID, entry));
	}

	private void floodMessage(MessageFrame message) throws IOException {
		boolean fromMyNeighbor = false;
		boolean toMyNeighbor = false;

		for (String neighbor : myConfig.neighbors()) {
			if (neighbor.equals(message.sourceID))
				fromMyNeighbor = true;
			else if (neighbor.equals(message.destinationID))
				toMyNeighbor = true;
		}

		// FIXME: correct flood routing
		boolean couldFloodInfinitely = !fromMyNeighbor && !toMyNeighbor;

		if (couldFloodInfinitely)
			return;

		for (String neighbor : myConfig.neighbors())
			if (!message.sourceID.equals(neighbor))
				sendMessage(message, neighbor);
	}

	@Override
	protected void onOpen() throws IOException {
		// by design, the loop can only be manually interrupted
		//noinspection InfiniteLoopStatement
		while (true) {
			MessageFrame message = receiveMessage();
			transferMessage(message);
		}
	}

	@Override
	// The switch is interrupted manually, therefore
	// closing behavior is redundant.
	protected void onClose() {}

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
			return String.format("%15s | %s", switchPort, time.format(timeFormatter));
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