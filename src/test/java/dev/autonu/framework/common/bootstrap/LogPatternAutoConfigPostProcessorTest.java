package dev.autonu.framework.common.bootstrap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.File;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author autonu2X
 */
class LogPatternAutoConfigPostProcessorTest {

    @Test
    void givenProperty_whenProcessed_thenPresentAllLoggingProperties() {

        String logFilePath = Paths.get(".", "src", "test", "resources")
                .toString();
        PropertiesPropertySource propertiesPropertySource = propertiesPropertySource(logFilePath);
        ConfigurableEnvironment configurableEnvironment = configurableEnvironment(propertiesPropertySource);
        MutablePropertySources mutablePropertySources = configurableEnvironment.getPropertySources();
        assertNotNull(mutablePropertySources);
        assertTrue(mutablePropertySources.contains(LogPatternAutoConfigPostProcessor.PROPERTY_SOURCE_NAME), LogPatternAutoConfigPostProcessor.PROPERTY_SOURCE_NAME + " is not present");
        MapPropertySource mapPropertySource = (MapPropertySource) mutablePropertySources.get(LogPatternAutoConfigPostProcessor.PROPERTY_SOURCE_NAME);
        assert mapPropertySource != null;
        assertTrue(mapPropertySource.containsProperty(LogPatternAutoConfigPostProcessor.SPRING_LOGGING_PATTERN_CONSOLE_PROPERTY), LogPatternAutoConfigPostProcessor.SPRING_LOGGING_PATTERN_CONSOLE_PROPERTY + " is not present");
        assertTrue(mapPropertySource.containsProperty(LogPatternAutoConfigPostProcessor.SPRING_LOGGING_PATTERN_FILE_PROPERTY), LogPatternAutoConfigPostProcessor.SPRING_LOGGING_PATTERN_FILE_PROPERTY + " is not present");
        assertTrue(mapPropertySource.containsProperty(LogPatternAutoConfigPostProcessor.SPRING_LOGGING_FILE_NAME_PROPERTY), LogPatternAutoConfigPostProcessor.SPRING_LOGGING_FILE_NAME_PROPERTY + " is not present");
        Assertions.assertEquals(LogPatternAutoConfigPostProcessor.LOGGING_PATTERN, mapPropertySource.getProperty(LogPatternAutoConfigPostProcessor.SPRING_LOGGING_PATTERN_CONSOLE_PROPERTY), "console");
        Assertions.assertEquals(LogPatternAutoConfigPostProcessor.LOGGING_PATTERN, mapPropertySource.getProperty(LogPatternAutoConfigPostProcessor.SPRING_LOGGING_PATTERN_FILE_PROPERTY), "file");
        String fileName = logFilePath + File.separator + LogPatternAutoConfigPostProcessor.LOGGING_FILE_NAME;
        assertEquals(fileName, mapPropertySource.getProperty(LogPatternAutoConfigPostProcessor.SPRING_LOGGING_FILE_NAME_PROPERTY));
    }

    @Test
    void givenNoProperty_whenProcessed_thenPresentOnlyConsoleLoggingProperties() {

        ConfigurableEnvironment configurableEnvironment = configurableEnvironment();
        MutablePropertySources mutablePropertySources = configurableEnvironment.getPropertySources();
        assertNotNull(mutablePropertySources);
        assertTrue(mutablePropertySources.contains(LogPatternAutoConfigPostProcessor.PROPERTY_SOURCE_NAME), LogPatternAutoConfigPostProcessor.PROPERTY_SOURCE_NAME + " is not present");
        MapPropertySource mapPropertySource = (MapPropertySource) mutablePropertySources.get(LogPatternAutoConfigPostProcessor.PROPERTY_SOURCE_NAME);
        assert mapPropertySource != null;
        assertTrue(mapPropertySource.containsProperty(LogPatternAutoConfigPostProcessor.SPRING_LOGGING_PATTERN_CONSOLE_PROPERTY), LogPatternAutoConfigPostProcessor.SPRING_LOGGING_PATTERN_CONSOLE_PROPERTY + " is not present");
        assertNull(mapPropertySource.getProperty(LogPatternAutoConfigPostProcessor.SPRING_LOGGING_PATTERN_FILE_PROPERTY), LogPatternAutoConfigPostProcessor.SPRING_LOGGING_PATTERN_FILE_PROPERTY + " is present");
        assertNull(mapPropertySource.getProperty(LogPatternAutoConfigPostProcessor.SPRING_LOGGING_FILE_NAME_PROPERTY), LogPatternAutoConfigPostProcessor.SPRING_LOGGING_FILE_NAME_PROPERTY + " is present");
        Assertions.assertEquals(LogPatternAutoConfigPostProcessor.LOGGING_PATTERN, mapPropertySource.getProperty(LogPatternAutoConfigPostProcessor.SPRING_LOGGING_PATTERN_CONSOLE_PROPERTY));
    }

    private PropertiesPropertySource propertiesPropertySource(String value) {

        Properties properties = new Properties();
        properties.setProperty(LogPatternAutoConfigPostProcessor.LOGGING_FILE_PATH_PROPERTY, value);
        return new PropertiesPropertySource("testProperties", properties);
    }

    private ConfigurableEnvironment configurableEnvironment() {

        return configurableEnvironment(null);
    }

    private ConfigurableEnvironment configurableEnvironment(PropertiesPropertySource propertiesPropertySource) {

        EnvironmentPostProcessor environmentPostProcessor = new LogPatternAutoConfigPostProcessor();
        SpringApplication springApplication = new SpringApplicationBuilder().sources(LogPatternAutoConfigPostProcessor.class)
                .web(WebApplicationType.NONE)
                .properties("spring.cloud.config.enabled=false", "spring.application.name=test")
                .build();
        ConfigurableApplicationContext context = springApplication.run();
        if (propertiesPropertySource != null) {
            context.getEnvironment()
                    .getPropertySources()
                    .addFirst(propertiesPropertySource);
        }
        environmentPostProcessor.postProcessEnvironment(context.getEnvironment(), springApplication);
        ConfigurableEnvironment environment = context.getEnvironment();
        context.close();
        return environment;
    }
}