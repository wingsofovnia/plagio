package eu.ioservices.plagio.algorithm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class HashShinglesAlgorithm extends ShinglesAlgorithm {
    public List<Integer> getHashedShingles(String text){
        return getHashedShingles(text, DEFAULT_SHINGLE_SIZE);
    }

    public List<Integer> getHashedShingles(String text, int wordsInShingle){
        List<String> textShingles = getTextShingles(text, wordsInShingle);

        return textShingles.stream()
                .map(textShingle -> Arrays.asList(textShingle.split("\\s+"))
                                          .stream()
                                          .map(String::hashCode)
                                          .reduce((hash1, hash2) -> hash1 + hash2)
                                          .get())
                .collect(Collectors.toList());
    }


    public Set<Integer> getDistinctHashedShingles(String text){
        return new HashSet<>(this.getHashedShingles(text));
    }

    public Set<Integer> getDistinctHashedShingles(String text, int wordsInShingle){
        return new HashSet<>(this.getHashedShingles(text, wordsInShingle));
    }
}
