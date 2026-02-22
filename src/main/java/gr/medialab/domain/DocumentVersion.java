package gr.medialab.domain;

import java.time.LocalDateTime;

public class DocumentVersion {
    private final int versionNumber;
    private final String text;
    private final LocalDateTime createdAt;

    public DocumentVersion(int versionNumber, String text, LocalDateTime createdAt) {
        this.versionNumber = versionNumber;
        this.text = text;
        this.createdAt = createdAt;
    }

    public int getVersionNumber() { return versionNumber; }
    public String getText() { return text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

