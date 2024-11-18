import java.net.*;
import java.util.*;
import java.io.*;

public class Echo {
    private static final int PORT = 5005; // Unique port for Echo
    private static int lamportClock = 0; // Lamport clock for Echo
    private static final List<String> words = new ArrayList<>(); // Words assigned to Echo

    public static void main(String[] args) {
        System.out.println("Echo process started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.startsWith("CLOCK:")) {
                            // Update Lamport clock
                            int receivedClock = Integer.parseInt(line.split(":")[1]);
                            lamportClock = Math.max(lamportClock, receivedClock) + 1;
                        } else if ("END".equals(line)) {
                            // End of word transmission
                            break;
                        } else if ("COLLECT".equals(line)) {
                            // Collect request from Main
                            lamportClock++; // Increment before responding
                            out.write("CLOCK:" + lamportClock + "\n"); // Send updated clock
                            for (String word : words) {
                                out.write(word + "\n"); // Send stored words
                            }
                            out.write("END\n"); // End of collection
                            out.flush();
                            break; // Done handling this request
                        } else {
                            // Store received word
                            words.add(line);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to start Echo process on port " + PORT + ": " + e.getMessage());
        }
    }
}
