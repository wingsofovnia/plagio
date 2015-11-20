package eu.ioservices.plagio;

import eu.ioservices.plagio.config.Config;
import eu.ioservices.plagio.core.publishing.ResultPublisher;
import eu.ioservices.plagio.core.processing.CoreProcessor;
import eu.ioservices.plagio.model.Result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 *         Created 20-Nov-15
 */
public class Plagio {
    private final Config config;
    private final CoreProcessor coreProcessor;
    private final List<ResultPublisher> resultPublishers;

    public Plagio(Config config, CoreProcessor coreProcessor, List<ResultPublisher> resultPublishers) {
        this.config = config;
        this.coreProcessor = coreProcessor;
        this.resultPublishers = resultPublishers;
    }

    public void process() {
        publish(results());
    }

    public void publish(List<Result> results) {
        resultPublishers.stream().forEach(publisher -> publisher.publish(results));
    }

    public void publish(Result result) {
        resultPublishers.stream().forEach(publisher -> publisher.publish(result));
    }

    public List<Result> results() {
        return coreProcessor.process(config);
    }

    public static PlagioBuilder builder() {
        return new PlagioBuilder();
    }

    public static class PlagioBuilder {
        private Config config;
        private CoreProcessor coreProcessor;
        private List<ResultPublisher> resultPublishers = new ArrayList<>();

        public PlagioBuilder core(CoreProcessor coreProcessor) {
            this.coreProcessor = coreProcessor;
            return this;
        }

        public PlagioBuilder config(Config config) {
            this.config = config;
            return this;
        }

        public PlagioBuilder publisher(ResultPublisher resultPublisher) {
            this.resultPublishers.add(resultPublisher);
            return this;
        }

        public PlagioBuilder publisher(Collection<ResultPublisher> resultPublishers) {
            this.resultPublishers.addAll(resultPublishers);
            return this;
        }

        public Plagio build() {
            return new Plagio(config, coreProcessor, resultPublishers);
        }
    }
}
