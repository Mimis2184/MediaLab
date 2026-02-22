package gr.medialab;

import gr.medialab.domain.UserAccount;
import gr.medialab.logic.MediaLabManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class A1AdminDocumentCategoryAddRenameTest {

    @Test
    void adminCanAddAndRenameDocumentCategory() {
        MediaLabManager manager = new MediaLabManager();
        UserAccount admin = manager.getUserByUsername("medialab");

        manager.addDocumentCategory(admin, "News");
        assertTrue(manager.getDocumentCategoriesReadOnly().contains("News"));

        manager.renameDocumentCategory(admin, "News", "Announcements");
        assertFalse(manager.getDocumentCategoriesReadOnly().contains("News"));
        assertTrue(manager.getDocumentCategoriesReadOnly().contains("Announcements"));
    }
}
