package eu.ioservices.plagio;

import eu.ioservices.plagio.config.AppConfiguration;
import eu.ioservices.plagio.config.SpringConfiguration;
import eu.ioservices.plagio.core.processor.CoreProcessor;
import eu.ioservices.plagio.core.publisher.ResultPublisher;
import eu.ioservices.plagio.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Entry point of Plagio application.
 * <br/>
 * Here Plagio configures itself, determine {@link eu.ioservices.plagio.core.processor.CoreProcessor} implementation and delegate
 * further processing to underling core implementation.
 *
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */

@Component
public class Application {
    CoreProcessor coreProcessor;
    ResultPublisher resultPublisher;

    @Autowired
    public Application(CoreProcessor coreProcessor, ResultPublisher resultPublisher) {
        this.coreProcessor = coreProcessor;
        this.resultPublisher = resultPublisher;
    }

    @PostConstruct
    void process() {
        final List<Result> process = coreProcessor.process(new AppConfiguration("config.properties"));
    }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfiguration.class);
    }
}
