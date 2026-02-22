package gr.medialab;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class A1UserSeesOnlyLatestVersionTest {

    @Test
    void userCanViewOnlyLatestVersion() {
        MediaLabManager manager = new MediaLabManager();
        UserAccount admin = manager.getUserByUsername("medialab");

        manager.addDocumentCategory(admin, "News");
        manager.addUser(admin, "U1", "User", "u1", "p1", UserRole.USER, Set.of("News"));
        UserAccount u1 = manager.getUserByUsername("u1");

        Document doc = manager.createDocument("N1", "Author1", "News", "v1");
        manager.editDocumentText(doc, "v2");
        manager.editDocumentText(doc, "v3");

        List<Integer> visibleVersions = manager.getVisibleVersionNumbers(u1, doc);

        assertEquals(1, visibleVersions.size());
        assertEquals(3, visibleVersions.get(0)); // only latest
    }
}
