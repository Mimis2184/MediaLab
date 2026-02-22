package gr.medialab;

import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class A1AdminAddUserTest {

    @Test
    void adminCanAddUser_withRoleCredentialsAndAtLeastOneAllowedCategory() {
        MediaLabManager manager = new MediaLabManager();
        UserAccount admin = manager.getUserByUsername("medialab");

        // Create the category first, so it can be assigned to the new user
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
        assertEquals("Maria", maria.getFirstName());
        assertEquals("Papadopoulou", maria.getLastName());
        assertEquals(UserRole.USER, maria.getRole());
        assertTrue(maria.getAllowedCategoriesReadOnly().contains("News"));
    }
}
