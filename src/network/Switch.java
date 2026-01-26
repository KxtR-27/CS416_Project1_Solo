package network;

import config.ConfigParser;
import config.DeviceConfig;

import java.io.IOException;
import java.net.SocketException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
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

	private void transferMessage() throws IOException {
		// blocking call
		MessageFrame message = receiveMessage();
		System.out.printf("Received message: \"%s\"%n", message);

		if (!inTable(message.sourceID)) {
			addTableEntry(message.sourceID);
			printSwitchTable();
		}

		if (inTable(message.destinationID))
			sendMessage(message, message.destinationID);
		else
			floodMessage(message);
	}

	private void floodMessage(MessageFrame message) throws IOException {
		for (String neighbor : myConfig.neighbors())
			if (!neighbor.equals(message.sourceID))
				sendMessage(message, neighbor);
	}

	private boolean inTable(String deviceID) {
		return switchTable.containsKey(deviceID);
	}

	private void addTableEntry(String deviceID) {
		if (switchTable.containsKey(deviceID))
			switchTable.get(deviceID).refresh();
		else
			switchTable.put(deviceID, new SwitchTableEntry(virtualPorts.get(deviceID)));

		System.out.printf("%s%n", switchTable);
	}

	private void printSwitchTable() {
		System.out.printf("Device ID | %15s | Time", "Virtual Port%n");

		switchTable.forEach((deviceID, entry) ->
				System.out.printf("%9s | %s%n", deviceID, entry));
	}

	@Override
	protected void onOpen() throws IOException {
		// by design, the loop can only be manually interrupted
		//noinspection InfiniteLoopStatement
		while (true)
			transferMessage();
	}

	@Override
	protected void onClose() {

	}

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
}