package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/// Scans messages from the console and sends/receives them from switches.
public class Host extends NetworkDevice {
	/// Scans messages from the console
	private final Scanner consoleScanner;

	/// Manages receiving thread
	private final ExecutorService executor;

	/// Once false, stops the loop and closes the host
	private boolean running = true;

	/// @param args the command-line args from main(), which should only be an ID
	private Host(String[] args) throws SocketException {
		super(args);
		consoleScanner = new Scanner(System.in);
		executor = Executors.newSingleThreadExecutor();
	}

	/// Scans and parses a message from the command-line and parses it into a `MessageFrame`
	private MessageFrame scanMessage() {
		System.out.printf("Please enter a short message:%n>> ");
		String message = consoleScanner.nextLine();

		System.out.printf("Please enter the ID of the host to send to:%n>> ");
		String destinationID = consoleScanner.nextLine();

		System.out.printf("Message sent!%n%n");
		return new MessageFrame(this.id, destinationID, message);
	}

	/// Creates a concurrent packet listener and
	/// initiates the scan + send loop
	@Override
	protected void onOpen() throws IOException {
		executor.submit(new ReceiverTask());

		while (running) {
			MessageFrame message = scanMessage();
			sendMessage(message, myConfig.neighbors()[0]);
		}
	}

	/// Stops the listener task and closes the Host
	@Override
	protected void onClose() {
		consoleScanner.close();
		executor.shutdown();
		running = false;
	}

	/// On a separate thread, constantly wait to receive message packets
	private class ReceiverTask implements Runnable {
		@Override
		public void run() {
			// the loop is intentionally broken manually by interrupting the program
			//noinspection InfiniteLoopStatement
			while (true) {
				try {
					DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
					socket.receive(packet);
					MessageFrame messageFrame = MessageFrame.fromPacket(packet);


					if (!messageFrame.destinationID().equals(id))
						System.out.printf(
								"%nMAC address mismatch (destination MAC: %s | my MAC: %s)%n>> ",
								messageFrame.destinationID(), id
						);
					else
						System.out.printf("%nReceived message:%n%s%n>> ", messageFrame);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	static void main(String[] args) {
		try (Host host = new Host(args)) {
			host.open();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}