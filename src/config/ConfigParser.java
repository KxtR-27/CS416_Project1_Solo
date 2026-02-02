package config;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// Parses and retrieves device configurations from a `config.json` file in the same directory.
///
/// **Unless you are a developer, you should <u>_only_</u> need to use:**
/// - `ConfigParser.getConfigForDevice()` - get device port, IP, and neighbors
/// - `ConfigParser.previousRecipient()` - a clearer way for intermediary
///   switches to know who NOT to flood (looking at you, S2)
///
/// @author KxtR-27 (Kat)
/// @see #getConfigForDevice(String)
/// @see #previousRecipient(String, String)
/// @see DeviceConfig
public class ConfigParser {
	private static final Gson GSON = new Gson();

	private static TopologyGraph topology;

	private static final Map<String, DeviceConfig> devices = new HashMap<>(7);

	public static DeviceConfig getConfigForDevice(String id) {
		updateConfigMap();
		return devices.get(id);
	}

	public static String previousRecipient(String sourceID, String myID) {
		updateConfigMap();

		List<String> path = topology.findShortestPathBetween(sourceID, myID);
		return path.get(path.size() - 2);
	}

	private static void updateConfigMap() {
		// can be null if error occurs
		ConfigSnapshot snapshot = loadConfigFile();

		if (snapshot == null)
			return;

		topology = new TopologyGraph(snapshot.links);

		devices.clear();
		snapshot.devices.forEach((id, rawConfig) -> devices.put(
				id, new DeviceConfig(
						rawConfig.port, rawConfig.ipAddress, topology.getAdjacentDevicesOf(id)
				)
		));
	}

	private static ConfigSnapshot loadConfigFile() {
		// try-with-resources automatically closes the readers after using them
		try (JsonReader reader = new JsonReader(new FileReader("src/config/config.json"))) {
			return GSON.fromJson(reader, ConfigSnapshot.class);
		}
		catch (Exception e) {
			printErrorWithMessage(e);
			return null;
		}
	}

	private static void printErrorWithMessage(Exception e) {
		String extraMessage = switch (e) {
			case JsonIOException _ -> "Unable to read config file.";
			case JsonSyntaxException _ -> "Could not correctly parse config file.";
			case FileNotFoundException _ -> "Config file not found.";
			case IOException _ -> "Unexpected file-related issue occurred.";

			default -> "Unhandled exception occurred.";
		};

		System.err.printf("%s%n", extraMessage);
	}

	private record ConfigSnapshot(
			Map<String, RawDeviceConfig> devices,
			Map<String, String> links
	) {
		@Override
		public String toString() {
			return String.format("%s%n%s", devices, links);
		}
	}

	private record RawDeviceConfig(
			String ipAddress,
			int port
	) {
	}

	static void main() {
		updateConfigMap();

		System.out.printf("%n%s%n", devices);
		System.out.printf("%s%n", topology);

		String sourceID = "A";
		String destinationID = "S3";

		System.out.printf(
				"%nPath from %s to %s:%n%s%n",
				sourceID, destinationID,
				topology.findShortestPathBetween(sourceID, destinationID)
		);
		System.out.printf(
				"Previous recipient (before %s) of message from %s:%n%s%n",
				sourceID, destinationID,
				previousRecipient(sourceID, destinationID)
		);
	}
}
