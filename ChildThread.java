import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author Tejas Vaity **/
public class ChildThread extends Thread {
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private String currentUser = null;
	private HashMap<String, String> users;
	private HashMap<String, String> activeUsers;
	private Map<Integer, String[]> addressBook = new HashMap<>();
	public static final String ADDRESS_BOOK = "address.txt";
	public static final int MAX_RECORDS = 20;
	private List<Socket> clientSockets;

	public ChildThread(Socket socket, HashMap<String, String> users, HashMap<String, String> activeUsers,
			Map<Integer, String[]> addressBook, List<Socket> clientSockets) {
		this.socket = socket;
		this.users = users;
		this.activeUsers = activeUsers;
		this.addressBook = addressBook;
		this.clientSockets = clientSockets;
	}

	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			String request;
			while ((request = in.readLine()) != null) {
				System.out.println("Received command from Client: " + request);
				String[] parts = request.split(" ");
				String command = parts[0];
				switch (command) {
				case "ADD":
					addRecord(parts);
					break;
				case "DELETE":
					deleteRecord(parts);
					break;
				case "LIST":
					listRecords();
					break;
				case "SHUTDOWN":
					shutdownServer();
					break;
				case "QUIT":
					quitClient();
					break;
				case "LOGIN":
					userLogin(parts);
					break;
				case "LOGOUT":
					userLogout();
					break;
				case "WHO":
					who();
					break;
				case "LOOK":
					userLook(parts);
					break;
				case "UPDATE":
					updateRecords(parts);
					break;
				default:
					out.println("400 Unknown command");
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
				System.out.println("Client disconnected: " + socket.getInetAddress());
			} catch (IOException e) {
				System.err.println("Error closing socket: " + e.getMessage());
			}
		}
	}

	private void userLogin(String[] parts) {
		if (parts.length != 3) {
			out.println("400 Invalid LOGIN command");
			return;
		}
		String userID = parts[1];
		String password = parts[2];
		if (users.containsKey(userID) && users.get(userID).equals(password)) {
			currentUser = userID;
			activeUsers.put(userID, socket.getInetAddress().toString());
			out.println("200 OK");
			System.out.println("User logged in: " + userID);
		} else {
			out.println("410 Wrong UserID or Password");
			System.out.println("Login failed for user: " + userID);
		}
	}

	private void userLogout() {
		if (currentUser != null) {
			activeUsers.remove(currentUser);
			System.out.println("User logged out: " + currentUser); // Log logout
			currentUser = null;
			out.println("200 OK");
		} else {
			out.println("401 You are not logged in");
		}
	}

	private void who() {
		StringBuilder response = new StringBuilder("200 OK\nThe list of active users: ");
		for (String user : activeUsers.keySet()) {
			response.append("\n").append(user + "\t" + activeUsers.get(user));
		}
		out.println(response);
	}

	private void userLook(String[] parts) {
		if (parts.length < 3) {
			out.println("400 Missing search criteria");
			return;
		}
		int searchField = Integer.parseInt(parts[1]);
		String searchValue = parts[2];
		StringBuilder response = new StringBuilder();
		String count;
		int count1=0;
		for (Integer record : addressBook.keySet()) {
			if ((searchField == 1 && addressBook.get(record)[0].equalsIgnoreCase(searchValue))
					|| (searchField == 2 && addressBook.get(record)[1].equalsIgnoreCase(searchValue))
					|| (searchField == 3 && addressBook.get(record)[2].equals(searchValue))) {
				count1++;
				response.append(record + " " + addressBook.get(record)[0] + " " + addressBook.get(record)[1] + " "
						+ addressBook.get(record)[2]).append("\n");
			}
		}
		count = "Found " + count1 +" match \n";
		if (response.length() > 0) {
			out.println("200 OK\n" + count + response);
		} else {
			out.println("404 Your search did not match any records");
		}
	}

	private void updateRecords(String[] parts) {
		if (parts.length < 4) { 
			out.println("400 Invalid UPDATE command");
			return;
		}
		if (currentUser == null) {
			out.println("401 You are not currently logged in, login first");
			return;
		}
		int recordID = Integer.parseInt(parts[1]);
		int value = Integer.parseInt(parts[2]);

		if (addressBook.containsKey(recordID)) {
			switch (value) {
			case 1:
				String[] newRecord = { parts[3], addressBook.get(recordID)[1], addressBook.get(recordID)[2] };
				addressBook.put(recordID, newRecord);
				saveAddressBook();
				out.println("200 OK \nRecord " + recordID + " updated: ");
				out.println(recordID + " " + addressBook.get(recordID)[0] + " " + addressBook.get(recordID)[1] + " "
						+ addressBook.get(recordID)[2]);
				System.out.println("Updated record: " + recordID);
				break;
			case 2:
				String[] newRecord1 = { addressBook.get(recordID)[0], parts[3], addressBook.get(recordID)[2] };
				addressBook.put(recordID, newRecord1);
				saveAddressBook();
				out.println("200 OK \nRecord " + recordID + " updated: ");
				out.println(recordID + " " + addressBook.get(recordID)[0] + " " + addressBook.get(recordID)[1] + " "
						+ addressBook.get(recordID)[2]);
				break;
			case 3:
				String[] newRecord2 = { addressBook.get(recordID)[0], addressBook.get(recordID)[1], parts[3] };
				addressBook.put(recordID, newRecord2);
				saveAddressBook();
				out.println("200 OK \nRecord " + recordID + " updated: ");
				out.println(recordID + " " + addressBook.get(recordID)[0] + " " + addressBook.get(recordID)[1] + " "
						+ addressBook.get(recordID)[2]);
				break;
			default:
				out.println("400 Invalid UPDATE command");
				break;
			}
		} else {
			out.println("403 The Record ID does not exist.");
		}
	}

	private void addRecord(String[] parts) {
		if (addressBook.size() >= MAX_RECORDS) {
			out.println("403 Address book full");
			return;
		}
		if (currentUser == null) {
			out.println("401 You are not currently logged in, login first");
			return;
		}
		if (parts.length < 3) {
			out.println("400 Invalid ADD command");
			return;
		}
		// Checks for input validation with First,LastName less than 8 characters and
		// Mobile Number Less than 12 characters
		if (parts[1].length() > 8 || parts[2].length() > 8 || parts[3].length() > 12) {
			out.println("301 Wrong Input format. Try Again.");
			return;
		}
		int newId = generateRecordID(); // to generate unique ID.
		addressBook.put(newId, new String[] { parts[1], parts[2], parts[3] });
		out.println("200 OK \nThe new Record ID is " + newId);
		saveAddressBook();
	}

	private void deleteRecord(String[] parts) {
		if (currentUser == null) {
			out.println("401 You are not currently logged in, login first");
			return;
		}
		if (parts.length != 2) {
			out.println("400 Invalid DELETE command");
			return;
		}
		try {
			int id = Integer.parseInt(parts[1]);
			if (addressBook.containsKey(id)) {
				addressBook.remove(id);
				out.println("200 OK");
				saveAddressBook();
			} else {
				out.println("403 The Record ID does not exist.");
			}
		} catch (NumberFormatException e) {
			out.println("301 message format error");
		}
	}

	private void listRecords() {
		if (addressBook.size() <= 0) {
			out.println("301 Address book empty");
			return;
		}
		StringBuilder sb = new StringBuilder("200 OK\nThe list of records in the book:\n");
		for (Map.Entry<Integer, String[]> entry : addressBook.entrySet()) {
			sb.append(entry.getKey() + " " + entry.getValue()[0] + " " + entry.getValue()[1] + " " + entry.getValue()[2]
					+ "\n");
		}
		out.println(sb.toString());
	}

	private void shutdownServer() {
		if (currentUser != null && "root".equals(currentUser)) { // only root user can SHUTDOWN the server
			saveAddressBook();
			out.println("200 OK\n210 the server is about to shutdown...");
			broadcastShutdownMessage(); // broadcasting the message to all the clients
			MultiThreadServer.stopServer();
			System.exit(0); // Shutting down the server
		} else {
			out.println("402 User not allowed to execute this command");
		}
	}

	private void broadcastShutdownMessage() {
		for (Socket clientSocket : clientSockets) {
			try {
				PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
				clientOut.println("210 the server is about to shutdown...");
			} catch (IOException e) {
				System.err.println("Error sending shutdown message to clients: " + e.getMessage());
			}
		}
	}

	private void quitClient() {
		if (currentUser != null) {
			activeUsers.remove(currentUser);
			System.out.println("User quit: " + currentUser);
			currentUser = null;
		}
		out.println("200 OK");
	}

	private void saveAddressBook() {
		try (PrintWriter writer = new PrintWriter(new FileWriter(ADDRESS_BOOK))) {
			for (Map.Entry<Integer, String[]> entry : addressBook.entrySet()) {
				writer.printf("%04d,%s,%s,%s%n", entry.getKey(), entry.getValue()[0], entry.getValue()[1],
						entry.getValue()[2]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int generateRecordID() {
		int maxId = 1000;
		for (int id : addressBook.keySet()) {
			if (id > maxId) {
				maxId = id;
			}
		}
		return maxId + 1;
	}
}