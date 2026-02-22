package gr.medialab;

import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class A1DefaultAdminExistsTest {

    @Test
    void defaultAdminUser_existsWithCorrectCredentials() {
        MediaLabManager manager = new MediaLabManager();

        UserAccount admin = manager.getUsersReadOnly().stream()
                .filter(u -> u.getUsername().equals("medialab"))
                .findFirst()
                .orElseThrow();

        assertEquals(UserRole.ADMIN, admin.getRole());
        assertEquals("medialab_2025", admin.getPassword());
    }
}
