import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Tokenizer {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please provide a filePath.");
            return;
        }

        File Dir = new File(args[0]);
        if (!Dir.isDirectory()) {
            System.out.println("The provided path is not a directory.");
            return;
        }

        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize(Dir);

    }

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
                    String[] tokens = line.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]+", "").split("\\s+");
                    for (String token : tokens) {
                        System.out.println(token);
                    }
                }
                br.close();
                System.out.println("Tokenizing file: " + file.getName());

            } else if (file.isDirectory()) {
                // Recursively tokenize subdirectories
                tokenize(file);
            }
        }

    }
}