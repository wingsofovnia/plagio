package eu.ioservices.plagio;

import eu.ioservices.plagio.config.Config;
import eu.ioservices.plagio.core.CoreProcessor;
import eu.ioservices.plagio.core.converting.Converter;
import eu.ioservices.plagio.core.processing.StringProcessorManager;
import eu.ioservices.plagio.core.publishing.ResultPublisher;
import eu.ioservices.plagio.model.Result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 *         Created 20-Nov-15
 */
public class Plagio {
    private final Config config;
    private final CoreProcessor coreProcessor;
    private final List<ResultPublisher> resultPublishers;

    public Plagio(Config config, CoreProcessor<?> coreProcessor, List<ResultPublisher> resultPublishers) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(coreProcessor);

        this.config = config;
        this.coreProcessor = coreProcessor;
        this.resultPublishers = resultPublishers;
    }

    public static PlagioBuilder builder() {
        return new PlagioBuilder();
    }

    public void process() {
        publish(results());
    }

    public void publish(List<Result> results) {
        Objects.requireNonNull(resultPublishers);
        resultPublishers.stream().forEach(publisher -> publisher.publish(results));
    }

    public void publish(Result result) {
        publish(new ArrayList<Result>(1) {{
            add(result);
        }});
    }

    public List<Result> results() {
        return coreProcessor.process(config);
    }

    public static class PlagioBuilder {
        private Config config;
        private CoreProcessor<?> coreProcessor;
        private List<ResultPublisher> resultPublishers = new ArrayList<>();

        public PlagioBuilder core(CoreProcessor<?> coreProcessor) {
            Objects.requireNonNull(coreProcessor);
            this.coreProcessor = coreProcessor;
            return this;
        }

        public PlagioBuilder config(Config config) {
            Objects.requireNonNull(config);
            this.config = config;
            return this;
        }

        public PlagioBuilder publisher(ResultPublisher resultPublisher) {
            Objects.requireNonNull(resultPublisher);
            this.resultPublishers.add(resultPublisher);
            return this;
        }

        public PlagioBuilder publisher(Collection<ResultPublisher> resultPublishers) {
            Objects.requireNonNull(resultPublishers);
            this.resultPublishers.addAll(resultPublishers);
            return this;
        }

        public PlagioBuilder converter(Converter converter) {
            Objects.requireNonNull(converter);
            this.config.converter(converter);
            return this;
        }

        public PlagioBuilder stringProcessorManager(StringProcessorManager stringProcessorManager) {
            Objects.requireNonNull(stringProcessorManager);
            this.config.stringProcessorManager(stringProcessorManager);
            return this;
        }

        public Plagio build() {
            if (config == null || coreProcessor == null)
                throw new IllegalStateException("Make sure config() and core() are set. These components are mandatory!");
            return new Plagio(config, coreProcessor, resultPublishers);
        }
    }
}
