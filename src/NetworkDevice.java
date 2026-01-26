import config.ConfigParser;
import config.DeviceConfig;

public abstract class NetworkDevice {
	/// The ID provided as a command-line argument.
	private final String id;

	/// The matching device config, including
	/// port, IP address, and neighbors.
	private DeviceConfig myConfig;

	protected NetworkDevice(String[] args) {
		id = validateArgs(args);
		myConfig = validateMyConfig();
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
}
