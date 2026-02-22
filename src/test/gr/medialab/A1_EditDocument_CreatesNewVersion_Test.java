package gr.medialab;

import gr.medialab.domain.Document;
import gr.medialab.logic.MediaLabManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class A1_EditDocument_CreatesNewVersion_Test {

    @Test
    void editDocument_createsNewVersion_andKeepsOldText() {
        MediaLabManager manager = new MediaLabManager();

        Document doc = manager.createDocument(
                "Title 1",
                "Author 1",
                "News",
                "Old text"
        );

        manager.editDocumentText(doc, "New text");

        assertEquals(2, doc.getLatestVersionNumber());
        assertEquals("New text", doc.getLatestText());
        assertEquals(2, doc.getVersionsCount());
        assertEquals("Old text", doc.getTextOfVersion(1));
        assertEquals("New text", doc.getTextOfVersion(2));
    }
}
