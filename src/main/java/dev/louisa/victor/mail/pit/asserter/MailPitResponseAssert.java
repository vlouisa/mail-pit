package dev.louisa.victor.mail.pit.asserter;

import dev.louisa.victor.mail.pit.model.MailPitResponse;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ObjectAssert;

public class MailPitResponseAssert extends AbstractAssert<MailPitResponseAssert, MailPitResponse> {

    protected MailPitResponseAssert(MailPitResponse actual) {
        super(actual, MailPitResponseAssert.class);
    }

    public static MailPitResponseAssert assertThatMailPitResponse(MailPitResponse actual) {
        return new MailPitResponseAssert(actual);
    }

    public MailPitMessageAssert message(int messageNumber) {
        isNotNull();

        if (messageNumber < 1) {
            failWithMessage("Message number should be greater than 0, but was <%s>", messageNumber);
        }

        if (actual.messages().size() < messageNumber) {
            failWithMessage(
                    "Total number of message in response is <%s>, requested message <%s> doesn't exist"
                    , actual.messages().size()
                    , messageNumber);
        }
        return MailPitMessageAssert.assertMailPitMessage(actual.messages().get(actual.messages().size() - messageNumber));
    }

    public ObjectAssert<Integer> numberOfMessages() {
        return new ObjectAssert<>(actual.messages().size());
    }
}
