package eu.ioservices.plagio.config;

import eu.ioservices.plagio.core.processor.CoreProcessor;
import eu.ioservices.plagio.core.publisher.ResultPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 *         Created 19-Nov-15
 */
@Configuration
@PropertySource("classpath:application.properties")
public class SpringConfiguration {

    @Value("${plagio.core.processor}")
    String coreProcessorClass;

    @Value("${plagio.core.publisher}")
    String corePublisherClass;

    @Autowired
    Environment environment;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    CoreProcessor buildCoreProcessor() {
        try {
            final CoreProcessor coreProcessor = (CoreProcessor) Class.forName(coreProcessorClass).newInstance();
            environment.ge
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Cannot find CoreProcessor implementation", e);
        } catch (ReflectiveOperationException e) {
            throw new ConfigurationException(e);
        }
    }

    @Bean
    ResultPublisher buildResultPublisher() {
        try {
            return (ResultPublisher) Class.forName(corePublisherClass).newInstance();
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Cannot find ResultPublisher implementation", e);
        } catch (ReflectiveOperationException e) {
            throw new ConfigurationException(e);
        }
    }
}
