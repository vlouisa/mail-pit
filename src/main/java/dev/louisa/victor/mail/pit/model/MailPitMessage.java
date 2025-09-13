package dev.louisa.victor.mail.pit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record MailPitMessage(
        @JsonProperty(value = "From")
        MailPitAddress from,

        @JsonProperty(value = "To")
        List<MailPitAddress> to,

        @JsonProperty(value = "Subject")
        String subject,

        @JsonProperty(value = "Snippet")
        String snippet
) {}