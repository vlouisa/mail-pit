package dev.louisa.victor.mail.pit.docker;

import dev.louisa.victor.mail.pit.api.MailPitApi;
import dev.louisa.victor.mail.pit.api.MailPitUtil;
import lombok.RequiredArgsConstructor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.testcontainers.utility.MountableFile.forClasspathResource;

@Testcontainers
@RequiredArgsConstructor
public class MailPitContainer {
    private static final String SMTP_USER = "mail-pit-user";
    private static final String SMTP_PASSWORD = "mail-pit-password";

    private static final Integer DOCKER_CONTAINER_PORT_SMTP = 1025;
    private static final Integer DOCKER_CONTAINER_PORT_HTTP = 8025;
    private static final Integer HOST_PORT_SMTP = 2525;
    private static final Integer HOST_PORT_HTTP = 8025;
    
    @Container
    public final static GenericContainer<?> DOCKER_CONTAINER = new GenericContainer<>("axllent/mailpit")
            .withExposedPorts(DOCKER_CONTAINER_PORT_SMTP, DOCKER_CONTAINER_PORT_HTTP)
            .withEnv("MP_SMTP_AUTH", credentials(SMTP_USER, SMTP_PASSWORD))
            .withEnv("MP_SMTP_REQUIRE_STARTTLS", "true")
            .withEnv("MP_SMTP_TLS_CERT", "/etc/ssl/certs/cert.pem")
            .withEnv("MP_SMTP_TLS_KEY", "/etc/ssl/certs/key.pem")
            .withEnv("MP_ENABLE_CHAOS", "true")
            .withCopyFileToContainer(
                    forClasspathResource("mail-pit/certs/server-cert.pem"),
                    "/etc/ssl/certs/cert.pem")
            .withCopyFileToContainer(
                    forClasspathResource("mail-pit/certs/server-key.pem"),
                    "/etc/ssl/certs/key.pem")
            .waitingFor(Wait.forHttp("/").forPort(DOCKER_CONTAINER_PORT_HTTP))
            .withLogConsumer(outputFrame -> System.out.println(outputFrame.getUtf8String()));

    public void start() {
        DOCKER_CONTAINER.setPortBindings(
                List.of(
                        binding(HOST_PORT_SMTP, DOCKER_CONTAINER_PORT_SMTP),
                        binding(HOST_PORT_HTTP, DOCKER_CONTAINER_PORT_HTTP)
                ));

        DOCKER_CONTAINER.start();
        MailPitApi.resetSmtpErrors(baseUri());
        MailPitUtil.loadClientTrustStore();
    }

    public String baseUri() {
        return "http://" + DOCKER_CONTAINER.getHost() + ":" + DOCKER_CONTAINER.getMappedPort(DOCKER_CONTAINER_PORT_HTTP);
    }

    public void stop() {
        DOCKER_CONTAINER.stop();
    }

    private static String credentials(String key, String value) {
        return key + ":" + value;
    }

    private static String binding(Integer key, Integer value) {
        return key + ":" + value;
    }
}