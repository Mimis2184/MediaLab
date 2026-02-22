package gr.medialab;

import gr.medialab.domain.Document;
import gr.medialab.logic.MediaLabManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class A1CreateDocumentStartsWithVersion1Test {

    @Test
    void createDocument_setsVersionTo1_andStoresRequiredFields() {
        MediaLabManager manager = new MediaLabManager();

        Document doc = manager.createDocument(
                "Title 1",
                "Author 1",
                "News",
                "Paragraph1\n\nParagraph2"
        );

        assertNotNull(doc);
        assertEquals(1, doc.getLatestVersionNumber());
        assertEquals("Title 1", doc.getTitle());
        assertEquals("Author 1", doc.getAuthorName());
        assertEquals("News", doc.getCategory());
        assertEquals("Paragraph1\n\nParagraph2", doc.getLatestText());
        assertNotNull(doc.getCreatedAt());
    }
}
