package dev.louisa.victor.mail.pit.asserter;


import dev.louisa.victor.mail.pit.docker.MailPitContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import setup.*;

import static dev.louisa.victor.mail.pit.asserter.MailPitResponseAssert.*;
import static org.assertj.core.api.Assertions.assertThatCode;

@Slf4j
@SpringBootTest(classes = {
        EmailSession.class,
        AsyncEmailService.class,
        TestMailConfig.class,
        MailPitContainer.class})
public class MailPitResponseAssertTest {

    @Autowired
    private MailPitContainer mailPitContainer;
    @Autowired
    private AsyncEmailService asyncEmailService;

    @BeforeEach
    public void setup() {
        mailPitContainer.start();
    }

    @AfterEach
    public void teardown() {
        mailPitContainer.stop();
    }

    @Test
    void shouldAssertResponseOkWhenMailPitResponseAssertionIsCompletelyCorrect() {
        asyncEmailService.sendEmail(
                Email.builder()
                        .from(address("Elaine from Monkey Island", "elaine.marley@monkey-island.test"))
                        .to(address("GuyBrush Threepwood", "guybrush.threepwood@monkey-island.test"))
                        .subject("Welcome to Monkey Island!")
                        .body("Ahoy Matey! Welcome aboard!")
                        .build());
        
        assertThatCode(() ->
                messagesFrom(mailPitContainer.baseUri())
                        .awaitMessages(1)
                        .pollingIntervalInMillis(200)
                        .assertThat()
                        .message(1)
                        .hasSender("elaine.marley@monkey-island.test")
                        .hasRecipient("guybrush.threepwood@monkey-island.test")
                        .hasSubject("Welcome to Monkey Island!")
                        .bodySnippetContains("Welcome aboard!"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionWhenMailPitResponseIsLate() {
        asyncEmailService.sendEmail(
                Email.builder()
                        .from(address("Elaine from Monkey Island", "elaine.marley@monkey-island.test"))
                        .to(address("GuyBrush Threepwood", "guybrush.threepwood@monkey-island.test"))
                        .subject("Welcome to Monkey Island!")
                        .body("Ahoy Matey! Welcome aboard!")
                        .build(),
                2000
        );

        assertThatCode(() ->
                messagesFrom(mailPitContainer.baseUri())
                        .awaitMessages(1)
                        .waitTimeInSeconds(1)
                        .assertThat()
                        .message(1)
                        .hasSender("elaine.marley@monkey-island.test")
                        .hasRecipient("guybrush.threepwood@monkey-island.test")
                        .hasSubject("Welcome to Monkey Island!")
                        .bodySnippetContains("Welcome aboard!"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expected at least <1> messages but they were not received within <1> seconds");
    }

    @Test
    void shouldAssertMultipleResponsesOkWhenMailPitResponseAssertionIsCompletelyCorrect() {
        asyncEmailService.sendEmail(
                Email.builder()
                        .from(address("Elaine from Monkey Island", "elaine.marley@monkey-island.test"))
                        .to(address("GuyBrush Threepwood", "guybrush.threepwood@monkey-island.test"))
                        .subject("Welcome to Monkey Island!")
                        .body("Ahoy Matey! Welcome aboard!")
                        .build());

        asyncEmailService.sendEmail(
                Email.builder()
                        .from(address("Elaine from Monkey Island", "elaine.marley@monkey-island.test"))
                        .to(address("GuyBrush Threepwood", "guybrush.threepwood@monkey-island.test"))
                        .subject("You're doomed!")
                        .body("There's no escape Matey!")
                        .build(),
                2000 // This is to assure that the second email is arriving after the first one
        );

        assertThatCode(() -> {
                    messagesFrom(mailPitContainer.baseUri())
                            .awaitMessages(2)
                            .assertThat()
                            .message(1)
                            .hasSender("elaine.marley@monkey-island.test")
                            .hasRecipient("guybrush.threepwood@monkey-island.test")
                            .hasSubject("Welcome to Monkey Island!")
                            .bodySnippetContains("Welcome aboard!");

                    messagesFrom(mailPitContainer.baseUri())
                            .awaitMessages(0)
                            .assertThat()
                            .message(2)
                            .hasSender("elaine.marley@monkey-island.test")
                            .hasRecipient("guybrush.threepwood@monkey-island.test")
                            .hasSubject("You're doomed!")
                            .bodySnippetContains("There's no escape Matey!");
                }
        ).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "voodoo.lady@monkey-island.test,guybrush.threepwood@monkey-island.test, Welcome to Monkey Island!, Ahoy Matey! Welcome aboard!, Expected 'sender' to be <voodoo.lady@monkey-island.test> but was <elaine.marley@monkey-island.test>",
            "elaine.marley@monkey-island.test, le-chuck@monkey-island.test, Welcome to Monkey Island!, Ahoy Matey! Welcome aboard!, Expected 'recipient' <le-chuck@monkey-island.test> does not exist in actual recipient list <[guybrush.threepwood@monkey-island.test]>",
            "elaine.marley@monkey-island.test, guybrush.threepwood@monkey-island.test, You're doomed!, Ahoy Matey! Welcome aboard!, Expected 'subject' to be <You're doomed!> but was <Welcome to Monkey Island!>",
            "elaine.marley@monkey-island.test, guybrush.threepwood@monkey-island.test, Welcome to Monkey Island!, There's no escape Matey!, Expected 'message body snippet' to contain <There's no escape Matey!> but it didn't. Actual body:",
    })
    void shouldThrowExceptionWhenMailPitResponseAssertionIsNotCorrect(String expectedSender, String expectedRecipient, String expectedSubject, String expectedBody, String assertionMessage) {
        asyncEmailService.sendEmail(
                Email.builder()
                        .from(address("Elaine from Monkey Island", "elaine.marley@monkey-island.test"))
                        .to(address("GuyBrush Threepwood", "guybrush.threepwood@monkey-island.test"))
                        .subject("Welcome to Monkey Island!")
                        .body("Ahoy Matey! Welcome aboard!")
                        .build());

        assertThatCode(() ->
                messagesFrom(mailPitContainer.baseUri())
                        .awaitMessages(1)
                        .assertThat()
                        .message(1)
                        .hasSender(expectedSender)
                        .hasRecipient(expectedRecipient)
                        .hasSubject(expectedSubject)
                        .bodySnippetContains(expectedBody))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(assertionMessage);

    }

    @Test
    void shouldThrowExceptionWhenExpectingMoreMessagesThanActuallySent() {
        asyncEmailService.sendEmail(
                Email.builder()
                        .from(address("Elaine from Monkey Island", "elaine.marley@monkey-island.test"))
                        .to(address("GuyBrush Threepwood", "guybrush.threepwood@monkey-island.test"))
                        .subject("Welcome to Monkey Island!")
                        .body("Ahoy Matey! Welcome aboard!")
                        .build());

        assertThatCode(() ->
                messagesFrom(mailPitContainer.baseUri())
                        .awaitMessages(2)
                        .waitTimeInSeconds(2)
                        .assertThat()
                        .message(1)
                        .hasSender("elaine.marley@monkey-island.test")
                        .hasRecipient("guybrush.threepwood@monkey-island.test")
                        .hasSubject("Welcome to Monkey Island!")
                        .bodySnippetContains("Welcome aboard!"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expected at least <2> messages but they were not received within <2> seconds");
    }

    @ParameterizedTest
    @CsvSource({
            "0, Message number should be greater than 0, but was <0>",
            "4, Total number of messages in response is <1>, requested message <4> doesn't exist",
    })
    void shouldThrowExceptionWhenAssertingMessageOutOfBounds(int messageNumber, String assertionMessage) {
        asyncEmailService.sendEmail(
                Email.builder()
                        .from(address("Elaine from Monkey Island", "elaine.marley@monkey-island.test"))
                        .to(address("GuyBrush Threepwood", "guybrush.threepwood@monkey-island.test"))
                        .subject("Welcome to Monkey Island!")
                        .body("Ahoy Matey! Welcome aboard!")
                        .build());

        assertThatCode(() ->
                messagesFrom(mailPitContainer.baseUri())
                        .awaitMessages(1)
                        .waitTimeInSeconds(2)
                        .assertThat()
                        .message(messageNumber)
                        .hasSender("elaine.marley@monkey-island.test")
                        .hasRecipient("guybrush.threepwood@monkey-island.test")
                        .hasSubject("Welcome to Monkey Island!")
                        .bodySnippetContains("Welcome aboard!"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(assertionMessage);
    }

    private EmailAddress address(String personal, String email) {
        return EmailAddress.builder()
                .address(email)
                .personal(personal)
                .build();
    }
}
