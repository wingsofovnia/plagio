package ua.edu.sumdu.dl.parsing.processor;

import net.sf.junidecode.Junidecode;

/**
 * @author superuser
 *         Created 28-Mar-15
 */
public class TransliterateStringProcessor implements IProcessor<String> {

    @Override
    public String process(String str) {
        return Junidecode.unidecode(str);
    }
}
