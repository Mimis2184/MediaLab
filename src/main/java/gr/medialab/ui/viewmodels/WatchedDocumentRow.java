package gr.medialab.ui.viewmodels;

public class WatchedDocumentRow {
    private final String documentId;
    private final String title;
    private final String author;
    private final String category;
    private final int latestVersion;

    public WatchedDocumentRow(String documentId, String title, String author, String category, int latestVersion) {
        this.documentId = documentId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.latestVersion = latestVersion;
    }

    public String getDocumentId() { return documentId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public int getLatestVersion() { return latestVersion; }
}