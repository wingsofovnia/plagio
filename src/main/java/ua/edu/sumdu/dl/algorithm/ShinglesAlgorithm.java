package ua.edu.sumdu.dl.algorithm;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author superuser
 *         Created 27-Mar-15
 */
public class ShinglesAlgorithm implements Serializable {
    private String text;
    private int shingleSize;

    public ShinglesAlgorithm(String text, int shingleSize) {
        this.shingleSize = shingleSize;
        this.text = text;
    }

    public Integer[] getHashedShingles(int shingleSize) throws IOException {
        String[] textShingles = getTextShingles(shingleSize);
        Integer[] hashedShingles = new Integer[textShingles.length];

        for (int i = 0; i < hashedShingles.length; i++)
            hashedShingles[i] = textShingles[i].hashCode();

        return hashedShingles;
    }

    public Integer[] getHashedShingles() throws IOException {
        return getHashedShingles(this.shingleSize);
    }

    public String[] getTextShingles(int shingleSize) {
        String[] words = this.text.split("\\s+");
        int shinglesAmount = words.length - shingleSize + 1;
        if (shinglesAmount < 1)
            throw new IllegalArgumentException("Text contains not enough words to get even 1 shingle: " + text);

        StringBuilder stringBuffer = new StringBuilder("");
        String[] shingles = new String[shinglesAmount];
        for (int i = 0; i < shinglesAmount; i++) {
            for (int j = i; j < shingleSize + i - 1; j++)
                stringBuffer.append(words[j]).append(" ");

            stringBuffer.append(words[(shingleSize + i - 1)]);
            shingles[i] = new String(stringBuffer);

            stringBuffer.setLength(0);
        }
        return shingles;
    }

    public String[] getTextShingles() {
        return getTextShingles(this.shingleSize);
    }
}
