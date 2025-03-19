package dev.autonu.framework.common.autoconfig;

import com.zaxxer.hikari.HikariDataSource;
import dev.autonu.framework.common.context.ClientAwareDataSource;
import dev.autonu.framework.common.properties.ClientAwareDataSourceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * @author autonu2X
 */
@AutoConfiguration
@EnableConfigurationProperties(ClientAwareDataSourceProperties.class)
public class ClientAwareDataSourceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientAwareDataSourceConfiguration.class);

    private final ClientAwareDataSourceProperties dataSourceProperties;

    public ClientAwareDataSourceConfiguration(ClientAwareDataSourceProperties dataSourceProperties) {

        this.dataSourceProperties = dataSourceProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "common.starter.datasource", name = {"url", "username", "password"})
    public DataSource dataSource() {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating Client-Aware DataSource with properties: {}", dataSourceProperties);
        }
        HikariDataSource dataSource = new ClientAwareDataSource();
        dataSource.setJdbcUrl(dataSourceProperties.url());
        dataSource.setUsername(dataSourceProperties.username());
        dataSource.setPassword(dataSourceProperties.password());
        return dataSource;
    }
}
