package gr.medialab.ui.viewmodels;

public class DocumentSearchRow {
    private final String title;
    private final String author;
    private final String category;
    private final String createdAt;
    private final int version;

    public DocumentSearchRow(String title, String author, String category, String createdAt, int version) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.createdAt = createdAt;
        this.version = version;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getCreatedAt() { return createdAt; }
    public int getVersion() { return version; }
}