package eu.ioservices.plagio.util;

import net.sf.junidecode.Junidecode;

import java.util.Objects;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public final class Texts {
    private static final String SPACE_ESCAPE_MATCH = "[\\s\\n]";
    private static final String SPEC_CHARACTERS_ESCAPE_MATCH = "[^A-Za-z0-9\\s]";

    private Texts() {
        throw new AssertionError("No eu.ioservices.plagio.util.Texts instances for you!");
    }

    public static String cleanFormatting(String str) {
        return Objects.requireNonNull(str).replaceAll(SPACE_ESCAPE_MATCH, " ");
    }

    public static String removeSpecialCharacters(String str) {
        return Objects.requireNonNull(str).replaceAll(SPEC_CHARACTERS_ESCAPE_MATCH, "");
    }

    public static String transliterate(String str) {
        return Junidecode.unidecode(str);
    }
}
