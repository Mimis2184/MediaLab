package gr.medialab;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class A1AdminDeleteUserTest {

    @Test
    void adminCanDeleteUser_andUserWatchlistIsRemoved() {
        MediaLabManager manager = new MediaLabManager();
        UserAccount admin = manager.getUserByUsername("medialab");

        manager.addDocumentCategory(admin, "News");

        manager.addUser(admin,
                "Maria",
                "Papadopoulou",
                "maria",
                "pass123",
                UserRole.USER,
                Set.of("News")
        );

        UserAccount maria = manager.getUserByUsername("maria");

        Document doc = manager.createDocument("N1", "Author1", "News", "Text");
        manager.watchDocument(maria, doc);

        assertTrue(manager.getWatchedDocumentIdsReadOnly(maria).contains(doc.getId()));

        manager.deleteUser(admin, "maria");

        assertThrows(NoSuchElementException.class, () -> manager.getUserByUsername("maria"));
        assertTrue(manager.getWatchedDocumentIdsByUsernameReadOnly("maria").isEmpty());
    }
}
