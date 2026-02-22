package gr.medialab;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class A1UserGetsNewVersionNotificationOnNextLoginTest {

    @Test
    void userIsNotifiedOnNextLoginWhenWatchedDocumentHasNewVersion() {
        MediaLabManager manager = new MediaLabManager();
        UserAccount admin = manager.getUserByUsername("medialab");

        manager.addDocumentCategory(admin, "News");
        manager.addUser(admin, "U1", "User", "u1", "p1", UserRole.USER, Set.of("News"));
        UserAccount u1 = manager.getUserByUsername("u1");

        Document doc = manager.createDocument("N1", "Author1", "News", "v1");
        manager.watchDocument(u1, doc);

        // first login: no updates
        List<Document> updates1 = manager.loginAndGetWatchedDocumentsWithNewVersion(u1);
        assertTrue(updates1.isEmpty());

        // update document
        manager.editDocumentText(doc, "v2");

        // next login: should notify
        List<Document> updates2 = manager.loginAndGetWatchedDocumentsWithNewVersion(u1);
        assertEquals(1, updates2.size());
        assertEquals(doc.getId(), updates2.get(0).getId());

        // next login again (no new changes): no updates
        List<Document> updates3 = manager.loginAndGetWatchedDocumentsWithNewVersion(u1);
        assertTrue(updates3.isEmpty());
    }
}
