package eu.ioservices.plagio.algorithm;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is an implementation of w-shingling algorithm that produces set of unique hashed "shingles" (n-grams,
 * contiguous subsequences of tokens in a document) that can be used to gauge the similarity of two documents.
 * <br/>
 * For example, document <b>"This is a great implementation of w-shingling"</b> with {@link ShinglesAlgorithm#shingleSize}
 * ~ 2 can be tokenized as follows:
 * <br/>
 * {@link ShinglesAlgorithm#getTextShingles}:
 * <br/>
 * {this is, is a, a great, great implementation, implementation of, of w-shingling}
 * <br/>
 * {@link ShinglesAlgorithm#getHashedShingles()}:
 * <br/>
 * {-1345893908, 3239659, 1203262990, -1144809051, 228058373, 1459963972}
 *
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class ShinglesAlgorithm implements Serializable {
    private final String text;
    private final int shingleSize;

    public ShinglesAlgorithm(String text, int shingleSize) {
        this.shingleSize = shingleSize;
        this.text = text;
    }

    public Set<Integer> getDistinctHashedShingles() {
        List<String> textShingles = getTextShingles();

        return textShingles.stream()
                           .map(String::hashCode)
                           .collect(Collectors.toSet());
    }

    public List<Integer> getHashedShingles() throws IOException {
        List<String> textShingles = getTextShingles();

        return textShingles.stream()
                           .map(String::hashCode)
                           .collect(Collectors.toList());
    }

    public List<String> getTextShingles() {
        String[] words = this.text.split("\\s+");
        int shinglesAmount = words.length - shingleSize + 1;
        if (shinglesAmount < 1)
            throw new AlgorithmException("Text contains not enough words to get even 1 shingle: " + text);

        StringBuilder stringBuffer = new StringBuilder("");
        List<String> shingles = new ArrayList<>(shinglesAmount);
        for (int i = 0; i < shinglesAmount; i++) {
            for (int j = i; j < shingleSize + i - 1; j++)
                stringBuffer.append(words[j]).append(" ");

            stringBuffer.append(words[(shingleSize + i - 1)]);
            shingles.add(new String(stringBuffer));

            stringBuffer.setLength(0);
        }
        return shingles;
    }
}
