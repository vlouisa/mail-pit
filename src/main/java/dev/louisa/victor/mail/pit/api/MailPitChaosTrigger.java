package dev.louisa.victor.mail.pit.api;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MailPitChaosTrigger {
    RESET("Reset"),
    AUTHENTICATION("Authentication"),
    RECIPIENT("Recipient"),
    SENDER("Sender");
    
    private final String label;
    
    public String label() {
        return this.label;
    }
}
