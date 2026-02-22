package gr.medialab;

import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class A1AuthorCannotCreateOutsideAllowedCategoriesTest {

    @Test
    void authorCanCreateOnlyInAllowedCategories() {
        MediaLabManager manager = new MediaLabManager();
        UserAccount admin = manager.getUserByUsername("medialab");

        manager.addDocumentCategory(admin, "News");
        manager.addDocumentCategory(admin, "Sports");

        manager.addUser(admin,
                "Anna",
                "Author",
                "anna",
                "p1",
                UserRole.AUTHOR,
                Set.of("News")
        );

        UserAccount author = manager.getUserByUsername("anna");

        // allowed
        assertDoesNotThrow(() ->
                manager.createDocument(author, "Doc1", "Anna Author", "News", "text")
        );

        // not allowed
        assertThrows(SecurityException.class, () ->
                manager.createDocument(author, "Doc2", "Anna Author", "Sports", "text")
        );
    }
}
