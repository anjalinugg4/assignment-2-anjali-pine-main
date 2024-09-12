package nlp.lm;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class Tokenizer {
    public List<String> processFile(String filePath) {
    File file = new File(filePath);
    final List<String> words = new ArrayList<>();
    // comment
    // Try to read the file
    try {
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // line = "<s> " + line + " </s>";
            // Tokenize each line
            words.addAll(splitText(line));
            }
            scanner.close();
        }

    catch (FileNotFoundException e) {
        System.out.println("File not found: " + filePath);
        }
    List<String> replaced = getUnknown(words);
    return replaced;
    }


    public List<String> splitText(String text) {
        String [] tokens = text.split("\\s+"); 
        List<String> tokenList = new ArrayList<>();
        for (String token:tokens) {
            tokenList.add(token);
        }
        return tokenList;
    }

    public List<String> getUnknown(List<String> words){
        Set<String> set = new HashSet<>();
        List<String> replaceUnknown = new ArrayList<>();
        for (String word:words) {
            if ((!word.equals("<s>")) && (!word.equals("</s>"))) {
                if (!set.contains(word)) {
                    replaceUnknown.add("<UNK>");
                    set.add(word);
                }
                else {
                    replaceUnknown.add(word);
                }
            }
            else{
                replaceUnknown.add(word);
            }
            
        }
        return replaceUnknown;
    }


    public static void main(String[]args) {
        Tokenizer tokenizer = new Tokenizer();
        // String filepath = "/Users/anjalinuggehalli/Desktop/assignment-2-anjali-pine-main/data/test.txt";
        String filepath = "/Users/anjalinuggehalli/Desktop/assignment-2-anjali-pine-main/data/sentences";
        List<String> words = tokenizer.processFile(filepath);

        

        for(String word:words){
            System.out.println(word);
        }
    }
    
}