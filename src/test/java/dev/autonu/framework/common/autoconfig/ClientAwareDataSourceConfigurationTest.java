package dev.autonu.framework.common.autoconfig;

import dev.autonu.framework.common.properties.ClientAwareDataSourceProperties;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author autonu2X
 */
class ClientAwareDataSourceConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(ClientAwareDataSourceConfiguration.class));

    @Test
    void givenNoProperties_whenContextCreated_thenDataSourceIsNotPresent() {

        contextRunner.run(context -> assertFalse(context.containsBean("dataSource")));
    }

    @Test
    void givenProperties_whenContextCreated_thenDataSourceIsPresent() {

        contextRunner.withPropertyValues("common.starter.datasource.url=jdbc:postgresql://localhost:5432/saas", "common.starter.datasource.username=root", "common.starter.datasource.password=saas")
                .run(context -> {
                    assertThat(context).hasSingleBean(ClientAwareDataSourceProperties.class);
                    AssertionsForClassTypes.assertThat(context.getBean(ClientAwareDataSourceProperties.class)
                            .url()).isEqualTo("jdbc:postgresql://localhost:5432/saas");
                    AssertionsForClassTypes.assertThat(context.getBean(ClientAwareDataSourceProperties.class)
                            .password()).isEqualTo("saas");
                    AssertionsForClassTypes.assertThat(context.getBean(ClientAwareDataSourceProperties.class)
                            .username()).isEqualTo("root");
                });
    }
}