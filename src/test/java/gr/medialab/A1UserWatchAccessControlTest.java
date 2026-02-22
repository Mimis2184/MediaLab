package gr.medialab;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class A1UserWatchAccessControlTest {

    @Test
    void userCanWatchOnlyAccessibleDocuments() {
        MediaLabManager manager = new MediaLabManager();
        UserAccount admin = manager.getUserByUsername("medialab");

        manager.addDocumentCategory(admin, "News");
        manager.addDocumentCategory(admin, "Sports");

        manager.addUser(admin, "U1", "User", "u1", "p1", UserRole.USER, Set.of("News"));
        UserAccount u1 = manager.getUserByUsername("u1");

        Document newsDoc = manager.createDocument("N1", "Author1", "News", "Text");
        Document sportsDoc = manager.createDocument("S1", "Author1", "Sports", "Text");

        assertDoesNotThrow(() -> manager.watchDocument(u1, newsDoc));
        assertThrows(SecurityException.class, () -> manager.watchDocument(u1, sportsDoc));

        assertTrue(manager.getWatchedDocumentIdsReadOnly(u1).contains(newsDoc.getId()));
        assertFalse(manager.getWatchedDocumentIdsReadOnly(u1).contains(sportsDoc.getId()));
    }
}
