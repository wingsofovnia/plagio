package ua.edu.sumdu.dl.parsing.processor;

/**
 * @author superuser
 *         Created 28-Mar-15
 */
public class RemoveSpaceStringProcessor implements IProcessor<String> {
    private static final String SPACE_ESCAPE_MATCH = "[\\s\\n]";

    @Override
    public String process(String str) {
        return str.replaceAll(SPACE_ESCAPE_MATCH, " ").trim();
    }
}
