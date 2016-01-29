package eu.ioservices.plagio.util;

import eu.ioservices.plagio.algorithm.HashShinglesAlgorithm;
import eu.ioservices.plagio.algorithm.ShinglesAlgorithm;
import net.sf.junidecode.Junidecode;

import java.util.*;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public final class Texts {
    private static final String SPACE_ESCAPE_MATCH = "[\\s\\n]";
    private static final String SPEC_CHARACTERS_ESCAPE_MATCH = "[^A-Za-z0-9\\s]";
    private static final List<String> STOP_WORDS = new ArrayList<>(Arrays.asList(new String[]{"a", "about", "above",
            "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't", "as", "at",
            "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can't",
            "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't",
            "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't",
            "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers",
            "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if",
            "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most",
            "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or",
            "other", "ought", "our", "ours"}));

    private Texts() {
        throw new AssertionError("No eu.ioservices.plagio.util.Texts instances for you!");
    }

    public static String cleanFormatting(String str) {
        return Objects.requireNonNull(str).replaceAll(SPACE_ESCAPE_MATCH, " ").toLowerCase();
    }

    public static String removeSpecialCharacters(String str) {
        return Objects.requireNonNull(str).replaceAll(SPEC_CHARACTERS_ESCAPE_MATCH, "");
    }

    public static String removeStopWords(String str) {
        final List<String> text = new LinkedList<>(Arrays.asList(str.split("\\s+")));

        Iterator<String> textIterator = text.iterator();
        while (textIterator.hasNext())
            if (STOP_WORDS.contains(textIterator.next().toLowerCase()))
                textIterator.remove();

        return text.stream().reduce((str1, str2) -> str1 + " " + str2).get();
    }

    public static String transliterate(String str) {
        return Junidecode.unidecode(str);
    }
}
