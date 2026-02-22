package gr.medialab;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import gr.medialab.persistence.AppState;
import gr.medialab.persistence.JsonStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class A2PersistenceRoundTripTest {

    @TempDir
    Path tempDir;

    @Test
    void saveThenLoad_restoresApplicationState() throws Exception {
        Path folder = tempDir.resolve("medialab");
        JsonStorage storage = new JsonStorage(folder);

        // ---- 1) Create state in memory ----
        MediaLabManager m1 = new MediaLabManager();
        UserAccount admin = m1.getUserByUsername("medialab");

        m1.addDocumentCategory(admin, "News");
        m1.addUser(admin, "U1", "User", "u1", "p1", UserRole.USER, Set.of("News"));
        UserAccount u1 = m1.getUserByUsername("u1");

        Document doc = m1.createDocument("Title1", "Alice", "News", "v1");
        m1.watchDocument(u1, doc);

        // user logs in once -> learns v1
        assertTrue(m1.loginAndGetWatchedDocumentsWithNewVersion(u1).isEmpty());

        // create new version
        m1.editDocumentText(doc, "v2");

        // ---- 2) Save ----
        AppState state1 = m1.exportState();
        storage.saveAll(state1);

        // ---- 3) Load into new manager ----
        AppState loaded = storage.loadAll();
        MediaLabManager m2 = new MediaLabManager();
        m2.loadFrom(loaded);

        // ---- 4) Assert state restored ----
        UserAccount u1b = m2.getUserByUsername("u1");
        assertTrue(m2.getDocumentCategoriesReadOnly().contains("News"));

        List<Document> visible = m2.searchDocuments(u1b, "News", null, null);
        assertEquals(1, visible.size());

        Document loadedDoc = visible.get(0);
        assertEquals(doc.getId(), loadedDoc.getId());
        assertEquals(2, loadedDoc.getLatestVersionNumber());

        assertTrue(m2.getWatchedDocumentIdsReadOnly(u1b).contains(doc.getId()));

        // first login on m2 should notify because known=1, current=2
        List<Document> updates = m2.loginAndGetWatchedDocumentsWithNewVersion(u1b);
        assertEquals(1, updates.size());
        assertEquals(doc.getId(), updates.get(0).getId());
    }
}
