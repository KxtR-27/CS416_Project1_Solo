package config;

import java.util.Arrays;

/// Stores configuration information for network devices except for the device ID.
/// Device IDs are passed into and handled by the `ConfigParser`.
///
/// _As this is a record class, fields are called as methods of the same name.
/// To access, for example, the port of a `DeviceConfig`, use `myDeviceConfig.port()` with parentheses._
///
/// @param port      The port on which the host or switch operates
/// @param ipAddress The IP address on which the host or switch operates
/// @param neighbors The "linked" host and switches of this device in the topology
///
/// @author KxtR-27 (Kat)
/// @see ConfigParser
public record DeviceConfig(
		int port,
		String ipAddress,
		String[] neighbors
) {
	@Override
	public String toString() {
		return String.format(
				"%s:%s | neighbors: %s",
				ipAddress, port, Arrays.toString(neighbors)
		);
	}
}
