import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final ConcurrentHashMap<String, String> userPassword = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        final int port = 4444;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started and waiting for clients on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept incoming client connections
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Start a new thread for each client
                serverThread newClient = new serverThread(clientSocket, userPassword);
                newClient.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error starting server: " + e.getMessage());
        }
    }
}
