package gr.medialab;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class A1AuthorCannotEditOutsideAllowedCategoriesTest {

    @Test
    void authorCanEditOnlyInAllowedCategories() {
        MediaLabManager manager = new MediaLabManager();
        UserAccount admin = manager.getUserByUsername("medialab");

        manager.addDocumentCategory(admin, "News");
        manager.addDocumentCategory(admin, "Sports");

        manager.addUser(admin, "Anna", "Author", "anna", "p1", UserRole.AUTHOR, Set.of("News"));
        UserAccount author = manager.getUserByUsername("anna");

        Document newsDoc = manager.createDocument("N1", "Someone", "News", "v1");
        Document sportsDoc = manager.createDocument("S1", "Someone", "Sports", "v1");

        assertDoesNotThrow(() -> manager.editDocumentText(author, newsDoc, "v2"));

        assertThrows(SecurityException.class, () -> manager.editDocumentText(author, sportsDoc, "v2"));
    }
}
