package eu.ioservices.plagio.core.publisher;

import eu.ioservices.plagio.model.Result;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 *         Created 19-Nov-15
 */
public class ConsoleResultPublisher implements ResultPublisher {
    private static OutputStream CONSOLE_OUT = System.out;

    @Override
    public void publish(Result result)  throws ResultPublishingException {
        publish(new ArrayList<Result>(1) {{
            add(result);
        }});
    }

    @Override
    public void publish(Collection<Result> results) throws ResultPublishingException {
        StringBuilder outputBuilder = new StringBuilder();
        if (results.size() == 0) {
            outputBuilder.append("No results has been generated! Output missed.");
        } else {
            outputBuilder.append("#-------------------------------------------# \n");
            for (Result r : results) {
                outputBuilder.append("  -> Document #").append(r.getDocName()).append("\n");
                outputBuilder.append("     Total shingles: ").append(r.getDocShingles()).append("\n");
                outputBuilder.append("     Coincides: ").append(r.getCoincidences()).append("\n");
                outputBuilder.append("     PLAGIARISM LEVEL: ").append((int) r.getDuplicationLevel()).append("% \n");
                outputBuilder.append("\n");
            }
            outputBuilder.deleteCharAt(outputBuilder.length() - 1);
            outputBuilder.append("#-------------------------------------------# \n");
        }
        try {
            CONSOLE_OUT.write(outputBuilder.toString().getBytes("UTF-8"));
        } catch (IOException e) {
            throw new ResultPublishingException(e);
        }
    }
}
