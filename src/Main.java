import java.net.*;
import java.util.*;
import java.io.*;

public class Main {
    private static final String WORKER_ADDRESS = "localhost";
    private static final int[] WORKER_PORTS = {5001, 5002, 5003, 5004, 5005};
    private static final Random RANDOM = new Random();
    private static int lamportClock = 0; // Lamport clock for Main process

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Step 1: Get user input
        System.out.print("Enter a paragraph: ");
        String paragraph = scanner.nextLine();
        if (paragraph.trim().isEmpty()) {
            System.err.println("Paragraph cannot be empty. Please restart and enter valid input.");
            return;
        }
        String[] words = paragraph.split("\\s+"); // Split paragraph into words

        // Step 2: Randomly distribute words to workers
        Map<Integer, List<String>> workerWordMap = new HashMap<>();
        for (int i = 0; i < WORKER_PORTS.length; i++) {
            workerWordMap.put(i, new ArrayList<>());
        }

        for (String word : words) {
            int workerIndex = RANDOM.nextInt(WORKER_PORTS.length);
            workerWordMap.get(workerIndex).add(word);
        }

        // Send words to workers
        System.out.println("Distributing words to workers...");
        for (int i = 0; i < WORKER_PORTS.length; i++) {
            sendWordsToWorker(WORKER_PORTS[i], workerWordMap.get(i));
        }

        // Step 3: Wait 15 seconds
        System.out.println("Waiting 15 seconds...");
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            System.err.println("Error while waiting: " + e.getMessage());
        }

        // Step 4: Collect words from workers
        System.out.println("Collecting words from workers...");
        List<String> collectedWords = new ArrayList<>();
        for (int workerPort : WORKER_PORTS) {
            collectedWords.addAll(collectWordsFromWorker(workerPort));
        }

        // Step 5: Reconstruct and print the paragraph
        String reconstructedParagraph = String.join(" ", collectedWords);
        System.out.println("Reconstructed paragraph: " + reconstructedParagraph);
    }

    // Sends words to a worker process
    private static void sendWordsToWorker(int port, List<String> words) {
        try (Socket socket = new Socket(WORKER_ADDRESS, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            lamportClock++; // Increment Lamport clock before sending
            out.write("CLOCK:" + lamportClock + "\n"); // Send the current Lamport clock

            for (String word : words) {
                out.write(word + "\n"); // Send each word
            }
            out.write("END\n"); // Signal end of word transmission
            out.flush(); // Ensure all data is sent
        } catch (IOException e) {
            System.err.println("Error sending words to worker at " + WORKER_ADDRESS + ":" + port + " - " + e.getMessage());
        }
    }

    // Collects words from a worker process
    private static List<String> collectWordsFromWorker(int port) {
        List<String> words = new ArrayList<>();
        try (Socket socket = new Socket(WORKER_ADDRESS, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            lamportClock++; // Increment Lamport clock before requesting collection
            out.write("COLLECT\n"); // Send collection request
            out.flush(); // Ensure the message is sent

            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("CLOCK:")) {
                    // Update Lamport clock based on the received clock value
                    int receivedClock = Integer.parseInt(line.split(":")[1]);
                    lamportClock = Math.max(lamportClock, receivedClock) + 1;
                } else if ("END".equals(line)) {
                    break; // End of transmission
                } else {
                    words.add(line); // Add received word to the list
                }
            }
        } catch (IOException e) {
            System.err.println("Error collecting words from worker at " + WORKER_ADDRESS + ":" + port + " - " + e.getMessage());
        }
        return words;
    }
}
