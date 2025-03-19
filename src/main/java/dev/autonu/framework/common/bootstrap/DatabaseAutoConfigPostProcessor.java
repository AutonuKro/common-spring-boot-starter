package dev.autonu.framework.common.bootstrap;

import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author autonu2X
 */
@Component
public class DatabaseAutoConfigPostProcessor implements EnvironmentPostProcessor {

    public static final String SPRING_EXCLUDE_PROPERTY = "spring.autoconfigure.exclude";

    protected static final String PROPERTY_SOURCE_NAME = "databaseExclusionAutoConfigurationProperties";

    protected static final String INCLUDE_DATABASE_PROPERTY = "common.starter.datasource.include";

    protected static final String DB_MONGO = "MONGODB";

    protected static final String DB_POSTGRES = "POSTGRES";

    protected static final Map<String, List<String>> EXCLUSION_PROPERTIES_BY_DATABASE;

    protected static final List<String> DATABASE_EXCLUSION_PROPERTIES;

    static {
        final List<String> mongoProperties = List.of("org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration", "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration", "dev.autonu.framework.starterutils.TenantAwareMongoDataSourceConfiguration");
        final List<String> postgresProperties = List.of("org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration", "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration", "dev.autonu.framework.starterutils.TenantAwareDataSourceConfiguration");
        EXCLUSION_PROPERTIES_BY_DATABASE = new HashMap<>();
        EXCLUSION_PROPERTIES_BY_DATABASE.put(DB_MONGO, mongoProperties);
        EXCLUSION_PROPERTIES_BY_DATABASE.put(DB_POSTGRES, postgresProperties);
        DATABASE_EXCLUSION_PROPERTIES = new ArrayList<>(mongoProperties);
        DATABASE_EXCLUSION_PROPERTIES.addAll(postgresProperties);
    }

    private final Log LOGGER;

    /**
     * @apiNote Use this one for testing
     */
    protected DatabaseAutoConfigPostProcessor() {

        this.LOGGER = null;
    }

    public DatabaseAutoConfigPostProcessor(DeferredLogFactory deferredLogFactory) {

        this.LOGGER = deferredLogFactory.getLog(DatabaseAutoConfigPostProcessor.class);
    }

    /**
     * Update or Add {@value SPRING_EXCLUDE_PROPERTY} property to disable <br>
     * based on {@value INCLUDE_DATABASE_PROPERTY} property.
     *
     * @param environment will never be {@literal null}
     * @param application will never be {@literal null}
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        if (LOGGER != null) {
            LOGGER.info("Checking if is there any auto configuration for database to add.");
        }
        List<String> excludedAutoConfigs = new ArrayList<>(DATABASE_EXCLUSION_PROPERTIES);
        String dbToBeIncluded = environment.getProperty(INCLUDE_DATABASE_PROPERTY);
        if (!StringUtils.hasText(dbToBeIncluded)) {
            if (LOGGER != null) {
                LOGGER.warn(String.format("No database has been added to the application. " + "Please provide value for %s if this is not intended", INCLUDE_DATABASE_PROPERTY));
                EnvironmentPropertyHelper.addDatabaseExclusionProperties(environment, PROPERTY_SOURCE_NAME, excludedAutoConfigs);
            }
            return;
        }
        String[] dbs = dbToBeIncluded.split(",");
        for (String db : dbs) {
            if (db == null) {
                if (LOGGER != null) {
                    LOGGER.warn(String.format("Invalid value provided for property '%s'. " + "Please provide correct value %s or %s. " + "Falling back to default auto configuration", INCLUDE_DATABASE_PROPERTY, DB_MONGO, DB_POSTGRES));
                }
                return;
            }
            String database = db.trim();
            if (EXCLUSION_PROPERTIES_BY_DATABASE.containsKey(database)) {
                removeFromDbExclusionProperties(excludedAutoConfigs, database);
            } else {
                if (LOGGER != null) {
                    LOGGER.warn(String.format("Invalid value provided for property '%s'. " + "Please provide correct value %s or %s. " + "Falling back to default auto configuration", INCLUDE_DATABASE_PROPERTY, DB_MONGO, DB_POSTGRES));
                }
                return;
            }
        }
        if (excludedAutoConfigs.isEmpty()) {
            if (LOGGER != null) {
                LOGGER.info("Adding all data configuration");
            }
            return;
        }
        EnvironmentPropertyHelper.addDatabaseExclusionProperties(environment, PROPERTY_SOURCE_NAME, excludedAutoConfigs);
    }

    private void removeFromDbExclusionProperties(List<String> excludedDbConfigs, String database) {

        if (LOGGER != null) {
            LOGGER.info(String.format("Adding database configuration for: %s", database));
        }
        List<String> autoConfigurations = EXCLUSION_PROPERTIES_BY_DATABASE.get(database);
        excludedDbConfigs.removeAll(autoConfigurations);
    }
}
