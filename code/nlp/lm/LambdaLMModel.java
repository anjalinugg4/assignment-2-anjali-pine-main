package nlp.lm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.DoubleAccumulator;

public class LambdaLMModel implements LMModel {
    private HashMap<String, HashMap<String, Integer>> bigramCount;
    String filename;
    double lambda;

    LambdaLMModel(String filename, double lambda) {
        this.filename = filename;
        this.lambda = lambda;
        this.bigramCount = bigramCount;
    }
    /**
     * get unigram probabilities of every word in text file
     * @param words
     */
    public HashMap<String,Double> findUnigram(List<String>words) {
        HashMap<String,Integer> wordCount = new HashMap<>();
        for (String word:words){
            if (wordCount.containsKey(word)) {
                wordCount.put(word, wordCount.get(word) + 1);
            } 
            // If the word is not in the map, add it with a count of 1
            else {
                wordCount.put(word, 1);
            }
        }
        int totalWords = 0;
        for (int count : wordCount.values()) {
            totalWords += count;
        }

        // New HashMap to store words and their probabilities
        HashMap<String, Double> wordProbability = new HashMap<>();

        // Calculate the probability for each word and store it in the new HashMap
        for (HashMap.Entry<String, Integer> entry : wordCount.entrySet()) {
            String word = entry.getKey();
            int count = entry.getValue();
            double probability = (double) count / totalWords;  // Calculate probability
            wordProbability.put(word, probability);  // Add word and its probability
        }
        return wordProbability;
    }


    /**
	 * Given a sentence, return the log of the probability of the sentence based on the LM.
	 * 
	 * @param sentWords the words in the sentence.  sentWords should NOT contain <s> or </s>.
	 * @return the log probability
	 */
	public double logProb(ArrayList<String> sentWords) {

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
 
        double sum = 0.0;
        for (double f : secondHash.values()) {
            sum += f;
        }
        return (bCount / sum);
    }
        

    public void bigramCounts(String[]words) {
        for (int i = 0; i < words.length -1; i ++) {
            String first = words[i];
            String second = words[i + 1];

            bigramCount.putIfAbsent(first, new HashMap<>());
            HashMap<String, Integer> secondHash = bigramCount.get(first);
            secondHash.put(second, secondHash.getOrDefault(second,0) + 1);
        }
    }
	
}
