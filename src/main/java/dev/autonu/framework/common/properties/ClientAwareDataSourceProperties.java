package dev.autonu.framework.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author autonu2X
 */

@ConfigurationProperties(prefix = "common.starter.datasource")
public record ClientAwareDataSourceProperties(String url, String username, String password) {
}
