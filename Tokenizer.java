import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Tokenizer {

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

        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize(Dir);
        tokenizer.printTokenFrequencies(tokenizer.tokenFrequency);

    }

    // method to print the token frequencies
    private void printTokenFrequencies(HashMap<String, Integer> tokenFrequency) {
        System.out.println("Token Frequencies:");
        for (String token : tokenFrequency.keySet()) {
            System.out.printf("%s: %d%n", token, tokenFrequency.get(token));
        }
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
                    String[] tokens = line.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]+", "").split("");
                    for (String token : tokens) {
                        if(!token.isEmpty()) {
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
}}