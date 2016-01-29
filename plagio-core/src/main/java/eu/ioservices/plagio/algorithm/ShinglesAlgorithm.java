package eu.ioservices.plagio.algorithm;

import java.io.Serializable;
import java.util.*;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class ShinglesAlgorithm implements Serializable {
    public static final int DEFAULT_SHINGLE_SIZE = 3;

    public List<String> getTextShingles(String text) {
        return this.getTextShingles(text, DEFAULT_SHINGLE_SIZE);
    }

    public List<String> getTextShingles(String text, int wordsInShingle) {
        if (wordsInShingle <= 0)
            throw new IllegalArgumentException("Shingle size must be bigger that 0");

        String[] words = Objects.requireNonNull(text).split("\\s+");
        int totalShingles = words.length - wordsInShingle + 1;

        if (totalShingles < 1)
            throw new IllegalArgumentException("Text contains not enough words to get even 1 shingle: " + text);

        StringBuilder stringBuffer = new StringBuilder();
        List<String> shingles = new ArrayList<>(totalShingles);
        for (int i = 0; i < totalShingles; i++) {
            for (int j = i; j < i + wordsInShingle; j++) {
                stringBuffer.append(words[j]).append(" ");
            }

            stringBuffer.setLength(Math.max(stringBuffer.length() - 1, 0));
            shingles.add(stringBuffer.toString());
            stringBuffer.setLength(0);
        }
        return shingles;
    }

    public Set<String> getDistinctTextShingles(String text) {
        return new HashSet<>(this.getTextShingles(text));
    }

    public Set<String> getDistinctTextShingles(String text, int wordsInShingle) {
        return new HashSet<>(this.getTextShingles(text, wordsInShingle));
    }
}
