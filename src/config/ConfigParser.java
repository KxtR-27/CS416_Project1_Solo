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
/// - `ConfigParser.getConfigForDevice()` - get device port, IP, and neighbors in a `DeviceConfig`
/// - `ConfigParser.previousRecipient()` - a clearer way for intermediary
///   switches to know who NOT to flood (looking at you, S2)
/// - `ConfigParser.nextRecipient` - a clear way to know who to transfer the message to.
///   For some reason, messages got lost in translation before this method.
///
/// @author KxtR-27 (Kat)
/// @see #getConfigForDevice(String)
/// @see #previousRecipient(String, String)
/// @see #nextRecipient(String, String)
/// @see DeviceConfig
public class ConfigParser {
	/// GSON object used to parse the `config.json` file.
	private static final Gson GSON = new Gson();

	/// A custom graph structure capable of getting neighbors and pathfinding.
	/// Constructed from the `config.json` file.
	private static TopologyGraph topology;

	/// The configurations for every device in the `config.json` file.
	private static final Map<String, DeviceConfig> devices = new HashMap<>(7);

	/// Returns a record containing the port, IP address, and neighbors of a given device by its ID.
	public static DeviceConfig getConfigForDevice(String id) {
		updateConfigMap();
		return devices.get(id);
	}

	/// Uses the topology to trace a path from the source device to "my" device
	/// and returns the neighbor that received the message just before.
	public static String previousRecipient(String sourceID, String myID) {
		updateConfigMap();

		List<String> path = topology.findShortestPathBetween(sourceID, myID);
		return path.get(path.size() - 2);
	}

	/// Uses the topology to trace a path from the destination device to "my" device
	/// and returns the neighbor that should receive the message next to reach the destination.
	public static String nextRecipient(String destinationID, String myID) {
		updateConfigMap();

		List<String> path = topology.findShortestPathBetween(destinationID, myID);
		return path.get(path.size() - 2);
	}

	/// Reloads/reparses the `config.json` file just in case it was changed during runtime.
	private static void updateConfigMap() {
		// can be null if error occurs
		ConfigSnapshot snapshot = loadConfigFile();

		if (snapshot == null)
			return;

		topology = new TopologyGraph(snapshot.links);

		devices.clear();
		snapshot.devices.forEach((id, rawConfig) -> devices.put(
				id, new DeviceConfig(
						rawConfig.port,
						rawConfig.ipAddress,
						topology.getAdjacentDevicesOf(id)
				)
		));
	}

	/// Uses GSON to convert the `config.json` file to
	/// a map of devices
	/// and a map of topological edges for the graph.
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

	/// Should an error occur in `#loadConfigFile`,
	/// print a helpful message along with the error.
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

	/// Harbors an effectively identical structure to that of `config.json`
	/// to easily convert the json data to an object.
	private record ConfigSnapshot(
			Map<String, RawDeviceConfig> devices,
			Map<String, String> links
	) {
	}

	/// Harbors an effectively identical structure to that of the device values in the `config.json` file.
	private record RawDeviceConfig(
			String ipAddress,
			int port
	) {
	}

	/// Test driver
	static void main() {
		updateConfigMap();
		System.out.printf("%n%s%n", devices);
		System.out.printf("%s%n", topology);

		String sourceID = "A";
		String destinationID = "D";

		System.out.printf(
				"%nPath from %s to %s:%n%s%n", sourceID, destinationID,
				topology.findShortestPathBetween(sourceID, destinationID)
		);
		System.out.printf(
				"Path from %s to %s:%n%s%n", destinationID, sourceID,
				topology.findShortestPathBetween(destinationID, sourceID)
		);

		System.out.printf(
				"%nPrevious recipient (before %s) of message from %s:%n%s%n",
				destinationID, sourceID, previousRecipient(sourceID, destinationID)
		);
		System.out.printf(
				"Next recipient (after %s) of message to %s:%n%s%n",
				sourceID, destinationID, nextRecipient(destinationID, sourceID)
		);

		System.out.printf("%nRecipient line:%n");
		String currentLocation = destinationID;

		try {
			while ((currentLocation = previousRecipient(sourceID, currentLocation)) != null)
				System.out.printf("%s%n", currentLocation);
		}
		catch (IndexOutOfBoundsException _) {}
		finally {
			System.out.printf("End of the line%n");
		}
	}
}
