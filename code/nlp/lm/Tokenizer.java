/**
 * Author: Anjali Nuggehalli and Pine Netcharussaeng
 * Assignment 2B
 * Date: September 18, 2024
 */

package nlp.lm;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;


public class Tokenizer {

    /**
     * Processes a text file and returns a list of words with unknown tokens replaced.
     *
     * This method reads the content of a file line by line, adds boundary markers 
     * ("<s>" and "</s>") around each line, tokenizes the lines, and then replaces 
     * any unknown words using a custom method.
     *
     * @param filePath the path to the file to be processed
     * @return an ArrayList of Strings where unknown words have been replaced
     */

    public ArrayList<String> processFile(String filePath) {
    File file = new File(filePath);
    ArrayList<String> words = new ArrayList<>();
    // comment
    // Try to read the file
    try {
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            line = "<s> " + line + " </s>";
            // Tokenize each line
            words.addAll(splitText(line));
            }
            scanner.close();
        }

    catch (FileNotFoundException e) {
        System.out.println("File not found: " + filePath);
        }
    ArrayList<String> replaced = getUnknown(words);
    return replaced;
    }

    /**
     * Splits the given text into individual tokens based on whitespace.
     *
     * This method takes a string of text and splits it into an array of tokens 
     * using whitespace as the delimiter. The tokens are then added to an ArrayList 
     * and returned.
     *
     * @param text the text to be tokenized
     * @return an ArrayList of tokens extracted from the input text
     */
    public ArrayList<String> splitText(String text) {
        String [] tokens = text.split("\\s+"); 
        ArrayList<String> tokenList = new ArrayList<>();
        for (String token:tokens) {
            tokenList.add(token);
        }
        return tokenList;
    }

    /**
     * Replaces unknown words in a list with the token "<UNK>".
     *
     * This method processes a list of words and replaces each word that is not part 
     * of a previously seen set with the token "<UNK>". Special tokens "<s>" and "</s>" 
     * are not replaced. Once a word has been replaced, it is added to the set of known words.
     *
     * @param words the list of words to process
     * @return an ArrayList of words where unknown words are replaced with "<UNK>"
     */

    public ArrayList<String> getUnknown(List<String> words){
        Set<String> set = new HashSet<>();
        ArrayList<String> replaceUnknown = new ArrayList<>();
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