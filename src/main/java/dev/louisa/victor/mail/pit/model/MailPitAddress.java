package dev.louisa.victor.mail.pit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record MailPitAddress(
        @JsonProperty(value = "Address")
        String address,
        @JsonProperty(value = "Name")
        String name
) {}
