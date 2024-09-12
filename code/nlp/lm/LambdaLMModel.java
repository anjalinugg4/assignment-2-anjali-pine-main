package nlp.lm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.DoubleAccumulator;

public class LambdaLMModel implements LMModel {
    protected static HashMap<String, HashMap<String, Integer>> bigramCount = new HashMap<>();
    protected static HashMap<String, Double> unigramCount = new HashMap<>();
    protected static HashMap<String,Integer> wordCount = new HashMap<>();
    protected static int totalWords = 0;
    protected HashMap<String,Integer> sumWords;
    String filename;
    double lambda;

    LambdaLMModel(String filename, double lambda) {
        this.filename = filename;
        this.lambda = lambda;  // Initialize bigramCount as a new HashMap
        this.sumWords = new HashMap<>(); 

    }

    /**
     * get unigram probabilities of every word in text file
     * @param words
     */
    public void findUnigram(List<String>words) {
        
        for (String word:words){
            if (wordCount.containsKey(word)) {
                wordCount.put(word, wordCount.get(word) + 1);
            } 
            // If the word is not in the map, add it with a count of 1
            else {
                wordCount.put(word, 1);
            }
        }

        for (int count : wordCount.values()) {
            totalWords += count;
        }

        // New HashMap to store words and their probabilities
        

        // Calculate the probability for each word and store it in the new HashMap
        for (HashMap.Entry<String, Integer> entry : wordCount.entrySet()) {
            String word = entry.getKey();
            int count = entry.getValue();
            double probability = (double) count / totalWords;  // Calculate probability
            unigramCount.put(word, probability);  // Add word and its probability
        }
    }


    /**
	 * Given a sentence, return the log of the probability of the sentence based on the LM.
	 * 
	 * @param sentWords the words in the sentence.  sentWords should NOT contain <s> or </s>.
	 * @return the log probability
	 */
	public double logProb(ArrayList<String> sentWords) {
        return 0.0;
    }
	/**
	 * Returns p(second | first)
	 * 
	 * @param first
	 * @param second
	 * @return the probability of the second word given the first word (as a probability)
	 */
	public double getBigramProb(String first, String second) {
        HashMap <String, Integer> secondHash = bigramCount.get(first); 
        if (secondHash.get(second) == null) {
            return 0.0;
        }
        //gets the bigram count of (first, second)
        Integer bCount = secondHash.get(second); 
        if (bCount == null) {
            return 0.0;
        }
        Integer sumBigram = sumWords.get(first);
        return ((double)bCount / (double)sumBigram);
    }
        

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
	 * Given a text file, calculate the perplexity of the text file, that is the negative average per word log
	 * probability
	 * 
	 * @param filename a text file.  The file will contain sentences WITHOUT <s> or </s>.
	 * @return the perplexity of the text in file based on the LM
	 */
	public double getPerplexity(String filename) {
        return 0.0;
    }

    public double bigramProbLambda(String first, String second, double lambda) {
        HashMap <String, Integer> secondHash = bigramCount.get(first); 
        if (secondHash.get(second) == null) {
            return 0.0;
        }
        //gets the bigram count of (first, second)
        Integer bCount = secondHash.get(second); 
        if (bCount == null) {
            return 0.0;
        }
        Integer secondWords = bigramCount.get(first).get(second);
        System.out.println(secondWords);
        Integer allBigrams = bigramCount.get(first).size();
        System.out.println(allBigrams);
        Integer sumBigram = sumWords.get(first);
        System.out.println(sumBigram);
        Double numerator = (secondWords + lambda);
        Double denominator = (allBigrams * lambda) + sumBigram;
        return (numerator / denominator);
    }
    public double getUnigramIndividual (String word) {
        return wordCount.get(word)/totalWords;
    }
    public double bigramProbDiscount(String first, String second, double discount) {
        // find reserved mass
        Integer numFollows = bigramCount.get(first).get(second);
        Integer allBigrams = bigramCount.get(first).size();

        Double reservedMass = ((double)(discount * numFollows) / (double)allBigrams);
        List<String> words = new ArrayList<>(bigramCount.get(first).keySet());
        findUnigram(words);
        Double sumUnigram = 0.0; 
        for(Double uni: unigramCount.values()) {
            sumUnigram += uni;
        }

        Double alpha = (double)(reservedMass)/(1 - sumUnigram);
        Double multAlpha = alpha * getUnigramIndividual(first);

        if (bigramCount.get(first).get(second) != null) {
            Integer countFirstSecond = bigramCount.get(first).get(second);
            Integer countSecond = wordCount.get(second);

            return (double)(countFirstSecond - discount)/countSecond;
        }
            return multAlpha;

    }


    public static void writeToFile(List<String> words, String outputFilePath) {
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

    public static void main(String[] args) {
        Tokenizer token = new Tokenizer();
        
        // Process the file to get the list of words
        List<String> words = token.processFile("/Users/anjalinuggehalli/Desktop/assignment-2-anjali-pine-main/data/test1.txt");
        String outputFilePath = "/Users/anjalinuggehalli/Desktop/assignment-2-anjali-pine-main/data/processed_output1.txt";
        LambdaLMModel model = new LambdaLMModel(outputFilePath, 0.0);

        // Call the method to write the words to the new file
        writeToFile(words, outputFilePath);
        // Build the bigram counts
        model.bigramCounts(words);
        // System.out.println(model.getBigramProb(words.get(1), words.get(3)));  
        // Print out the bigram counts for verification
        //model.printBigramCounts();
        // System.out.println(model.bigramProbLambda(words.get(1), words.get(3), 1));
        System.out.println(model.bigramProbDiscount(words.get(1), words.get(3), 0.5));
        }   
        
}
	


        