package nlp.lm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Set;


public class DiscountLMModel extends Tokenizer implements LMModel{
    HashMap<String, HashMap<String, Integer>> bigramCount = new HashMap<>();
    HashMap<String, Double> unigramCount = new HashMap<>();
    HashMap<String,Integer> wordCount = new HashMap<>();
    int totalWords = 0;
    protected HashMap<String,Integer> sumWords;
    String filename;
    double discount;

    DiscountLMModel(String filename, double discount) {
        this.filename = filename;
        this.discount = discount;  // Initialize bigramCount as a new HashMap
        this.sumWords = new HashMap<>();

        // tokenize
        ArrayList<String> words = processFile(filename);

        // take bigram and unigram count
        bigramCounts(words);
        findUnigram(words); 

    }


    // HELPER FUNCTIONS!!!!
    // ******************************************************************** //

    /**
     * get unigram probabilities of every word in text file
     * @param words
     */
    public void findUnigram(List<String>words) {
        totalWords = 0;
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
        Double sumLogProb = 0.0;
        Set<String> uniqueBigramProbs = new HashSet<>();

        for (int i = 0; i < sentWords.size() - 1; i++) {
            String first = sentWords.get(i);
            String second = sentWords.get(i + 1);
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
        // find reserved mass
        if (!bigramCount.containsKey(first) || bigramCount.get(first).get(second) == null) {
            return 0.0;
        }
        Integer numFollows = bigramCount.get(first).get(second);
        Integer allBigrams = sumWords.get(first);

        Double reservedMass = ((double)(discount * numFollows) / (double)allBigrams);

        // printUnigramCounts();
        Double sumUnigram = 0.0; 
        for(Double uni: unigramCount.values()) {
            sumUnigram += uni;
        }

        Double alpha = (double)(reservedMass)/(1 - sumUnigram);
        Double multAlpha = alpha * getUnigramIndividual(first);

        if (bigramCount.get(first).containsKey(second)) {
            Integer countFirstSecond = bigramCount.get(first).get(second);
            Integer countSecond = wordCount.get(second);

            return (double)(countFirstSecond - discount)/countSecond;
        }
            return multAlpha;
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

    public void printUnigramCounts() {
        System.out.println("Unigram Counts:");
        System.out.println("==============");
    
        // Iterate through the outer HashMap (first word)
        for (String first : unigramCount.keySet()) {
            System.out.println("First Word: \"" + first + "\"");
            System.out.printf("    %-10s: %.2f\n", first, unigramCount.get(first));
            // Add a separator line for readability between bigram groups
            System.out.println("---------------------------");
        }
    }

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
        ArrayList<String>words = processFile(filename);
        Integer numWords = words.size();

        Double perplexity = Math.pow(10.0, (-1 * (logProb(words)) / getTotalBigramCount()));

        return perplexity;
    }

    public double getUnigramIndividual (String word) {
        return wordCount.get(word)/totalWords;
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

    // ******************************************************************** //

    public static void main(String[] args) {
        
        String filepath = "./././data/training.txt";
        String devFile = "./././data/development.txt";

        DiscountLMModel model1 = new DiscountLMModel(filepath, 0.99);
        DiscountLMModel model2 = new DiscountLMModel(filepath, 0.9);
        DiscountLMModel model3 = new DiscountLMModel(filepath, 0.75);
        DiscountLMModel model4 = new DiscountLMModel(filepath, 0.5);
        DiscountLMModel model5 = new DiscountLMModel(filepath, 0.25);
        DiscountLMModel model6 = new DiscountLMModel(filepath, 0.1);

        System.out.println("Discount Perplexity");
        System.out.println("-------------------------------");
        System.out.println("model 1: " + model1.getPerplexity(devFile));
        System.out.println("model 2: " + model2.getPerplexity(devFile));
        System.out.println("model 3: " + model3.getPerplexity(devFile));
        System.out.println("model 4: " + model4.getPerplexity(devFile));
        System.out.println("model 5: " + model5.getPerplexity(devFile));
        System.out.println("model 6: " + model6.getPerplexity(devFile));


    }       
}
	


        