package gr.medialab;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class A1DeleteDocumentCleansWatchlistsTest {

    @Test
    void deleteDocument_removesDocumentAndCleansWatchlists() {
        MediaLabManager manager = new MediaLabManager();
        UserAccount admin = manager.getUserByUsername("medialab");

        manager.addDocumentCategory(admin, "News");
        manager.addUser(admin, "U1", "User", "u1", "p1", UserRole.USER, Set.of("News"));
        UserAccount u1 = manager.getUserByUsername("u1");

        manager.addUser(admin, "A1", "Author", "a1", "p2", UserRole.AUTHOR, Set.of("News"));
        UserAccount author = manager.getUserByUsername("a1");

        Document doc = manager.createDocument(author, "N1", "A1 Author", "News", "v1");

        manager.watchDocument(u1, doc);
        assertTrue(manager.getWatchedDocumentIdsReadOnly(u1).contains(doc.getId()));

        manager.deleteDocument(author, doc);

        assertTrue(manager.getDocumentsReadOnly().stream().noneMatch(d -> d.getId().equals(doc.getId())));
        assertFalse(manager.getWatchedDocumentIdsReadOnly(u1).contains(doc.getId()));
    }
}
