package gr.medialab;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.logic.MediaLabManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class A1AdminDeleteCategoryDeletesDocumentsAndCleansWatchesTest {

    @Test
    void deleteCategory_deletesDocumentsInCategory_andRemovesFromWatchlists() {
        MediaLabManager manager = new MediaLabManager();
        UserAccount admin = manager.getUserByUsername("medialab");

        manager.addDocumentCategory(admin, "News");
        manager.addDocumentCategory(admin, "Sports");

        Document newsDoc = manager.createDocument("N1", "Author1", "News", "Text");
        Document sportsDoc = manager.createDocument("S1", "Author1", "Sports", "Text");

        manager.watchDocument(admin, newsDoc);
        manager.watchDocument(admin, sportsDoc);

        manager.deleteDocumentCategory(admin, "News");

        // Category removed
        assertFalse(manager.getDocumentCategoriesReadOnly().contains("News"));
        assertTrue(manager.getDocumentCategoriesReadOnly().contains("Sports"));

        // Documents of deleted category removed
        assertTrue(manager.getDocumentsReadOnly().stream().noneMatch(d -> d.getId().equals(newsDoc.getId())));
        assertTrue(manager.getDocumentsReadOnly().stream().anyMatch(d -> d.getId().equals(sportsDoc.getId())));

        // Watchlist cleaned
        assertFalse(manager.getWatchedDocumentIdsReadOnly(admin).contains(newsDoc.getId()));
        assertTrue(manager.getWatchedDocumentIdsReadOnly(admin).contains(sportsDoc.getId()));
    }
}
