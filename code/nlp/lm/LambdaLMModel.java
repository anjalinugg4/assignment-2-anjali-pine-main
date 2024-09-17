package nlp.lm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Set;


public class LambdaLMModel extends Tokenizer implements LMModel{
    HashMap<String, HashMap<String, Integer>> bigramCount = new HashMap<>();
    HashMap<String,Integer> wordCount = new HashMap<>();
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
        // take bigram count
        bigramCounts(words);
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
        Integer allBigrams = bigramCount.get(first).size();
        Integer sumBigram = sumWords.get(first);
        Double numerator = (secondWords + lambda);
        Double denominator = (allBigrams * lambda) + sumBigram;
        return (numerator / denominator);
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
        ArrayList<String>words = processFile(filename);
        Integer numWords = words.size();

        Double perplexity = Math.pow(10.0, (-1 * (logProb(words)) / numWords));

        return perplexity;
    }

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
        String filepath = "./././data/development.txt";

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
        System.out.println("model 1: " + model1.getPerplexity(filepath));
        System.out.println("model 2: " + model2.getPerplexity(filepath));
        System.out.println("model 3: " + model3.getPerplexity(filepath));
        System.out.println("model 4: " + model4.getPerplexity(filepath));
        System.out.println("model 5: " + model5.getPerplexity(filepath));
        System.out.println("model 6: " + model6.getPerplexity(filepath));
        System.out.println("model 7: " + model7.getPerplexity(filepath));
        System.out.println("model 8: " + model8.getPerplexity(filepath));
    }       
}
	


        