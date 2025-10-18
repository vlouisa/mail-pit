package setup;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@TestConfiguration
@EnableConfigurationProperties(EmailProperties.class)
@EnableAsync
public class TestMailConfig {

}