import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Tokenizer {
    private static HashSet<String> englishWords = new HashSet<>();
    private static Map<String, Integer> sortedTokenFrequency;
    private static Map<String, Map<String, Integer>> invertedIndex = new HashMap<>();

    public static void main(String[] args) throws IOException {
        /* checks arg length to ensure an argumement is provided */
        if (args.length == 0) {
            System.out.println("Please provide a filePath.");
            return;
        }

        /* checks if the provided path is a directory */
        File Dir = new File(args[0]);
        if (!Dir.isDirectory()) {
            System.out.println("The provided path is not a directory.");
            return;
        }

        loadDict("words.txt");

        Tokenizer tokenizer = new Tokenizer();

        double startTime = System.currentTimeMillis();
        tokenizer.tokenize(Dir);
        double endTime = System.currentTimeMillis();
        System.out.println("Tokenization time: " + ((endTime - startTime) / 1000.0) + " s");

        printTop10Docs("doctor");

    }

    private static void writeTokensToFile(Map<String, Integer> sortedLocalMap, String string) {
        Map<String, Double> rtfResults = calculateRTF(sortedLocalMap);
        
        try {
            File file = new File(string);
            if (!file.exists()) {
                file.createNewFile();
            }
            Formatter formatter = new Formatter(file);
            sortedTokenFrequency.entrySet().stream().forEach(entry -> {
                String token = entry.getKey();
                int count = entry.getValue();
                double rtf = rtfResults.get(token);
                formatter.format("%s: %d (RTF: %.6f)%n", token, count, rtf);
            });
            formatter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadDict(String path) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(path));

        String line;
        while ((line = br.readLine()) != null) {
            englishWords.add(line.trim().toLowerCase());
        }

        br.close();
    }

    // method to print the token frequencies
    private Map<String, Integer> printTokenFrequencies(HashMap<String, Integer> tokenFrequency) {

        sortedTokenFrequency = tokenFrequency.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        return sortedTokenFrequency;
    }
    
     // helper method for sorting hashmaps
    public Map<String, Integer> sortMap(HashMap<String, Integer> fileHashMap) {
        return printTokenFrequencies(fileHashMap);
    }

    /*
     * tokenizes the files in the provided directory and prints the tokens to the
     * console
     */
    private void tokenize(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            System.out.println("No files found in the directory.");
            return;
        }

        for (File file : files) {
            if (file.isFile()) {

                HashMap<String, Integer> fileSpecificHashMap = new HashMap<>();

                System.out.println("Tokenizing file: " + file.getName());
                String line = "";
                BufferedReader br = new BufferedReader(new FileReader(file));

                StringBuilder currentDoc = new StringBuilder();
                String currentDocName = null;
                boolean inContent = false;

                while ((line = br.readLine()) != null) {
                    // Convert to lowercase, remove punctuation, and split into tokens using regex.
                    String[] tokens = line.toLowerCase().split("\\s+");
                    for (String token : tokens) {
                        token = Stemm(token);
                        if (!token.isEmpty() && englishWords.contains(token)) {
                            fileSpecificHashMap.put(token, fileSpecificHashMap.getOrDefault(token, 0) + 1);
                        }
                    }

                    // Start of a new document
                    if (line.startsWith("WARC/1.0")) {

                        if (currentDoc.length() > 0 && currentDocName != null) {
                            processDocument(currentDoc.toString(), currentDocName);
                        }

                        currentDoc = new StringBuilder();
                        currentDocName = null;
                        inContent = false;
                        continue;
                    }

                    // Extract document name (URL)
                    if (line.startsWith("WARC-Target-URI:")) {
                        currentDocName = line.substring(17).trim();
                    }

                    // Empty line separates headers from content
                    if (line.trim().isEmpty()) {
                        inContent = true;
                        continue;
                    }

                    // Collect content
                    if (inContent) {
                        currentDoc.append(line).append(" ");
                    }
                }

                // Process last document
                if (currentDoc.length() > 0 && currentDocName != null) {
                    processDocument(currentDoc.toString(), currentDocName);
                }

                br.close();

                pruneTokens(fileSpecificHashMap);
                String outPutName = "freq_" + file.getName() + ".txt";
                Map<String, Integer> sortedLocal = sortMap(fileSpecificHashMap);
                writeTokensToFile(sortedLocal, outPutName);

            } else if (file.isDirectory()) {
                // Recursively tokenize subdirectories
                tokenize(file);
            }
        }
    }

    private void processDocument(String text, String docName) {

        HashMap<String, Integer> fileSpecificHashMap = new HashMap<>();

        String[] tokens = text.toLowerCase().split("\\s+");

        for (String token : tokens) {
            token = Stemm(token);
            if (!token.isEmpty() && englishWords.contains(token)) {
                fileSpecificHashMap.put(token, fileSpecificHashMap.getOrDefault(token, 0) + 1);
            }
        }

        pruneTokens(fileSpecificHashMap);

        for (Map.Entry<String, Integer> entry : fileSpecificHashMap.entrySet()) {
            String word = entry.getKey();
            int count = entry.getValue();

            invertedIndex.putIfAbsent(word, new HashMap<>());
            invertedIndex.get(word).put(docName, count);
        }
    }

    private static HashMap<String, Integer> pruneTokens(HashMap<String, Integer> tokenFrequency) {
        tokenFrequency.entrySet().removeIf(entry -> entry.getValue() < 2 || entry.getValue() > 10000
                || entry.getKey().length() < 2 || entry.getKey().length() > 20);
        return tokenFrequency;
    }

    // method to stem the tokens by removing common suffixes i.e ing, es, ed, s

    private String Stemm(String word) {
        if (word.endsWith("ing") && word.length() > 5)
            return word.substring(0, word.length() - 3);
        if (word.endsWith("es") && word.length() > 4)
            return word.substring(0, word.length() - 2);
        if (word.endsWith("ed") && word.length() > 4)
            return word.substring(0, word.length() - 2);
        if (word.endsWith("s") && word.length() > 3 && !word.endsWith("ss"))
            return word.substring(0, word.length() - 1);
        return word;
    }

    /* Method for calculating the RTF for each token in its file. */
    
    private static Map<String, Double> calculateRTF(Map<String,Integer> sortedLocalMap){
        int totalTokens = sortedLocalMap.values().stream().mapToInt(Integer::intValue).sum();

        Map<String, Double> rtfMap = new HashMap<>();

        if(totalTokens == 0){
            return rtfMap;
        }

        for (Map.Entry<String,Integer> entry : sortedLocalMap.entrySet()){
            double rtf = (double) entry.getValue() / totalTokens;
            rtfMap.put(entry.getKey(), rtf);
        }
        return rtfMap;
    }

    public static List<Map.Entry<String, Integer>> getTop10Docs(String word) {
        word = word.toLowerCase();

        if (!invertedIndex.containsKey(word)) {
            return new ArrayList<>();
        }

        return invertedIndex.get(word)
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    public static void printTop10Docs(String word) {
        List<Map.Entry<String, Integer>> results = getTop10Docs(word);

        if (results.isEmpty()) {
            System.out.println("Word not found.");
            return;
        }

        System.out.println("Top documents for word: " + word);
        for (Map.Entry<String, Integer> entry : results) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}