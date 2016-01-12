package eu.ioservices.plagio.algorithm;

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
    private static final String SPACE_ESCAPE_MATCH = "[\\s\\n]";
    private static final String SPEC_CHARACTERS_ESCAPE_MATCH = "[^A-Za-z0-9\\s]";
    private static final int DEFAULT_SHINGLE_SIZE = 4;
    private static final boolean DEFAULT_NORMALIZING_SWITCH = true;
    private String text;
    private int shingleSize;
    private boolean isNormalizing;
    public ShinglesAlgorithm(String text) {
        this(text, DEFAULT_NORMALIZING_SWITCH, DEFAULT_SHINGLE_SIZE);
    }

    public ShinglesAlgorithm(String text, boolean normalize) {
        this(text, normalize, DEFAULT_SHINGLE_SIZE);
    }

    public ShinglesAlgorithm(String text, boolean normalize, int shingleSize) {
        this.isNormalizing = normalize;
        this.shingleSize = shingleSize;
        this.text = Objects.requireNonNull(text);
    }

    private static String normalizeSpaces(String str) {
        return str.replaceAll(SPACE_ESCAPE_MATCH, " ").trim();
    }

    private static String removeSpecialCharacters(String str) {
        return str.replaceAll(SPEC_CHARACTERS_ESCAPE_MATCH, "");
    }

    public Set<Shingle> getDistinctHashedShingles() throws IOException {
        return new HashSet<>(this.getHashedShingles());
    }

    public List<Shingle> getHashedShingles() throws IOException {
        List<String> textShingles = getTextShingles();

        return textShingles.stream()
                .map(String::hashCode)
                .map(Shingle::new)
                .collect(Collectors.toList());
    }

    public List<String> getTextShingles() {
        String content;
        if (this.isNormalizing()) {
            content = normalizeSpaces(this.text);
            content = removeSpecialCharacters(content);
        } else {
            content = this.text;
        }

        String[] words = content.split("\\s+");
        int totalShingles = words.length - shingleSize + 1;
        if (totalShingles < 1)
            throw new AlgorithmException("Text contains not enough words to get even 1 shingle: " + text);

        StringBuilder stringBuffer = new StringBuilder();
        List<String> shingles = new ArrayList<>(totalShingles);
        for (int i = 0; i < totalShingles; i++) {
            for (int j = i; j < i + shingleSize; j++) {
                stringBuffer.append(words[j]).append(" ");
            }
            stringBuffer.setLength(Math.max(stringBuffer.length() - 1, 0));
            shingles.add(stringBuffer.toString());
            stringBuffer.setLength(0);
        }
        return shingles;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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