import java.net.*;
import java.util.*;
import java.io.*;

public class Charlie {
    private static final int PORT = 5003; // Unique port for Charlie
    private static int lamportClock = 0; // Lamport clock for Charlie
    private static final List<Pair<String, Integer>> words = new ArrayList<>(); // Words assigned to Charlie

    public static void main(String[] args) {
        System.out.println("Charlie process started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.startsWith("CLOCK:")) {
                            int receivedClock = Integer.parseInt(line.split(":")[1]);
                            lamportClock = Math.max(lamportClock, receivedClock) + 1;
                        } else if ("END".equals(line)) {
                            break;
                        } else if ("COLLECT".equals(line)) {
                            lamportClock++;
                            out.write("CLOCK:" + lamportClock + "\n");
                            for (Pair<String, Integer> word : words) {
                                out.write(word.getKey() + "\n");
                            }
                            out.write("END\n");
                            out.flush();
                        } else {
                            words.add(new Pair<>(line, lamportClock));
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to start Charlie process on port " + PORT + ": " + e.getMessage());
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
