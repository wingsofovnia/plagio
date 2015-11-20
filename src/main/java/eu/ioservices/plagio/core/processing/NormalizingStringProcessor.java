package eu.ioservices.plagio.core.processing;

import net.sf.junidecode.Junidecode;

/**
 * Created by u548850 on 11/20/2015.
 */
public class NormalizingStringProcessor implements StringProcessor {
    private static final String SPACE_ESCAPE_MATCH = "[\\s\\n]";
    private static final String SPEC_CHARACTERS_ESCAPE_MATCH = "[^A-Za-z0-9\\s]";

    @Override
    public String process(String str) {
        str = normalizeSpaces(str);
        str = removeSpecialCharacters(str);
        return transliterate(str);
    }

    private String normalizeSpaces(String str) {
        return str.replaceAll(SPACE_ESCAPE_MATCH, " ").trim();
    }

    private String removeSpecialCharacters(String str) {
        return str.replaceAll(SPEC_CHARACTERS_ESCAPE_MATCH, "");
    }

    private String transliterate(String str) {
        return Junidecode.unidecode(str);
    }
}
