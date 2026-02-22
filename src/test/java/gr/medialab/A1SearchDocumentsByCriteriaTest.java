package gr.medialab;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class A1SearchDocumentsByCriteriaTest {

    @Test
    void searchFiltersByAnyCombinationOfCategoryTitleAuthor_andRespectsAccess() {
        MediaLabManager manager = new MediaLabManager();
        UserAccount admin = manager.getUserByUsername("medialab");

        manager.addDocumentCategory(admin, "News");
        manager.addDocumentCategory(admin, "Sports");

        manager.addUser(admin, "U1", "User", "u1", "p1", UserRole.USER, Set.of("News"));
        UserAccount u1 = manager.getUserByUsername("u1");

        Document n1 = manager.createDocument("Breaking News", "Alice", "News", "t1");
        Document n2 = manager.createDocument("Daily Update", "Bob", "News", "t2");
        Document s1 = manager.createDocument("Match Report", "Alice", "Sports", "t3"); // not accessible to u1

        // 1) category only
        List<Document> r1 = manager.searchDocuments(u1, "News", null, null);
        assertTrue(r1.stream().anyMatch(d -> d.getId().equals(n1.getId())));
        assertTrue(r1.stream().anyMatch(d -> d.getId().equals(n2.getId())));
        assertTrue(r1.stream().noneMatch(d -> d.getId().equals(s1.getId())));

        // 2) author only
        List<Document> r2 = manager.searchDocuments(u1, null, null, "Alice");
        assertTrue(r2.stream().anyMatch(d -> d.getId().equals(n1.getId())));
        assertTrue(r2.stream().noneMatch(d -> d.getId().equals(s1.getId())));

        // 3) title contains
        List<Document> r3 = manager.searchDocuments(u1, null, "daily", null);
        assertEquals(1, r3.size());
        assertEquals(n2.getId(), r3.get(0).getId());

        // 4) combined: category + author
        List<Document> r4 = manager.searchDocuments(u1, "News", null, "Bob");
        assertEquals(1, r4.size());
        assertEquals(n2.getId(), r4.get(0).getId());
    }
}
