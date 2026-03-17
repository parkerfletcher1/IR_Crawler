//import org.netpreserverve.warc.WarcReader;
import java.io.*;
import java.util.zip.GZIPInputStream;
import java.net.http.*;
import java.net.URI;


public class WarcWetExtract {


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

        String[] wetPaths = new String[2];


        for(File file : Dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".gz")) {
                String path = file.getAbsolutePath();
                wetPaths = getWetPaths(path); 
            }
        }

        for (String url : wetPaths) {
            try { Thread.sleep(5000); } catch (InterruptedException ignored) {}

            readWarcWetPath(url);

        }
        
    }


    public static String[] getWetPaths(String path) throws IOException {


        BufferedReader reader = new BufferedReader(

            new InputStreamReader(
                new GZIPInputStream(new FileInputStream(path))
            )
        );


        //number of paths to record 
        String[] wetStrings = new String[5];


        int count = 0; 
        String line;

        while ((line = reader.readLine()) != null) {

            if(count == wetStrings.length) {
                continue; 
            }

            //get the url for the wet file and store it in an array
            String url = "https://data.commoncrawl.org/" + line;
            wetStrings[count] = url;
            count ++; 

            System.out.println(url);
        }

        reader.close();

        return wetStrings;
    }


    //reads the content of a warc wet file from a given url and writes text per document to TEXT dir 
    public static void readWarcWetPath(String url){

        try{
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();



            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            

            //return if the response is not successful
            if (response.statusCode() != 200) {
                System.out.println("Server returned: " + response.statusCode());
                BufferedReader errReader = new BufferedReader(new InputStreamReader(response.body()));
                String line;
                while ((line = errReader.readLine()) != null) {
                    System.out.println(line); 
                }
                return;
            }

            //uncompress the response body and read it line by line, writing each line to a text file in the TEXT directory
            GZIPInputStream gzipInputStream = new GZIPInputStream(response.body());
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInputStream));
            String line; 

            BufferedWriter writer = new BufferedWriter(new FileWriter("TEXT/" + url.substring(url.lastIndexOf("/") + 1) + ".txt"));
            boolean inConversion = false;
            boolean inContent = false;

            while ((line = reader.readLine())!= null) {

                //check if the line starts with "WARC/1.0" and if so, set a flag to indicate that we are no longer in the conversion section of the warc file
                if(line.startsWith("WARC/1.0")){
                    inConversion = false;
                    inContent = false;
                    continue; 
                }
                //check if the line starts with "WARC-Type: conversion" and if so, set a flag to indicate that we are in the conversion section of the warc file
                if(line.startsWith("WARC-Type: conversion")) {
                    inConversion = true;
                    continue; 
                }

                //check if we are in the conversion section and if the line is empty, if so, set a flag to indicate that we are in the content section of the warc file
                if(inConversion && line.isEmpty()) {
                    inContent = true;
                    continue; 
                }
                //if we are in the content section, write the line to the text file
                if(inContent) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            writer.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


        
    }


    
}
