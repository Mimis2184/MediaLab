package gr.medialab.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Document {
    private final String id;
    private final String title;
    private final String authorName;
    private String category; // changes only when admin renames a category
    private final LocalDateTime createdAt;

    private final List<DocumentVersion> versions = new ArrayList<>();

    public Document(String title, String authorName, String category, LocalDateTime createdAt, String initialText) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.authorName = authorName;
        this.category = category;
        this.createdAt = createdAt;

        versions.add(new DocumentVersion(1, initialText, createdAt));
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthorName() { return authorName; }
    public String getCategory() { return category; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public int getLatestVersionNumber() {
        return versions.get(versions.size() - 1).getVersionNumber();
    }

    public String getLatestText() {
        return versions.get(versions.size() - 1).getText();
    }

    public int getVersionsCount() {
        return versions.size();
    }

    public String getTextOfVersion(int versionNumber) {
        for (DocumentVersion v : versions) {
            if (v.getVersionNumber() == versionNumber) {
                return v.getText();
            }
        }
        throw new IllegalArgumentException("Version not found: " + versionNumber);
    }

    public void addNewVersion(String newText, LocalDateTime time) {
        int newVersionNumber = getLatestVersionNumber() + 1;
        versions.add(new DocumentVersion(newVersionNumber, newText, time));
    }

    // Used only by admin when renaming a category
    public void applyCategoryRename(String newCategoryName) {
        this.category = newCategoryName;
    }

    // Returns latest version numbers (latest first), up to maxCount
    public List<Integer> getLatestVersionNumbers(int maxCount) {
        if (maxCount <= 0) {
            throw new IllegalArgumentException("maxCount must be positive");
        }

        int total = versions.size();
        int start = Math.max(0, total - maxCount);

        List<Integer> result = new ArrayList<>();
        for (int i = total - 1; i >= start; i--) { // latest -> older
            result.add(versions.get(i).getVersionNumber());
        }
        return result;
    }
}
