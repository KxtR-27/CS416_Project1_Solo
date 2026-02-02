package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Host extends NetworkDevice {
	private final Scanner consoleScanner;
	private final ExecutorService executor;
	private boolean running = true;

	private Host(String[] args) throws SocketException {
		super(args);
		consoleScanner = new Scanner(System.in);
		executor = Executors.newSingleThreadExecutor();
	}

	private MessageFrame scanMessage() {
		System.out.printf("Please enter a short message:%n>> ");
		String message = consoleScanner.nextLine();

		System.out.printf("Please enter the ID of the host to send to:%n>> ");
		String destinationID = consoleScanner.nextLine();

		System.out.printf("Message sent!%n%n");
		return new MessageFrame(this.id, destinationID, message);
	}

	@Override
	protected void onOpen() throws IOException {
		executor.submit(new ReceiverTask());

		while (running) {
			MessageFrame message = scanMessage();
			sendMessage(message, myConfig.neighbors()[0]);
		}
	}

	@Override
	protected void onClose() {
		consoleScanner.close();
		executor.shutdown();
		running = false;
	}

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


					if (!messageFrame.destinationID.equals(id))
						System.out.printf(
								"%nMAC address mismatch (destination MAC: %s | my MAC: %s)%n>> ",
								messageFrame.destinationID, id
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