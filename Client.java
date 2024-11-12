import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/** @author Tejas Vaity,
 * Sayojya Patil **/
public class Client {
	public static final int SERVER_PORT = 3811;

	public static void main(String[] args) {

		if (args.length < 1) {
			System.out.println("Usage: client <Server IP Address>");
			System.exit(1);
		}

		String serverIp = args[0];

		try (Socket socket = new Socket(serverIp, SERVER_PORT);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
			System.out.println(
					"Enter command [LOGIN, LOGOUT, WHO ,LOOK, LIST, SHUTDOWN(only root user), QUIT] and when Logged in [ADD, DELETE, UPDATE] : ");
			String userInput;
			connectToServer(in);

			while ((userInput = consoleInput.readLine()) != null) {
				out.println(userInput);

				if (userInput.equalsIgnoreCase("QUIT")) {
					System.exit(0);
					break;
				}
			}

		} catch (IOException e) {
			System.err.println("Client error: " + e.getMessage());
		}
	}

	private static void connectToServer(BufferedReader serverInput) {
		new Thread(() -> {
			String response;
			try {
				while ((response = serverInput.readLine()) != null) {
					System.out.println(response);
					if (response.contains("210 the server is about to shutdown")) {
						System.out.println("Server shutdown signal has been received. Terminating client...");
						System.exit(0);
					}
				}
			} catch (IOException e) {
				System.err.println("Connection to server lost.");
			}
		}).start();
	}
}