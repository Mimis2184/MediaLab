package gr.medialab;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class A1AuthorSeesLatestThreeVersionsTest {

    @Test
    void authorSeesLatestAndUpToTwoPreviousVersions() {
        MediaLabManager manager = new MediaLabManager();
        UserAccount admin = manager.getUserByUsername("medialab");

        manager.addDocumentCategory(admin, "News");

        manager.addUser(admin,
                "A1",
                "Author",
                "a1",
                "p1",
                UserRole.AUTHOR,
                Set.of("News")
        );

        UserAccount author = manager.getUserByUsername("a1");

        Document doc = manager.createDocument("N1", "A1 Author", "News", "v1");
        manager.editDocumentText(doc, "v2");
        manager.editDocumentText(doc, "v3");
        manager.editDocumentText(doc, "v4");
        manager.editDocumentText(doc, "v5");

        List<Integer> visible = manager.getVisibleVersionNumbers(author, doc);

        assertEquals(3, visible.size());
        assertEquals(List.of(5, 4, 3), visible); // latest first, max 3
    }
}
