package dev.louisa.victor.mail.pit.asserter;

import dev.louisa.victor.mail.pit.model.MailPitAddress;
import dev.louisa.victor.mail.pit.model.MailPitMessage;
import org.assertj.core.api.AbstractAssert;


public class MailPitMessageAssert extends AbstractAssert<MailPitMessageAssert, MailPitMessage> {

    protected MailPitMessageAssert(MailPitMessage actual) {
        super(actual, MailPitMessageAssert.class);
    }

    public static MailPitMessageAssert assertMailPitMessage(MailPitMessage actual) {
        return new MailPitMessageAssert(actual);
    }

    public MailPitMessageAssert hasBodySnippet(String text) {
        isNotNull();

        if (!actual.snippet().equals(text)) {
            failWithMessage(
                    "Expected 'message body snippet' to be <%s> but was <%s>"
                    , text
                    , actual.snippet()
            );
        }
        return this;
    }

    public MailPitMessageAssert bodySnippetContains(String text) {
        isNotNull();

        if (!actual.snippet().contains(text)) {
            failWithMessage(
                    "Expected 'message body snippet' to contain <%s> but it didn't. Actual body: \n--- BEGIN --- \n<%s>\n--- END ---"
                    , text
                    , actual.snippet()
            );
        }
        return this;
    }

    public MailPitMessageAssert hasSubject(String subject) {
        isNotNull();

        if (!actual.subject().contains(subject)) {
            failWithMessage(
                    "Expected 'subject' to be <%s> but was <%s>"
                    , subject
                    , actual.subject()
            );
        }
        return this;
    }

    public MailPitMessageAssert hasSender(String sender) {
        isNotNull();

        if (!actual.from().address().equals(sender)) {
            failWithMessage(
                    "Expected 'sender' <%s> does not exist in actual sender list <%s>"
                    , sender
                    , actual.from().address()
            );

        }
        return this;
    }

    public MailPitMessageAssert hasRecipient(String recipient) {
        isNotNull();

        if (actual.to().stream()
                .map(MailPitAddress::address)
                .noneMatch(recipient::equals)) {
            failWithMessage(
                    "Expected 'recipient' <%s> does not exist in actual recipient list <%s>"
                    , recipient
                    , actual.to().stream().map(MailPitAddress::address).toList()
            );

        }
        return this;
    }
}
