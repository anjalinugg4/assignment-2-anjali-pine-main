/**
 * Author: Anjali Nuggehalli and Pine Netcharussaeng
 * Assignment 2B
 * Date: September 18, 2024
 */

package nlp.lm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Set;



public class LambdaLMModel extends Tokenizer implements LMModel{
    HashMap<String, HashMap<String, Integer>> bigramCount = new HashMap<>();
    HashMap<String,Integer> wordCount = new HashMap<>();
    HashSet<String> vocabs = new HashSet<>();
    int totalWords = 0;
    HashMap<String,Integer> sumWords;
    String filename;
    double lambda;

    LambdaLMModel(String filename, double lambda) {
        this.filename = filename;
        this.lambda = lambda;  // Initialize bigramCount as a new HashMap
        this.sumWords = new HashMap<>(); 

        // tokenize
        ArrayList<String> words = processFile(filename);
        Collections.shuffle(words);
 
        // take bigram count
        bigramCounts(words);
        vocabs = new HashSet<String>(bigramCount.keySet());
    }


    // HELPER FUNCTIONS!!!!
    // ******************************************************************** //
    
    /**
	 * Given a sentence, return the log of the probability of the sentence based on the LM.
	 * 
	 * @param sentWords the words in the sentence.  sentWords should NOT contain <s> or </s>.
	 * @return the log probability
	 */
    
	public double logProb(ArrayList<String> sentWords) {
        Double sumLogProb = 0.0;
        Set<String> uniqueBigramProbs = new HashSet<>();
        sentWords.add(0, "<s>");
        sentWords.add("</s>");
        
        for (int i = 0; i < sentWords.size() - 1; i++) {
            String first = sentWords.get(i);
            String second = sentWords.get(i + 1);

            if(!vocabs.contains(first)) {
                first = "<UNK>";
            } 
            if(!vocabs.contains(second)) {
                second = "<UNK>";
            } 

            Double bigramProb = getBigramProb(first, second);
            Double logProb = 1e-10; 
            if (bigramProb != 0.0) {
                logProb = Math.log10(bigramProb);
            }
            // Create a string representing the bigram and its probability
            String bigramProbString = "Bigram: (" + first + ", " + second + ") - Probability: " + bigramProb;
    
            // Only print if the bigram-probability pair is not already in the set
            if (uniqueBigramProbs.add(bigramProbString)) {
                // It was successfully added to the set, so it's unique
                sumLogProb += logProb;
            }
        }
        return sumLogProb;
    }

	/**
	 * Returns p(second | first)
	 * 
	 * @param first
	 * @param second
	 * @return the probability of the second word given the first word (as a probability)
	 */
    public double getBigramProb(String first, String second) {
        HashMap<String, Integer> secondHash = bigramCount.get(first);
        
        if (secondHash == null || !secondHash.containsKey(second)) {
            return 0.0;  // Handle missing bigram
        }
    
        Integer bigramCountValue = secondHash.get(second);
        Integer sumBigram = sumWords.get(first);
        
        // Optimized lambda-smoothing calculation
        Double numerator = bigramCountValue + lambda;
        Double denominator = sumBigram + lambda * vocabs.size();  // Use vocab size for unseen smoothing
    
        return numerator / denominator;
    }

    /**
     * Calculates the bigram counts from a list of words and stores the results 
     * in a bigram frequency map.
     *
     * This method processes a list of words to compute the frequency of bigrams 
     * (word pairs). For each consecutive pair of words, it updates the bigram 
     * count in a nested HashMap, where the key is the first word and the value 
     * is another HashMap that stores the second word and its count. It also 
     * updates the total occurrences of the first word in a separate map.
     *
     * @param words the list of words to generate bigram counts from
     */
        
    public void bigramCounts(List<String>words) {

        for (int i = 0; i < words.size() -1; i ++) {
            String first = words.get(i);
            String second = words.get(i+1);

            bigramCount.putIfAbsent(first, new HashMap<>());
            HashMap<String, Integer> secondHash = bigramCount.get(first);
            secondHash.put(second, secondHash.getOrDefault(second,0) + 1);
            sumWords.put(first,sumWords.getOrDefault(first,0) + 1);

        }
    }

    /**
     * Prints the bigram counts stored in the bigram frequency map.
     *
     * This method iterates through the bigramCount map, printing each bigram 
     * (first word and second word pair) along with its corresponding frequency count.
     * It prints the first word followed by the second word and the count of how many 
     * times the bigram appeared in the input text. The output is formatted for 
     * readability, with a separator between different bigram groups.
     */
    public void printBigramCounts() {
        System.out.println("Bigram Counts:");
        System.out.println("==============");
    
        // Iterate through the outer HashMap (first word)
        for (String first : bigramCount.keySet()) {
            System.out.println("First Word: \"" + first + "\"");
            
            // Get the inner HashMap for the second word counts
            HashMap<String, Integer> secondHash = bigramCount.get(first);
    
            // Iterate through the inner HashMap (second words and their counts)
            for (String second : secondHash.keySet()) {
                int count = secondHash.get(second);
                System.out.printf("   %-10s → %-10s : %d\n", first, second, count);
            }
            
            // Add a separator line for readability between bigram groups
            System.out.println("---------------------------");
        }
    }

    /**
     * Returns the total number of unique bigrams in the bigram frequency map.
     *
     * This method calculates the total number of unique bigram pairs stored 
     * in the bigramCount map. It iterates through the outer HashMap (which holds 
     * the first words) and sums up the sizes of the inner HashMaps (which hold 
     * the second words and their counts), thereby counting the total number of 
     * bigrams.
     *
     * @return the total number of unique bigrams
     */
    public int getTotalBigramCount() {
        int totalBigrams = 0;
    
        // Iterate over each first word in the bigramCount map
        for (HashMap<String, Integer> secondHash : bigramCount.values()) {
            // Add the number of second words (bigrams) for the current first word
            totalBigrams += secondHash.size();
        }
        return totalBigrams;
    }
    

	/**
	 * Given a text file, calculate the perplexity of the text file, that is the negative average per word log
	 * probability
	 * 
	 * @param filename a text file.  The file will contain sentences WITHOUT <s> or </s>.
	 * @return the perplexity of the text in file based on the LM
	 */
	public double getPerplexity(String filename) {
        // ArrayList<String>words = processFile(filename);
        File file = new File(filename);
        Integer length = 0; 
        Double sumLogProb = 0.0;
        // Try to read the file
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                ArrayList<String> wordsSentences = splitText(line);
                sumLogProb += logProb(wordsSentences);
                length += wordsSentences.size()-1; 
            }
                scanner.close();
            }

        catch (FileNotFoundException e) {
            System.out.println("File not found: " + filename);
            }

        Double perplexity = Math.pow(10.0, (-1 * sumLogProb / length));

        return perplexity;
    }


    /**
     * Writes a list of words to a specified file.
     *
     * @param words the list of words to write
     * @param outputFilePath the file path to write the words to
     */
    public void writeToFile(List<String> words, String outputFilePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            // Write each word from the list to the file
            for (String word : words) {
                writer.write(word);
                writer.newLine();  // Write a new line after each word (or sentence)
            }
            System.out.println("Processed words have been written to " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    // ******************************************************************** //



    public static void main(String[] args) {
        String filepath = "./././data/training.txt";
        String devFile = "./././data/testing";

        LambdaLMModel model1 = new LambdaLMModel(filepath, 0.1);
        LambdaLMModel model2 = new LambdaLMModel(filepath, 0.01);
        LambdaLMModel model3 = new LambdaLMModel(filepath, 0.001);
        LambdaLMModel model4 = new LambdaLMModel(filepath, 0.0001);
        LambdaLMModel model5 = new LambdaLMModel(filepath, 0.00001);
        LambdaLMModel model6 = new LambdaLMModel(filepath, 0.000001);
        LambdaLMModel model7 = new LambdaLMModel(filepath, 0.0000001);
        LambdaLMModel model8 = new LambdaLMModel(filepath, 0.00000001);


        System.out.println("Lambda Perplexity");
        System.out.println("-------------------------------");
        System.out.println("model 1: " + model1.getPerplexity(devFile));
        System.out.println("model 2: " + model2.getPerplexity(devFile));
        System.out.println("model 3: " + model3.getPerplexity(devFile));
        System.out.println("model 4: " + model4.getPerplexity(devFile));
        System.out.println("model 5: " + model5.getPerplexity(devFile));
        System.out.println("model 6: " + model6.getPerplexity(devFile));
        System.out.println("model 7: " + model7.getPerplexity(devFile));
        System.out.println("model 8: " + model8.getPerplexity(devFile));
    }       
}
	




// public double getBigramProb(String first, String second) {
//     HashMap <String, Integer> secondHash = bigramCount.get(first); 
//     if (secondHash.get(second) == null) {
//         return 0.0;
//     }
//     //gets the bigram count of (first, second)
//     Integer bCount = secondHash.get(second); 
//     if (bCount == null) {
//         return 0.0;
//     }
//     Integer secondWords = bigramCount.get(first).get(second);
//     Integer allBigrams = bigramCount.get(first).size();
//     Integer sumBigram = sumWords.get(first);
//     Double numerator = (secondWords + lambda);
//     Double denominator = (allBigrams * lambda) + sumBigram;
//     return (numerator / denominator);
// }