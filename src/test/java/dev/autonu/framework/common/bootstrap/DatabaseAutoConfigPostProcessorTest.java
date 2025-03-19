package dev.autonu.framework.common.bootstrap;

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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author autonu2X
 */
class DatabaseAutoConfigPostProcessorTest {

    @Test
    void givenNoProperty_whenProcessed_thenPropertySourceIsPresent() {

        ConfigurableEnvironment configurableEnvironment = configurableEnvironment();
        MutablePropertySources mutablePropertySources = configurableEnvironment.getPropertySources();
        assertNotNull(mutablePropertySources);
        assertTrue(mutablePropertySources.contains(DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME), DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME + " is not present");
    }

    @Test
    void givenNoProperty_whenProcessed_thenAllExcludeConfigsPresent() {

        ConfigurableEnvironment configurableEnvironment = configurableEnvironment();
        MutablePropertySources mutablePropertySources = configurableEnvironment.getPropertySources();
        assertNotNull(mutablePropertySources);
        assertTrue(mutablePropertySources.contains(DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME), DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME + " is not present");
        MapPropertySource propertySource = (MapPropertySource) mutablePropertySources.get(DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME);
        assertNotNull(propertySource, "property source is null");
        assertTrue(propertySource.containsProperty(DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY), DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY + "is not present");
        assertEquals(String.join(",", DatabaseAutoConfigPostProcessor.DATABASE_EXCLUSION_PROPERTIES), propertySource.getProperty(DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY), DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY + "is not equal");
    }

    @Test
    void givenProperty_asMONGODB_whenProcessed_thenOnlyPostgresExcludeConfigsPresent() {

        PropertiesPropertySource propertiesPropertySource = propertiesPropertySource(DatabaseAutoConfigPostProcessor.DB_MONGO);
        ConfigurableEnvironment configurableEnvironment = configurableEnvironment(propertiesPropertySource);
        MutablePropertySources mutablePropertySources = configurableEnvironment.getPropertySources();
        assertNotNull(mutablePropertySources);
        assertTrue(mutablePropertySources.contains(DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME), DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME + " is not present");
        MapPropertySource propertySource = (MapPropertySource) mutablePropertySources.get(DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME);
        assertNotNull(propertySource, "property source is null");
        assertTrue(propertySource.containsProperty(DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY), DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY + "is not present");
        assertEquals(String.join(",", List.of("org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration", "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration", "dev.autonu.framework.starterutils" + ".TenantAwareDataSourceConfiguration")), propertySource.getProperty(DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY), DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY + " is not equal");
    }

    @Test
    void givenProperty_asPOSTGRES_whenProcessed_thenOnlyMongoExcludeConfigsPresent() {

        PropertiesPropertySource propertiesPropertySource = propertiesPropertySource(DatabaseAutoConfigPostProcessor.DB_POSTGRES);
        ConfigurableEnvironment configurableEnvironment = configurableEnvironment(propertiesPropertySource);
        MutablePropertySources mutablePropertySources = configurableEnvironment.getPropertySources();
        assertNotNull(mutablePropertySources);
        assertTrue(mutablePropertySources.contains(DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME), DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME + " is not present");
        MapPropertySource propertySource = (MapPropertySource) mutablePropertySources.get(DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME);
        assertNotNull(propertySource, "property source is null");
        assertTrue(propertySource.containsProperty(DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY), DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY + "is not present");
        assertEquals(String.join(",", List.of("org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration", "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration", "dev.autonu.framework.starterutils" + ".TenantAwareMongoDataSourceConfiguration")), propertySource.getProperty(DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY), DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY + " is not equal");
    }

    @Test
    void givenProperty_bothMONGODBandPOSTGRES_whenProcessed_thenSpringExcludePropertyIsNotPresent() {

        PropertiesPropertySource propertiesPropertySource = propertiesPropertySource(DatabaseAutoConfigPostProcessor.DB_MONGO + "," + DatabaseAutoConfigPostProcessor.DB_POSTGRES);
        ConfigurableEnvironment configurableEnvironment = configurableEnvironment(propertiesPropertySource);
        MutablePropertySources mutablePropertySources = configurableEnvironment.getPropertySources();
        assertNotNull(mutablePropertySources);
        assertTrue(mutablePropertySources.contains(DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME), DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME + " is present");
        MapPropertySource propertySource = (MapPropertySource) mutablePropertySources.get(DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME);
        assertNotNull(propertySource, "property source is null");
        assertNull(propertiesPropertySource.getProperty(DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY), DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY + " is present");
    }

    @Test
    void givenInvalidProperty_whenProcessed_thenSpringExcludePropertyIsPresent() {

        PropertiesPropertySource propertiesPropertySource = propertiesPropertySource("," + DatabaseAutoConfigPostProcessor.DB_POSTGRES);
        ConfigurableEnvironment configurableEnvironment = configurableEnvironment(propertiesPropertySource);
        MutablePropertySources mutablePropertySources = configurableEnvironment.getPropertySources();
        assertTrue(mutablePropertySources.contains(DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME), DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME + " is not present");
        MapPropertySource propertySource = (MapPropertySource) mutablePropertySources.get(DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME);
        assertNotNull(propertySource, "property source is null");
        assertTrue(propertySource.containsProperty(DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY), DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY + "is not present");
        assertEquals(String.join(",", DatabaseAutoConfigPostProcessor.DATABASE_EXCLUSION_PROPERTIES), propertySource.getProperty(DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY), DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY + "is not equal");

        propertiesPropertySource = propertiesPropertySource("MYSQL");
        configurableEnvironment = configurableEnvironment(propertiesPropertySource);
        mutablePropertySources = configurableEnvironment.getPropertySources();
        assertTrue(mutablePropertySources.contains(DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME), DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME + " is not present");
        propertySource = (MapPropertySource) mutablePropertySources.get(DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME);
        assertNotNull(propertySource, "property source is null");
        assertTrue(propertySource.containsProperty(DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY), DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY + "is not present");
        assertEquals(String.join(",", DatabaseAutoConfigPostProcessor.DATABASE_EXCLUSION_PROPERTIES), propertySource.getProperty(DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY), DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY + "is not equal");

        propertiesPropertySource = propertiesPropertySource("");
        configurableEnvironment = configurableEnvironment(propertiesPropertySource);
        mutablePropertySources = configurableEnvironment.getPropertySources();
        assertTrue(mutablePropertySources.contains(DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME), DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME + " is not present");
        propertySource = (MapPropertySource) mutablePropertySources.get(DatabaseAutoConfigPostProcessor.PROPERTY_SOURCE_NAME);
        assertNotNull(propertySource, "property source is null");
        assertTrue(propertySource.containsProperty(DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY), DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY + "is not present");
        assertEquals(String.join(",", DatabaseAutoConfigPostProcessor.DATABASE_EXCLUSION_PROPERTIES), propertySource.getProperty(DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY), DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY + "is not equal");
    }

    private PropertiesPropertySource propertiesPropertySource(String value) {

        Properties properties = new Properties();
        if (StringUtils.hasText(value)) {
            properties.setProperty(DatabaseAutoConfigPostProcessor.INCLUDE_DATABASE_PROPERTY, value);
        }
        return new PropertiesPropertySource("testProperties", properties);
    }

    private ConfigurableEnvironment configurableEnvironment() {

        return configurableEnvironment(null);
    }

    private ConfigurableEnvironment configurableEnvironment(PropertiesPropertySource propertiesPropertySource) {

        EnvironmentPostProcessor environmentPostProcessor = new DatabaseAutoConfigPostProcessor();
        SpringApplication springApplication = new SpringApplicationBuilder().sources(DatabaseAutoConfigPostProcessor.class)
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