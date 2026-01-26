package network;

import config.ConfigParser;
import config.DeviceConfig;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class Switch extends NetworkDevice {
	private final List<SwitchTableEntry> switchTable;
	private final Map<String, String> virtualPorts;

	private Switch(String[] args) {
		super(args);
		switchTable = new ArrayList<>();
		virtualPorts = new HashMap<>();
		populateVirtualPorts();
	}

	private void populateVirtualPorts() {
		for (String neighborID : myConfig.neighbors()) {
			DeviceConfig neighborConfig = ConfigParser.getConfigForDevice(neighborID);

			String virtualPort = String.format(
					"%s:%s", neighborConfig.ipAddress(), neighborConfig.port());

			virtualPorts.put(neighborID, virtualPort);
		}
	}

	private void addTableEntry(String deviceID) {
		switchTable.add(new SwitchTableEntry(deviceID, virtualPorts.get(deviceID)));
		System.out.printf("%s%n", switchTable);
	}

	@SuppressWarnings("FieldCanBeLocal")
	private static class SwitchTableEntry {
		private final String deviceID;
		private final String switchPort;
		private LocalTime time;

		public SwitchTableEntry(String deviceID, String switchPort) {
			this.deviceID = deviceID;
			this.switchPort = switchPort;
			refresh();
		}

		private void refresh() {
			time = LocalTime.now();
		}
	}
}