package ua.cv.ovchynnikov.processing.algorithm;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author superuser
 *         Created 27-Mar-15
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
            throw new IllegalArgumentException("Text contains not enough words to get even 1 shingle: " + text);

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
