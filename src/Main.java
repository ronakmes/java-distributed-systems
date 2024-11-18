
import java.net.*;
import java.util.*;
import java.io.*;


public class Main {
    // Addresses and ports for the worker processes
    private static final String WORKER_ADDRESS = "localhost";
    private static final int[] WORKER_PORTS = {5001, 5002, 5003, 5004, 5005};
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // Step 1: Get user input
        System.out.print("Enter a paragraph: ");
        String paragraph = scanner.nextLine();
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

    private static void sendWordsToWorker(int port, List<String> words) {
        try (Socket socket = new Socket(Main.WORKER_ADDRESS, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            for (String word : words) {
                out.println(word); // Send each word to the worker
            }
            out.println("END"); // Signal end of word transmission
        } catch (IOException e) {
            System.err.println("Error sending words to worker at " + Main.WORKER_ADDRESS + ":" + port);
        }
    }

    private static List<String> collectWordsFromWorker(int port) {
        List<String> words = new ArrayList<>();
        try (Socket socket = new Socket(Main.WORKER_ADDRESS, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println("COLLECT"); // Request to collect words

            String word;
            while ((word = in.readLine()) != null) {
                if ("END".equals(word)) {
                    break; // End of transmission
                }
                words.add(word);
            }
        } catch (IOException e) {
            System.err.println("Error collecting words from worker at " + Main.WORKER_ADDRESS + ":" + port);
        }
        return words;
    }
}
