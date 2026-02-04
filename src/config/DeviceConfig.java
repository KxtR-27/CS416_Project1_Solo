package config;

/// Stores configuration information for network devices except for the device ID.
/// Device IDs are passed into and handled by the `ConfigParser`.
///
/// _As this is a record class, fields are called as methods of the same name.
/// To access, for example, the port of a `DeviceConfig`, use `myDeviceConfig.port()` with parentheses._
///
/// @param port      The port on which the host or switch operates
/// @param ipAddress The IP address on which the host or switch operates
/// @param neighbors A string array of neighboring devices' IDs
///
/// @author KxtR-27 (Kat)
/// @see ConfigParser
public record DeviceConfig(
		int port,
		String ipAddress,
		String[] neighbors
) {}
