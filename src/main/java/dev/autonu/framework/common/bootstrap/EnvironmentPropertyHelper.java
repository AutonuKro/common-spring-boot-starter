package dev.autonu.framework.common.bootstrap;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.autonu.framework.common.bootstrap.DatabaseAutoConfigPostProcessor.SPRING_EXCLUDE_PROPERTY;

/**
 * @author autonu2X
 */
public class EnvironmentPropertyHelper {

    /**
     * Adds database exclusion properties to `spring.autoconfigure.exclude`.
     *
     * @param environment        the Spring environment (never {@literal null})
     * @param propertySourceName the name of the property source (never {@literal null})
     * @param excludedAutoConfigs list of auto-configurations to exclude (never {@literal null})
     */
    public static void addDatabaseExclusionProperties(ConfigurableEnvironment environment, String propertySourceName, List<String> excludedAutoConfigs) {

        MutablePropertySources propertySources = environment.getPropertySources();
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(SPRING_EXCLUDE_PROPERTY, String.join(",", excludedAutoConfigs));
        addPropertiesToEnvironment(properties, propertySources, propertySourceName);
    }

    /**
     * Adds properties to the Spring application environment.
     *
     * @param properties         the properties to add (never {@literal null})
     * @param propertySources    the property sources (never {@literal null})
     * @param propertySourceName the name of the property source (never {@literal null})
     */
    public static void addPropertiesToEnvironment(Map<String, Object> properties, MutablePropertySources propertySources, String propertySourceName) {

        MapPropertySource source = (MapPropertySource) propertySources.get(propertySourceName);
        if (source == null) {
            MapPropertySource target = new MapPropertySource(propertySourceName, properties);
            propertySources.addFirst(target);
        } else {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                source.getSource().put(entry.getKey(), entry.getValue());
            }
        }
    }
}

