package eu.ioservices.plagio.algorithm;

import eu.ioservices.plagio.util.Texts;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
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
    public static final int DEFAULT_SHINGLE_SIZE = 4;
    public static final boolean DEFAULT_NORMALIZING_SWITCH = true;
    private int shingleSize;
    private boolean isNormalizing;

    public ShinglesAlgorithm() {
        this(DEFAULT_NORMALIZING_SWITCH, DEFAULT_SHINGLE_SIZE);
    }

    public ShinglesAlgorithm(boolean normalize) {
        this(normalize, DEFAULT_SHINGLE_SIZE);
    }

    public ShinglesAlgorithm(boolean normalize, int shingleSize) {
        this.isNormalizing = normalize;
        this.shingleSize = shingleSize;
    }

    public Set<Shingle> getDistinctHashedShingles(String text) throws IOException {
        return new HashSet<>(this.getHashedShingles(text));
    }

    public List<Shingle> getHashedShingles(String text) throws IOException {
        List<String> textShingles = getTextShingles(text);

        return textShingles.stream()
                .map(String::hashCode)
                .map(Shingle::new)
                .collect(Collectors.toList());
    }

    public List<String> getTextShingles(String text) {
        String content = this.isNormalizing() ? this.normalizeString(text) : text;
        String[] words = content.split("\\s+");
        int totalShingles = words.length - shingleSize + 1;

        if (totalShingles < 1)
            throw new AlgorithmException("Text contains not enough words to get even 1 shingle: " + text);

        StringBuilder stringBuffer = new StringBuilder();
        List<String> shingles = new ArrayList<>(totalShingles);
        for (int i = 0; i < totalShingles; i++) {
            for (int j = i; j < i + shingleSize; j++)
                stringBuffer.append(words[j]).append(" ");

            stringBuffer.setLength(Math.max(stringBuffer.length() - 1, 0));
            shingles.add(stringBuffer.toString());
            stringBuffer.setLength(0);
        }
        return shingles;
    }

    public int getShingleSize() {
        return shingleSize;
    }

    public void setShingleSize(int shingleSize) {
        this.shingleSize = shingleSize;
    }

    public boolean isNormalizing() {
        return isNormalizing;
    }

    public void setNormalizing(boolean isNormalizing) {
        this.isNormalizing = isNormalizing;
    }

    private String normalizeString(String str) {
        String nString = Texts.cleanFormatting(str);
        nString = Texts.removeSpecialCharacters(nString);
        nString = Texts.transliterate(nString);

        return nString;
    }

    public static final class Shingle implements Serializable {
        private final int shingle;

        public Shingle(int shingle) {
            this.shingle = shingle;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;

            if (!(o instanceof Shingle))
                return false;

            Shingle shingle1 = (Shingle) o;
            return shingle == shingle1.shingle;
        }

        @Override
        public int hashCode() {
            return shingle;
        }

        @Override
        public String toString() {
            return "Shingle{shingle=" + shingle + '}';
        }
    }

}