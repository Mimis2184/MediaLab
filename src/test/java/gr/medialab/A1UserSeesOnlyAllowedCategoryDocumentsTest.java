package gr.medialab;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class A1UserSeesOnlyAllowedCategoryDocumentsTest {

    @Test
    void userSeesOnlyDocumentsFromAllowedCategories() {
        MediaLabManager manager = new MediaLabManager();
        UserAccount admin = manager.getUserByUsername("medialab");

        manager.addDocumentCategory(admin, "News");
        manager.addDocumentCategory(admin, "Sports");

        manager.addUser(admin,
                "U1",
                "User",
                "u1",
                "p1",
                UserRole.USER,
                Set.of("News")
        );

        UserAccount u1 = manager.getUserByUsername("u1");

        Document d1 = manager.createDocument("N1", "Author1", "News", "Text");
        Document d2 = manager.createDocument("S1", "Author1", "Sports", "Text");

        List<Document> visible = manager.getAccessibleDocumentsForUser(u1);

        assertTrue(visible.stream().anyMatch(d -> d.getId().equals(d1.getId())));
        assertTrue(visible.stream().noneMatch(d -> d.getId().equals(d2.getId())));
    }
}
