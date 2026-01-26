package config;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/// Parses and retrieves device configurations from a `config.json` file in the same directory.
///
/// **Unless you are a developer, you should <u>_only_</u> need to use the static
/// `getConfigForDevice()` method.**
///
/// @author KxtR-27 (Kat)
/// @see #getConfigForDevice(String)
/// @see DeviceConfig
@SuppressWarnings("unused" /* because it's used later based on the rubric. */)
public class ConfigParser {
	/// The Gson object used in conjunction with a JsonReader to read data from the `config.json` file.
	private static final Gson GSON = new Gson();

	/// A parsed map of keys (device IDs) and values (`DeviceConfig`s)
	/// that serves as a functional representation of the JSON structure itself.
	private static final Map<String, DeviceConfig> CONFIGS_MAP = new HashMap<>(7);

	/// Gets the configuration information for a virtual network device.
	///
	/// Calling this method also updates the parsed configuration,
	/// so you can call this multiple times to pull potential changes to the config file.
	///
	/// @param id ID/"MAC" for a host or switch device in the virtual network, (ex. "S1" or "A")
	///
	/// @return a `DeviceConfig` object with the device's port, IP address, and neighbors,
	/// 				_or **null** if no configuration exists for the ID used.<br>
	/// 				<sup>If this method returns null, please ensure that the ID used
	/// 				exists in the `config.json` file.</sup>_
	///
	/// @see DeviceConfig
	@SuppressWarnings("unused" /* because it's used later on based upon the rubric.*/)
	public static DeviceConfig getConfigForDevice(String id) {
		updateConfigMap();
		return CONFIGS_MAP.get(id);
	}

	/// Parses config file information into entries which are put into the config map.
	///
	/// @see #loadConfigFile()
	/// @see RawDeviceConfig
	private static void updateConfigMap() {
		// can be null if error occurs
		RawDeviceConfig[] rawConfigs = loadConfigFile();

		// do not update if error occurred
		if (rawConfigs == null)
			return;

		for (RawDeviceConfig rawConfig : rawConfigs)
			CONFIGS_MAP.put(
					rawConfig.id, new DeviceConfig(
							rawConfig.port,
							rawConfig.ipAddress,
							rawConfig.neighbors
					)
			);
	}

	/// Converts the `config.json` file into an effectively identical array of objects.
	///
	/// @return an array of `RawDeviceConfig` objects
	///
	/// @see RawDeviceConfig
	private static RawDeviceConfig[] loadConfigFile() {
		// try-with-resources automatically closes the readers after using them
		try (JsonReader reader = new JsonReader(new FileReader("src/config/config.json"))) {
			return GSON.fromJson(reader, RawDeviceConfig[].class);
		}
		catch (Exception e) {
			printErrorWithMessage(e);
			return null;
		}
	}

	/// Prints the error with an additional help message.
	///
	/// @param e the caught exception to map to a message and print
	///
	/// @see #loadConfigFile()
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

	/// Used internally to map the `config.json` file's contents to an object of effectively identical form.
	/// A `RawDeviceConfig` is later used to create a map entry for a parsed configuration structure.
	///
	/// For usable information, use a `DeviceConfig` record instead.
	///
	/// @param id        The ID/"MAC" of a host or switch device
	/// @param port      The port on which the host or switch operates
	/// @param ipAddress The IP address on which the host or switch operates
	/// @param neighbors The "linked" host and switches of this device in the topology
	///
	/// @see #loadConfigFile()
	/// @see DeviceConfig
	private record RawDeviceConfig(
			String id,
			int port,
			String ipAddress,
			String[] neighbors
	) {
	}
}
