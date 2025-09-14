package dev.louisa.victor.mail.pit.asserter;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.louisa.victor.mail.pit.api.MailPitApi;
import dev.louisa.victor.mail.pit.model.MailPitResponse;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ObjectAssert;

import java.time.Duration;

import static org.awaitility.Awaitility.await;

public class MailPitResponseAssert extends AbstractAssert<MailPitResponseAssert, MailPitResponse> {
    protected MailPitResponseAssert(MailPitResponse actual) {
        super(actual, MailPitResponseAssert.class);
    }

    public static MailPitResponseInput mailPitMessages() {
        return new MailPitResponseInput();
    }
    
    public static class MailPitResponseInput {
        private String baseUri;
        private int expectedCount = 0;

        public MailPitResponseInput fromBaseUri(String baseUri) {
            this.baseUri = baseUri;
            return this;
        }
        
        public MailPitResponseInput awaitMessages(int expectedCount) {
            this.expectedCount = expectedCount;
            return this;
        }

        public MailPitResponseAssert assertThat() throws JsonProcessingException {
            if(expectedCount <= 0) {
                return new MailPitResponseAssert(MailPitApi.fetchMessages(baseUri));
            }
            
            await().atMost(Duration.ofSeconds(10))
                    .pollInterval(Duration.ofMillis(500))
                    .until(() -> {
                        MailPitResponse response = MailPitApi.fetchMessages(baseUri);
                        return response.messages().size() >= expectedCount;
                    });

            return new MailPitResponseAssert(MailPitApi.fetchMessages(baseUri));
        }
    }
    
    // Existing fluent methods
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

        return MailPitMessageAssert.assertMailPitMessage(
                actual.messages().get(actual.messages().size() - messageNumber)
        );
    }

    public ObjectAssert<Integer> numberOfMessages() {
        return new ObjectAssert<>(actual.messages().size());
    }
}