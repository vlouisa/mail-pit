package dev.louisa.victor.mail.pit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record MailPitResponse(
        List<MailPitMessage> messages
) {}

