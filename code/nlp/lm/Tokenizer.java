package nlp.lm;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    public List<String> processFile(String filePath) {
    File file = new File(filePath);
    List<String> words = new ArrayList<>();
    // comment
    // Try to read the file
    try {
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // Tokenize each line
            words.addAll(splitText(line));
            }
            scanner.close();
        }

    catch (FileNotFoundException e) {
        System.out.println("File not found: " + filePath);
        }
    return words;
    }


    public List<String> splitText(String text) {
        String [] tokens = text.split("\\s+"); 
        List<String> tokenList = new ArrayList<>();
        for (String token:tokens) {
            tokenList.add(token);
        }
        return tokenList;
    }


    public static void main(String[]args) {
        Tokenizer tokenizer = new Tokenizer();
        String filepath = "/Users/anjalinuggehalli/Desktop/assignment-2-anjali-pine-main/data/sentences";
        List<String> words = tokenizer.processFile(filepath);

        for(String word:words){
            System.out.println(word);
        }
    }
    
}
