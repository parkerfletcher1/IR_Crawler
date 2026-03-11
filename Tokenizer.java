import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class Tokenizer {
    private static String stopWords = "a,an,the,and,or,but,if,while,with,by,for,to,in,on,at,of,is,are,was,were,be,been,being,have,has,had,do,does,did,this,that,these,those,it";
    private static String[] stopWordsArray;
    private HashMap<String, Integer> tokenFrequency = new HashMap<>();

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
        stopWordsArray = new String[stopWords.split(",").length];
        for (int i = 0; i < stopWords.split(",").length; i++) {
            stopWordsArray[i] = stopWords.split(",")[i].trim();
        }

        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize(Dir);
        tokenizer.printTokenFrequencies(tokenizer.tokenFrequency);

    }

    // method to print the token frequencies
    private void printTokenFrequencies(HashMap<String, Integer> tokenFrequency) {
        int count = 0;
        System.out.println("Token Frequencies:");
        for (String token : tokenFrequency.keySet()) {
            if (tokenFrequency.get(token) > 18) {
                count++;
            }
        }

        for (String s : stopWordsArray) {
            tokenFrequency.remove(s);
        }

        Map<String, Integer> sortedTokenFrequency = tokenFrequency.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        System.out.println("Top 50000 Tokens:");
        sortedTokenFrequency.entrySet().stream().limit(50000).forEach(entry -> {
            System.out.printf("%s: %d%n", entry.getKey(), entry.getValue());
        });
        System.out.println("Tokenization complete. Total tokens: " + sortedTokenFrequency.size());
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
                System.out.println("Tokenizing file: " + file.getName());
                String line = "";
                BufferedReader br = new BufferedReader(new FileReader(file));
                while ((line = br.readLine()) != null) {
                    // Convert to lowercase, remove punctuation, and split into tokens using regex.
                    String[] tokens = line.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]+", "").split(" ");
                    for (String token : tokens) {
                        if (stopWords.contains(token) || token.isEmpty()) {
                            continue; // Skip stop words and empty tokens
                        }
                        token = Stemm(token);
                        if (!token.isEmpty()) {
                            tokenFrequency.put(token, tokenFrequency.getOrDefault(token, 0) + 1);
                        }
                    }
                }
                br.close();
            } else if (file.isDirectory()) {
                // Recursively tokenize subdirectories
                tokenize(file);
            }
        }
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
        if (word.matches(".*[0-9].*") && !(word.length() == 4)) {
            return "";
        }

        return word;
    }
}