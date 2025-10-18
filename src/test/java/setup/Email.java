package setup;

import lombok.Builder;

@Builder
public record Email(
        EmailAddress from,
        EmailAddress to,
        String subject,
        String body) {}