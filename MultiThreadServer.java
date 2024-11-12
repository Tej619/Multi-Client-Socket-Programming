import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author Tejas Vaity **/
public class MultiThreadServer {
	private static final int PORT = 3811;
	private static final HashMap<String, String> users = new HashMap<>();
	private static final HashMap<String, String> activeUsers = new HashMap<>();
	private static final Map<Integer, String[]> addressBook = new HashMap<>();
	public static final String ADDRESS_BOOK = "address.txt";
	public static final int MAX_RECORDS = 20;
	private static final List<Socket> clientSockets = new ArrayList<>();
	private static volatile boolean isRunning = true; // Flag to control server shutdown
	private static ServerSocket serverSocket;

	public static void main(String[] args) {
		// Initializing users
		users.put("root", "root05");
		users.put("john", "john06");
		users.put("david", "david07");
		users.put("mary", "mary08");
		loadAddressBook(); //Loading existing records from address Book

		try {
			serverSocket = new ServerSocket(PORT);
			System.out.println("Server started on port " + PORT);

			while (isRunning) {
				Socket clientSocket = serverSocket.accept(); // Accepting new client connections
				clientSockets.add(clientSocket);
				System.out.println("New client connected: " + clientSocket.getInetAddress());
				new ChildThread(clientSocket, users, activeUsers, addressBook, clientSockets).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			shutdownCleanup(); // To close all the client connections
			try {
				if (serverSocket != null && !serverSocket.isClosed()) {
					serverSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Server shutting down...");
		}
	}

	// This method will be called when a SHUTDOWN command is received
	public static void stopServer() {
		isRunning = false; // Set flag to false to stop accepting new connections
		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close(); // Close the server socket to stop accepting new connections
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void shutdownCleanup() {
		for (Socket clientSocket : clientSockets) { 
			try {
				clientSocket.close(); //Closing all the client connections
			} catch (IOException e) {
				System.err.println("Error closing client socket: " + e.getMessage());
			}
		}
	}

	private static synchronized void loadAddressBook() {
		// Load the records from the file (if it exists)
		File addressFile = new File(ADDRESS_BOOK); // Creates new file if file does not exist
		try (BufferedReader reader = new BufferedReader(new FileReader(addressFile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				int id = Integer.parseInt(parts[0]);
				addressBook.put(id, new String[] { parts[1], parts[2], parts[3] });
			}
		} catch (IOException e) {
			System.out.println("No address book file found, starting fresh.");
		}
	}

}
