package dev.louisa.victor.mail.pit.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.louisa.victor.mail.pit.model.MailPitResponse;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.Builder;

import java.util.function.Predicate;

import static dev.louisa.victor.mail.pit.api.MailPitChaosTrigger.*;

public class MailPitApi {
    private static final String MESSAGES_ENDPOINT = "/api/v1/messages";
    private static final String CHAOS_ENDPOINT = "/api/v1/chaos";
    private static final int ENABLE = 100;
    private static final int DISABLE = 0;

    public static MailPitResponse fetchMessages(String baseUri) throws JsonProcessingException {
        final String response = RestAssured
                .given()
                .baseUri(baseUri)
                .when()
                .get(MESSAGES_ENDPOINT)
                .then()
                .extract()
                .response()
                .asString();
        return parse(response);
    }
    
    public static void resetSmtpErrors(String baseUri) {
        final ChaosConfigDto requestBody = ChaosConfigDto.builder()
                .authenticationTrigger(createDisabledTrigger())
                .recipientTrigger(createDisabledTrigger())
                .senderTrigger(createDisabledTrigger())
                .build();

        final Response response = putChaosConfig(baseUri, requestBody);

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to reset SMTP errors: " + response.getStatusLine());
        }
    }


    public static void forceSmtpError(String baseUri, MailPitChaosTrigger trigger) {
        final ChaosConfigDto requestBody = ChaosConfigDto.builder()
                .authenticationTrigger(createTrigger(trigger, isEnabled(AUTHENTICATION)))
                .recipientTrigger(createTrigger(trigger, isEnabled(RECIPIENT)))
                .senderTrigger(createTrigger(trigger, isEnabled(SENDER)))
                .build();

        Response response = putChaosConfig(baseUri, requestBody);

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to force SMTP error: " + response.getStatusLine());
        }
    }

    private static Predicate<MailPitChaosTrigger> isEnabled(MailPitChaosTrigger trigger) {
        return t -> t.label().equals(trigger.label());
    }

    private static ChaosErrorConfigDto createDisabledTrigger() {
        return ChaosErrorConfigDto.builder()
                .errorCode(451)
                .probability(DISABLE)
                .build();
    }

    private static ChaosErrorConfigDto createTrigger(MailPitChaosTrigger trigger, Predicate<MailPitChaosTrigger> enableTrigger) {
        return ChaosErrorConfigDto.builder()
                .errorCode(451)
                .probability(enableTrigger.test(trigger) ? ENABLE : DISABLE)
                .build();
    }


    private static Response putChaosConfig(String baseUri, ChaosConfigDto chaosConfigDto) {
        return RestAssured
                .given()
                .baseUri(baseUri)
                .contentType("application/json")
                .body(chaosConfigDto)
                .when()
                .put(CHAOS_ENDPOINT)
                .then()
                .extract()
                .response();
    }
    
    private static MailPitResponse parse(String response) throws JsonProcessingException {
        try {
            return new ObjectMapper().readValue(response, MailPitResponse.class);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingException("Failed to parse response") {};
        }
    }
    
    @Builder
    private record ChaosConfigDto(
            @JsonProperty("Authentication")
            ChaosErrorConfigDto authenticationTrigger,

            @JsonProperty("Recipient")
            ChaosErrorConfigDto recipientTrigger,

            @JsonProperty("Sender")
            ChaosErrorConfigDto senderTrigger
    ) {}

    @Builder
    private record ChaosErrorConfigDto(
            @JsonProperty("ErrorCode")
            Integer errorCode,
            
            @JsonProperty("Probability")
            Integer probability
    ) {}
}
