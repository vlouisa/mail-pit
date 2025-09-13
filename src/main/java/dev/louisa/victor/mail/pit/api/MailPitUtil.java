package dev.louisa.victor.mail.pit.api;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

@Slf4j
public class MailPitUtil {
    private static final String TRUST_STORE_PATH = "src/test/resources/mail-pit/certs/cacerts.jks";
    private static final String TRUST_STORE_PASSWORD = "changeit";
    private static final String TRUST_STORE_TYPE = "JKS";
    
    /*
     * In general, a TrustStore is used to verify the certificate presented by an SMTP server when connecting to it.
     * In 'production' we don't need to load/set the truststore because the certificates of the SMTP server 
     * are already trusted by the JVM. Google certificates are a good example of already trusted certificates.
     * 
     * Locally (in our integration tests) however, we need to load/set the truststore for the calling client, because the 
     * certificates of the (MailPit) SMTP server are self-signed and therefor not trusted by the JVM.
     */
    public static void loadClientTrustStore() {
        final KeyStore keyStore = fetchKeyStore();
        final TrustManagerFactory trustManagerFactory = initializeTrustStore(keyStore);
        initializeSslContext(trustManagerFactory);
    }

    private static KeyStore fetchKeyStore() {
        try (FileInputStream fis = new FileInputStream(TRUST_STORE_PATH)) {
            final KeyStore keyStore = KeyStore.getInstance(TRUST_STORE_TYPE);
            keyStore.load(fis, TRUST_STORE_PASSWORD.toCharArray());
            return keyStore;
        } catch (Exception e) {
            log.error("Error fetching and loading key store: {}", e.getMessage());
            throw new RuntimeException("Error fetching and loading key store", e);
        }
    }

    private static @NotNull TrustManagerFactory initializeTrustStore(KeyStore keyStore) {
        try {
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            return tmf;
        } catch (Exception e) {
            log.error("Error initializing TrustManagerFactory: {}", e.getMessage());
            throw new RuntimeException("Error initializing TrustManagerFactory", e);
        }
    }

    private static void initializeSslContext(TrustManagerFactory tmf) {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            SSLContext.setDefault(sslContext);
        } catch (Exception e) {
            log.error("Error initializing SSLContext: {}", e.getMessage());
            throw new RuntimeException("Error initializing SSLContext", e);
        }
    }
}
