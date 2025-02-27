package csci2020u.assignment01;

import java.io.*;
import java.util.*;

public class SpamDetector {
    private Map<String, Integer> trainHamFreq;
    private Map<String, Integer> trainSpamFreq;
    private Set<String> vocabulary;
    private List<TestFile> testFiles;
    private int totalHamFiles;
    private int totalSpamFiles;

    public SpamDetector() {
        this.trainHamFreq = new HashMap<>();
        this.trainSpamFreq = new HashMap<>();
        this.vocabulary = new HashSet<>();
        this.testFiles = new ArrayList<>();
        this.totalHamFiles = 0;
        this.totalSpamFiles = 0;
    }

    public void train() {
        String basePath = "C:\\Users\\abdul\\csci2020u\\w25-csci2020u-assignment01-a1-afzal-abulhassan\\src\\main\\resources\\data\\train";

        File hamDir1 = new File(basePath + "\\ham");
        File hamDir2 = new File(basePath + "\\ham2");
        File spamDir = new File(basePath + "\\spam");

        processEmails(hamDir1, trainHamFreq, true);
        processEmails(hamDir2, trainHamFreq, true);
        processEmails(spamDir, trainSpamFreq, false);
    }

    private void processEmails(File dir, Map<String, Integer> freqMap, boolean isHam) {
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Invalid directory: " + dir.getAbsolutePath());
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            Set<String> uniqueWords = extractWordCounts(file).keySet();
            for (String word : uniqueWords) {
                freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
            }
            if (isHam) totalHamFiles++;
            else totalSpamFiles++;
        }
    }

    private Map<String, Integer> extractWordCounts(File file) {
        Map<String, Integer> wordCounts = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] words = line.toLowerCase().replaceAll("[^a-zA-Z]", " ").split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        wordCounts.put(word, 1); // Each word counted once per file
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wordCounts;
    }

    public List<TestFile> testModel() {
        String basePath = "C:\\Users\\abdul\\csci2020u\\w25-csci2020u-assignment01-a1-afzal-abulhassan\\src\\main\\resources\\data\\test";
        File hamDir = new File(basePath + "\\ham");
        File spamDir = new File(basePath + "\\spam");

        testFiles.clear();
        testFiles.addAll(classifyDirectory(hamDir, "ham"));
        testFiles.addAll(classifyDirectory(spamDir, "spam"));

        return testFiles;
    }

    private List<TestFile> classifyDirectory(File dir, String actualClass) {
        List<TestFile> results = new ArrayList<>();

        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Invalid directory: " + dir.getAbsolutePath());
            return results;
        }

        File[] files = dir.listFiles();
        if (files == null) return results;

        for (File file : files) {
            double probability = calculateSpamProbability(extractWordCounts(file).keySet());
            results.add(new TestFile(file.getName(), probability, actualClass));
        }

        return results;
    }

    private double calculateSpamProbability(Set<String> words) {
        double eta = 0.0;

        for (String word : words) {
            int hamCount = trainHamFreq.getOrDefault(word, 0);
            int spamCount = trainSpamFreq.getOrDefault(word, 0);

            double smoothingFactor = 0.8;
            double pWS = (spamCount + smoothingFactor) / (totalSpamFiles + 2.0 * smoothingFactor);
            double pWH = (hamCount + smoothingFactor) / (totalHamFiles + 2.0 * smoothingFactor);
            double pSW = pWS / (pWS + pWH);

            if (pSW <= 0.0001) pSW = 0.0001;
            if (pSW >= 0.9999) pSW = 0.9999;

            if (pSW > 0.001 && pSW < 0.999) {
                eta += (Math.log(1 - pSW) - Math.log(pSW)) / (Math.sqrt(words.size()) + 1);
            }
        }

        return 1.0 / (1.0 + Math.exp(eta));
    }

    public double calcAccuracy() {
        int numCorrectGuesses = 0;
        int totalFiles = testFiles.size();

        for (TestFile testFile : testFiles) {
            boolean isSpam = testFile.getSpamProbability() > 0.5;
            boolean actualSpam = testFile.getActualClass().equals("spam");

            if (isSpam == actualSpam) numCorrectGuesses++;
        }

        return (totalFiles > 0) ? (double) numCorrectGuesses / totalFiles * 100 : 0.0;
    }

    public double calcPrecision() {
        int numTruePositives = 0;
        int numFalsePositives = 0;

        for (TestFile testFile : testFiles) {
            boolean isSpam = testFile.getSpamProbability() > 0.5;
            boolean actualSpam = testFile.getActualClass().equals("spam");

            if (isSpam && actualSpam) numTruePositives++;
            if (isSpam && !actualSpam) numFalsePositives++;
        }

        return (numTruePositives + numFalsePositives > 0) ?
                (double) numTruePositives / (numTruePositives + numFalsePositives) * 100 : 0.0;
    }
}
