package dev.louisa.victor.mail.pit.asserter;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.louisa.victor.mail.pit.api.MailPitApi;
import dev.louisa.victor.mail.pit.model.MailPitResponse;
import org.assertj.core.api.AbstractAssert;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;

import java.time.Duration;

public class MailPitResponseAssert extends AbstractAssert<MailPitResponseAssert, MailPitResponse> {
    protected MailPitResponseAssert(MailPitResponse actual) {
        super(actual, MailPitResponseAssert.class);
    }

    public static MailPitResponseAwaitStage messagesFrom(String baseUri) {
        return new MailPitResponseAssertBuilder(baseUri);
    }

    // --- Stage interfaces ---
    public interface MailPitResponseAwaitStage {
        MailPitResponseConfigStage awaitMessages(int expectedCount);
    }

    public interface MailPitResponseConfigStage {
        MailPitResponseConfigStage waitTimeInSeconds(int waitTimeInSeconds);
        MailPitResponseConfigStage pollingIntervalInMillis(int pollingIntervalInMillis);
        MailPitResponseAssert assertThat() throws JsonProcessingException;
    }

    // --- Implementation of staged builder ---
    private static class MailPitResponseAssertBuilder implements MailPitResponseAwaitStage, MailPitResponseConfigStage {
        private final String baseUri;
        private int expectedCount;
        private int waitTimeInSeconds = 10;
        private int pollingIntervalInMillis = 500;

        private MailPitResponseAssertBuilder(String baseUri) {
            this.baseUri = baseUri;
        }

        @Override
        public MailPitResponseConfigStage awaitMessages(int expectedCount) {
            this.expectedCount = expectedCount;
            return this;
        }

        @Override
        public MailPitResponseConfigStage waitTimeInSeconds(int waitTimeInSeconds) {
            this.waitTimeInSeconds = waitTimeInSeconds;
            return this;
        }

        @Override
        public MailPitResponseConfigStage pollingIntervalInMillis(int pollingIntervalInMillis) {
            this.pollingIntervalInMillis = pollingIntervalInMillis;
            return this;
        }

        @Override
        public MailPitResponseAssert assertThat() throws JsonProcessingException {
            awaitMessages();
            return new MailPitResponseAssert(MailPitApi.fetchMessages(baseUri));
        }

        private void awaitMessages() {
            try {
                Awaitility.await().atMost(Duration.ofSeconds(waitTimeInSeconds))
                        .pollInterval(Duration.ofMillis(pollingIntervalInMillis))
                        .until(() -> {
                            MailPitResponse response = MailPitApi.fetchMessages(baseUri);
                            return response.messages().size() >= expectedCount;
                        });
            } catch (ConditionTimeoutException e) {
                throw new AssertionError(
                        String.format("Expected at least <%s> messages but they were not received within <%s> seconds",
                                expectedCount, waitTimeInSeconds), e);
            }
        }
    }

    // --- Existing fluent methods ---
    public MailPitMessageAssert message(int messageNumber) {
        isNotNull();

        if (messageNumber < 1) {
            failWithMessage("Message number should be greater than 0, but was <%s>", messageNumber);
        }

        if (actual.messages().size() < messageNumber) {
            failWithMessage(
                    "Total number of messages in response is <%s>, requested message <%s> doesn't exist",
                    actual.messages().size(),
                    messageNumber);
        }

        return new MailPitMessageAssert(
                actual.messages().get(actual.messages().size() - messageNumber)
        );
    }
}