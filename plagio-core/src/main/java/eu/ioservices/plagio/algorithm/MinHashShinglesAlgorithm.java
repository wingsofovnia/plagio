package eu.ioservices.plagio.algorithm;

import java.util.*;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class MinHashShinglesAlgorithm extends HashShinglesAlgorithm {
    public List<Integer> getHashedShingles(String text){
        return this.getHashedShingles(text, DEFAULT_SHINGLE_SIZE);
    }

    public List<Integer> getHashedShingles(String text, int wordsInShingle){
        return this.getHashedShingles(text, wordsInShingle, wordsInShingle - 2);
    }

    public List<Integer> getHashedShingles(String text, int wordsInShingle, int minHashStep){
        if (minHashStep < 1)
            throw new IllegalArgumentException("MinHashStep must be bigger than 1");

        final List<Integer> hashedShingles = super.getHashedShingles(text, wordsInShingle);

        final List<Integer> minHashedShingles = new ArrayList<>(hashedShingles.size() / minHashStep + 1);
        for (int i = 0; i < hashedShingles.size(); i += minHashStep + 1) {
            int endPos = i + minHashStep;
            if (endPos > hashedShingles.size() - 1)
                endPos = hashedShingles.size() - 1;

            final Integer minShingleHash = min(hashedShingles, i, endPos);
            minHashedShingles.add(minShingleHash);
        }

        return minHashedShingles;
    }

    private Integer min(List<Integer> list, int start, int end) {
        if (Objects.requireNonNull(list).size() == 0)
            throw new IllegalArgumentException("List is null");
        if (start < 0 || start >= list.size())
            throw new IllegalArgumentException("Start is < 0 or is bigger than list size");
        if (start > end)
            throw new IllegalArgumentException("Start is > end!");
        if (end >= list.size())
            throw new IllegalArgumentException("End is bigger than list size");

        Integer min = list.get(start);
        for (int i = start + 1; i <= end; i++) {
            final Integer current = list.get(i);
            if (min > current)
                min = current;
        }

        return min;
    }

    public Set<Integer> getDistinctHashedShingles(String text){
        return new HashSet<>(this.getHashedShingles(text));
    }

    public Set<Integer> getDistinctMinHashedShingles(String text, int wordsInShingle){
        return new HashSet<>(this.getHashedShingles(text, wordsInShingle));
    }

    public Set<Integer> getDistinctMinHashedShingles(String text, int wordsInShingle, int minHashStep){
        return new HashSet<>(this.getHashedShingles(text, wordsInShingle, minHashStep));
    }
}
