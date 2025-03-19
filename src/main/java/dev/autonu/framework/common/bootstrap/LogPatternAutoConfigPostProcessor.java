package dev.autonu.framework.common.bootstrap;

import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author autonu2X
 */
@Component
public class LogPatternAutoConfigPostProcessor implements EnvironmentPostProcessor {

    protected static final String PROPERTY_SOURCE_NAME = "loggingPatternAutoConfigurationProperties";

    protected static final String SPRING_LOGGING_PATTERN_CONSOLE_PROPERTY = "logging.pattern.console";

    protected static final String SPRING_LOGGING_PATTERN_FILE_PROPERTY = "logging.pattern.file";

    protected static final String SPRING_LOGGING_FILE_NAME_PROPERTY = "logging.file.name";

    protected static final String LOGGING_FILE_PATH_PROPERTY = "common.starter.logging.file.path";

    protected static final String LOGGING_PATTERN = "[%X{clientId}][%X{subClientId}][%d{${LOG_DATEFORMAT_PATTERN:yyyy-MM-dd'T'HH:mm:ss.SSSXXX}}][${spring.application.name}][%c][%M]: %m%n${LOG_EXCEPTION_CONVERSION_WORD:%wEx}";

    protected static final String LOGGING_FILE_NAME = "${spring.application.name}-${spring.profiles.active}.log";

    private final Log LOGGER;

    /**
     * @apiNote Use this one for testing
     */
    protected LogPatternAutoConfigPostProcessor() {

        this.LOGGER = null;
    }

    public LogPatternAutoConfigPostProcessor(DeferredLogFactory deferredLogFactory) {

        this.LOGGER = deferredLogFactory.getLog(LogPatternAutoConfigPostProcessor.class);
    }

    /**
     * Update or Add {@value SPRING_LOGGING_PATTERN_CONSOLE_PROPERTY} and
     * {@value SPRING_LOGGING_PATTERN_FILE_PROPERTY} property <br></br>
     * with {@value LOGGING_PATTERN} pattern.
     * It also sets {@value SPRING_LOGGING_FILE_NAME_PROPERTY}
     * based on {@value LOGGING_FILE_PATH_PROPERTY} from properties.
     *
     * @param environment will never be {@literal null}
     * @param application will never be {@literal null}
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        if (LOGGER != null) {
            LOGGER.info(String.format("Setting up '%s' and '%s' variables in environment", SPRING_LOGGING_PATTERN_CONSOLE_PROPERTY, SPRING_LOGGING_PATTERN_FILE_PROPERTY));
        }
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(SPRING_LOGGING_PATTERN_CONSOLE_PROPERTY, LOGGING_PATTERN);
        String loggingFilePath = environment.getProperty(LOGGING_FILE_PATH_PROPERTY);
        if (!environment.containsProperty(LOGGING_FILE_PATH_PROPERTY)) {
            if (LOGGER != null) {
                LOGGER.warn("Logs will be generated without appending to the logging file");
            }
        } else if (!StringUtils.hasText(loggingFilePath)) {
            if (LOGGER != null) {
                LOGGER.warn(String.format("Not valid file path found in property '%s'." + " Logs will be generated without appending to the logging file", LOGGING_FILE_PATH_PROPERTY));
            }
        } else {
            properties.put(SPRING_LOGGING_PATTERN_FILE_PROPERTY, LOGGING_PATTERN);
            String loggingFile = loggingFilePath + File.separator + LOGGING_FILE_NAME;
            properties.put(SPRING_LOGGING_FILE_NAME_PROPERTY, loggingFile);
        }
        MutablePropertySources propertySources = environment.getPropertySources();
        EnvironmentPropertyHelper.addPropertiesToEnvironment(properties, propertySources, PROPERTY_SOURCE_NAME);
    }
}
