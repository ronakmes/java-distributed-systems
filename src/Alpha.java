import java.net.*;
import java.util.*;
import java.io.*;

public class Alpha {
    private static final int PORT = 5001; // Unique port for Alpha
    private static int lamportClock = 0; // Lamport clock for Alpha
    private static final List<String> words = new ArrayList<>(); // Words assigned to Alpha

    public static void main(String[] args) {
        System.out.println("Alpha process started on port " + PORT);

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
                            for (Pair<String, Integer> word : words) {
                                out.write(word.getKey() + "\n"); // Send stored words
                            }
                            out.write("END\n"); // End of collection
                            out.flush();
                        } else {
                            // Store received word
                            words.add(new Pair<>(line, lamperClock));
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to start Alpha process on port " + PORT + ": " + e.getMessage());
        }
    }
private static class Pair<K, V> {
        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}
