package gr.medialab.persistence;

import gr.medialab.app.AppBootstrap;
import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;

import java.util.Set;

public class Main {
    public static void main(String[] args) {

        AppBootstrap app = new AppBootstrap();
        MediaLabManager manager = app.start();   // LOAD από JSON

        System.out.println("Loaded users: " + manager.getUsersReadOnly().size());
        System.out.println("Loaded categories: " + manager.getDocumentCategoriesReadOnly().size());
        System.out.println("Loaded documents: " + manager.getDocumentsReadOnly().size());

        UserAccount admin = manager.getUserByUsername("medialab");

        // 1) Ensure category News exists
        if (!manager.getDocumentCategoriesReadOnly().contains("News")) {
            manager.addDocumentCategory(admin, "News");
        }

        // 2) Ensure user u1 exists
        boolean u1Exists = manager.getUsersReadOnly().stream()
                .anyMatch(u -> u.getUsername().equals("u1"));
        if (!u1Exists) {
            manager.addUser(admin, "U1", "User", "u1", "p1", UserRole.USER, Set.of("News"));
        }
        UserAccount u1 = manager.getUserByUsername("u1");

        // 3) Ensure document Doc1 exists
        Document doc = manager.getDocumentsReadOnly().stream()
                .filter(d -> d.getTitle().equals("Doc1"))
                .findFirst()
                .orElseGet(() -> manager.createDocument("Doc1", "Alice", "News", "Hello\n\nWorld"));

        // 4) Ensure watchlist contains Doc1
        manager.watchDocument(u1, doc);

        // 5) First login -> store known version
        manager.loginAndGetWatchedDocumentsWithNewVersion(u1);

        // 6) Create new version only if not already version 2+
        if (doc.getLatestVersionNumber() < 2) {
            manager.editDocumentText(doc, "Hello v2");
        }

        // 7) Second login -> detect update and update known version
        manager.loginAndGetWatchedDocumentsWithNewVersion(u1);

        app.stop(); // SAVE στα JSON
        System.out.println("Saved to JSON. Exiting...");
    }
}
