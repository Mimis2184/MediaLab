package gr.medialab.logic;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.persistence.AppState;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Central application service that holds the in-memory state of the system and
 * provides all operations required by the specification (users, categories, documents,
 * versioning, watchlists, and persistence bridge).
 *
 * <p>Runtime rule: all operations modify only the in-memory state. Persistence is performed
 * only at application shutdown through {@link #exportState()}.</p>
 */
public class MediaLabManager {

    /** Registered users (including the default admin). */
    private final List<UserAccount> users = new ArrayList<>();

    /** All documents stored in the system. */
    private final List<Document> documents = new ArrayList<>();

    /** All document categories defined in the system. */
    private final Set<String> documentCategories = new HashSet<>();

    /** Mapping: username -> watched documentIds. */
    private final Map<String, Set<String>> userWatchlist = new HashMap<>();

    /** Mapping: username -> (documentId -> last known version at login). */
    private final Map<String, Map<String, Integer>> watchKnownVersion = new HashMap<>();

    /**
     * Initializes manager and creates the default administrator account.
     * Default credentials: username {@code medialab}, password {@code medialab_2025}.
     */
    public MediaLabManager() {
        users.add(new UserAccount(
                "Default",
                "Admin",
                "medialab",
                "medialab_2025",
                UserRole.ADMIN,
                Set.of()
        ));
    }

    // -------- Users ----------

    /**
     * Returns a read-only snapshot of all users.
     *
     * @return immutable list of users
     */
    public List<UserAccount> getUsersReadOnly() {
        return List.copyOf(users);
    }

    /**
     * Finds a user by username.
     *
     * @param username target username
     * @return the matching {@link UserAccount}
     * @throws NoSuchElementException if user does not exist
     */
    public UserAccount getUserByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Authenticates a user by username and password.
     *
     * @param username account username
     * @param password account password
     * @return the authenticated user, or {@code null} if credentials are invalid
     * @throws IllegalArgumentException if any argument is blank
     */
    public UserAccount authenticate(String username, String password) {
        requireNonBlank(username);
        requireNonBlank(password);

        return users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    /**
     * Adds a new user to the system (admin-only operation).
     *
     * <p>Rules:</p>
     * <ul>
     *   <li>Username must be unique</li>
     *   <li>Role is required</li>
     *   <li>At least one allowed category is required</li>
     *   <li>All allowed categories must exist in the system</li>
     * </ul>
     *
     * @param admin admin user performing the operation
     * @param firstName first name
     * @param lastName last name
     * @param username new username
     * @param password new password
     * @param role user role
     * @param allowedCategories categories the user can access
     * @throws SecurityException if caller is not admin
     * @throws IllegalArgumentException for invalid input or unknown categories or duplicate username
     */
    public void addUser(UserAccount admin,
                        String firstName,
                        String lastName,
                        String username,
                        String password,
                        UserRole role,
                        Set<String> allowedCategories) {
        requireAdmin(admin);
        requireNonBlank(firstName);
        requireNonBlank(lastName);
        requireNonBlank(username);
        requireNonBlank(password);

        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }

        // At least one allowed category is required when creating a user.
        if (allowedCategories == null || allowedCategories.isEmpty()) {
            throw new IllegalArgumentException("At least one allowed category is required");
        }

        // Validate categories exist.
        for (String c : allowedCategories) {
            requireNonBlank(c);
            if (!documentCategories.contains(c)) {
                throw new IllegalArgumentException("Unknown category: " + c);
            }
        }

        if (users.stream().anyMatch(u -> u.getUsername().equals(username))) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        users.add(new UserAccount(firstName, lastName, username, password, role, allowedCategories));
    }

    /**
     * Deletes a user from the system (admin-only operation).
     *
     * <p>Rules:</p>
     * <ul>
     *   <li>The default admin {@code medialab} cannot be deleted</li>
     *   <li>Watchlist/known-version state is removed for the deleted user</li>
     * </ul>
     *
     * @param admin admin user performing the operation
     * @param username username to delete
     * @throws SecurityException if caller is not admin
     * @throws IllegalArgumentException if username is blank, user not found, or default admin deletion attempted
     */
    public void deleteUser(UserAccount admin, String username) {
        requireAdmin(admin);
        requireNonBlank(username);

        if ("medialab".equals(username)) {
            throw new IllegalArgumentException("Default admin cannot be deleted");
        }

        boolean removed = users.removeIf(u -> u.getUsername().equals(username));
        if (!removed) {
            throw new IllegalArgumentException("User not found: " + username);
        }

        // Remove watch state of deleted user.
        userWatchlist.remove(username);
        watchKnownVersion.remove(username);
    }

    /**
     * Updates an existing user (admin-only operation) without delete+add.
     *
     * <p>Rules:</p>
     * <ul>
     *   <li>The default admin {@code medialab} cannot be modified here</li>
     *   <li>Username may change, but must remain unique</li>
     *   <li>At least one allowed category is required</li>
     *   <li>All allowed categories must exist</li>
     *   <li>If username changes, watchlist and known-version state is migrated</li>
     * </ul>
     *
     * @param admin admin user performing the operation
     * @param targetUsername current username of the user to update
     * @param newFirstName new first name
     * @param newLastName new last name
     * @param newUsername new username (may be same as targetUsername)
     * @param newPassword new password
     * @param newRole new role
     * @param newAllowedCategories new allowed categories
     * @throws SecurityException if caller is not admin
     * @throws IllegalArgumentException for invalid input, unknown categories, user not found, or duplicate username
     */
    public void updateUser(UserAccount admin,
                           String targetUsername,
                           String newFirstName,
                           String newLastName,
                           String newUsername,
                           String newPassword,
                           UserRole newRole,
                           Set<String> newAllowedCategories) {
        requireAdmin(admin);

        requireNonBlank(targetUsername);
        requireNonBlank(newFirstName);
        requireNonBlank(newLastName);
        requireNonBlank(newUsername);
        requireNonBlank(newPassword);

        if (newRole == null) {
            throw new IllegalArgumentException("Role is required");
        }

        if ("medialab".equals(targetUsername)) {
            throw new IllegalArgumentException("Default admin cannot be modified");
        }

        UserAccount existing = users.stream()
                .filter(u -> u.getUsername().equals(targetUsername))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + targetUsername));

        if (newAllowedCategories == null || newAllowedCategories.isEmpty()) {
            throw new IllegalArgumentException("At least one allowed category is required");
        }

        Set<String> allowed = new HashSet<>(newAllowedCategories);
        for (String c : allowed) {
            requireNonBlank(c);
            if (!documentCategories.contains(c)) {
                throw new IllegalArgumentException("Unknown category: " + c);
            }
        }

        if (!targetUsername.equals(newUsername)) {
            boolean usernameTaken = users.stream().anyMatch(u -> u.getUsername().equals(newUsername));
            if (usernameTaken) {
                throw new IllegalArgumentException("Username already exists: " + newUsername);
            }
        }

        // Replace user object (safe even if fields are final).
        UserAccount updated = new UserAccount(
                newFirstName,
                newLastName,
                newUsername,
                newPassword,
                newRole,
                allowed
        );

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(existing.getUsername())) {
                users.set(i, updated);
                break;
            }
        }

        // Migrate watch structures if username changed.
        if (!targetUsername.equals(newUsername)) {
            Set<String> watched = userWatchlist.remove(targetUsername);
            if (watched != null) {
                userWatchlist.put(newUsername, watched);
            }

            Map<String, Integer> known = watchKnownVersion.remove(targetUsername);
            if (known != null) {
                watchKnownVersion.put(newUsername, known);
            }
        }
    }

    // -------- Categories ----------

    /**
     * Returns a read-only snapshot of all document categories.
     *
     * @return immutable set of category names
     */
    public Set<String> getDocumentCategoriesReadOnly() {
        return Set.copyOf(documentCategories);
    }

    /**
     * Adds a new document category (admin-only operation).
     *
     * @param admin admin user performing the operation
     * @param categoryName category name to add
     * @throws SecurityException if caller is not admin
     * @throws IllegalArgumentException if blank or already exists
     */
    public void addDocumentCategory(UserAccount admin, String categoryName) {
        requireAdmin(admin);
        requireNonBlank(categoryName);

        if (!documentCategories.add(categoryName)) {
            throw new IllegalArgumentException("Category already exists: " + categoryName);
        }
    }

    /**
     * Renames a document category (admin-only operation).
     *
     * <p>Side-effects:</p>
     * <ul>
     *   <li>Updates users' allowed categories</li>
     *   <li>Updates documents that belong to the renamed category</li>
     * </ul>
     *
     * @param admin admin user performing the operation
     * @param oldName existing category name
     * @param newName new category name
     * @throws SecurityException if caller is not admin
     * @throws IllegalArgumentException if names are blank, old not found, or new already exists
     */
    public void renameDocumentCategory(UserAccount admin, String oldName, String newName) {
        requireAdmin(admin);
        requireNonBlank(oldName);
        requireNonBlank(newName);

        if (!documentCategories.contains(oldName)) {
            throw new IllegalArgumentException("Category not found: " + oldName);
        }
        if (documentCategories.contains(newName)) {
            throw new IllegalArgumentException("Category already exists: " + newName);
        }

        documentCategories.remove(oldName);
        documentCategories.add(newName);

        for (UserAccount u : users) {
            u.replaceAllowedCategory(oldName, newName);
        }

        for (Document d : documents) {
            if (oldName.equals(d.getCategory())) {
                d.applyCategoryRename(newName);
            }
        }
    }

    /**
     * Deletes a document category (admin-only operation).
     *
     * <p>Side-effects:</p>
     * <ul>
     *   <li>Deletes all documents that belong to the category</li>
     *   <li>Removes the category from users' allowed categories</li>
     *   <li>Removes deleted document ids from watchlists and known-version maps</li>
     * </ul>
     *
     * @param admin admin user performing the operation
     * @param categoryName category name to delete
     * @throws SecurityException if caller is not admin
     * @throws IllegalArgumentException if blank or not found
     */
    public void deleteDocumentCategory(UserAccount admin, String categoryName) {
        requireAdmin(admin);
        requireNonBlank(categoryName);

        if (!documentCategories.contains(categoryName)) {
            throw new IllegalArgumentException("Category not found: " + categoryName);
        }

        Set<String> deletedDocIds = new HashSet<>();
        for (Document d : documents) {
            if (categoryName.equals(d.getCategory())) {
                deletedDocIds.add(d.getId());
            }
        }

        documents.removeIf(d -> categoryName.equals(d.getCategory()));
        documentCategories.remove(categoryName);

        for (UserAccount u : users) {
            u.removeAllowedCategory(categoryName);
        }

        for (Set<String> watchedIds : userWatchlist.values()) {
            watchedIds.removeAll(deletedDocIds);
        }
        for (Map<String, Integer> known : watchKnownVersion.values()) {
            known.keySet().removeAll(deletedDocIds);
        }
    }

    // -------- Documents ----------

    /**
     * Returns a read-only snapshot of all documents in the system.
     *
     * @return immutable list of documents
     */
    public List<Document> getDocumentsReadOnly() {
        return List.copyOf(documents);
    }

    /**
     * Creates a document without permission checks (legacy helper).
     * New documents start at version 1.
     *
     * @param title document title
     * @param authorName author name
     * @param category category name
     * @param text initial text (may include paragraph separators)
     * @return created document
     */
    public Document createDocument(String title, String authorName, String category, String text) {
        Document doc = new Document(title, authorName, category, LocalDateTime.now(), text);
        documents.add(doc);
        return doc;
    }

    /**
     * Edits a document without permission checks (legacy helper).
     * Creates a new version with incremented version number.
     *
     * @param doc target document
     * @param newText new content
     */
    public void editDocumentText(Document doc, String newText) {
        doc.addNewVersion(newText, LocalDateTime.now());
    }

    /**
     * Returns documents accessible by a user, based on role and allowed categories.
     *
     * @param user requesting user
     * @return list of accessible documents
     * @throws IllegalArgumentException if user is null
     */
    public List<Document> getAccessibleDocumentsForUser(UserAccount user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (user.getRole() == UserRole.ADMIN) {
            return List.copyOf(documents);
        }
        return documents.stream()
                .filter(d -> user.canAccessCategory(d.getCategory()))
                .toList();
    }

    /**
     * Searches accessible documents by optional criteria (category, title, author).
     *
     * @param user requesting user (search scope is limited to accessible documents)
     * @param category category filter (optional)
     * @param title title substring filter (optional, case-insensitive)
     * @param authorName author filter (optional, case-insensitive exact match)
     * @return matching documents (accessible scope)
     * @throws IllegalArgumentException if user is null
     */
    public List<Document> searchDocuments(UserAccount user, String category, String title, String authorName) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        List<Document> base = getAccessibleDocumentsForUser(user);

        return base.stream()
                .filter(d -> category == null || category.isBlank() || d.getCategory().equalsIgnoreCase(category))
                .filter(d -> title == null || title.isBlank() || d.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(d -> authorName == null || authorName.isBlank() || d.getAuthorName().equalsIgnoreCase(authorName))
                .toList();
    }

    /**
     * Creates a new document with permission checks (AUTHOR/ADMIN).
     *
     * @param actor user performing the operation
     * @param title document title
     * @param authorName author name
     * @param category category name (must exist)
     * @param text initial text
     * @return created document
     * @throws SecurityException if caller lacks permissions
     * @throws IllegalArgumentException for blank fields or unknown category
     */
    public Document createDocument(UserAccount actor, String title, String authorName, String category, String text) {
        requireAuthorOrAdmin(actor);
        requireNonBlank(title);
        requireNonBlank(authorName);
        requireNonBlank(category);
        requireNonBlank(text);

        if (!documentCategories.contains(category)) {
            throw new IllegalArgumentException("Unknown category: " + category);
        }

        if (actor.getRole() == UserRole.AUTHOR && !actor.canAccessCategory(category)) {
            throw new SecurityException("Author cannot create document in this category");
        }

        Document doc = new Document(title, authorName, category, LocalDateTime.now(), text);
        documents.add(doc);
        return doc;
    }

    /**
     * Edits a document with permission checks (AUTHOR/ADMIN).
     * Creates a new document version and keeps older versions.
     *
     * @param actor user performing the operation
     * @param doc target document
     * @param newText new document content
     * @throws SecurityException if caller lacks permissions
     * @throws IllegalArgumentException if document is null or text is blank
     */
    public void editDocumentText(UserAccount actor, Document doc, String newText) {
        requireAuthorOrAdmin(actor);
        if (doc == null) {
            throw new IllegalArgumentException("Document is required");
        }
        requireNonBlank(newText);

        if (actor.getRole() == UserRole.AUTHOR && !actor.canAccessCategory(doc.getCategory())) {
            throw new SecurityException("Author cannot edit document in this category");
        }

        doc.addNewVersion(newText, LocalDateTime.now());
    }

    /**
     * Deletes a document with permission checks (AUTHOR/ADMIN).
     * Removes all versions and clears references from watchlists/known-version maps.
     *
     * @param actor user performing the operation
     * @param doc target document
     * @throws SecurityException if caller lacks permissions
     * @throws IllegalArgumentException if document is null or not found
     */
    public void deleteDocument(UserAccount actor, Document doc) {
        requireAuthorOrAdmin(actor);
        if (doc == null) {
            throw new IllegalArgumentException("Document is required");
        }

        if (actor.getRole() == UserRole.AUTHOR && !actor.canAccessCategory(doc.getCategory())) {
            throw new SecurityException("Author cannot delete document in this category");
        }

        boolean removed = documents.removeIf(d -> d.getId().equals(doc.getId()));
        if (!removed) {
            throw new IllegalArgumentException("Document not found");
        }

        for (Set<String> watchedIds : userWatchlist.values()) {
            watchedIds.remove(doc.getId());
        }
        for (Map<String, Integer> known : watchKnownVersion.values()) {
            known.remove(doc.getId());
        }
    }

    // -------- Version visibility ----------

    /**
     * Returns the version numbers that are visible to a user for a given document.
     *
     * <p>Rule:</p>
     * <ul>
     *   <li>USER: latest only</li>
     *   <li>AUTHOR/ADMIN: latest + previous (2 total)</li>
     * </ul>
     *
     * @param user requesting user
     * @param doc target document
     * @return list of visible version numbers (latest-first)
     * @throws IllegalArgumentException if user or document is null
     * @throws SecurityException if user cannot access the document category
     */
    public List<Integer> getVisibleVersionNumbers(UserAccount user, Document doc) {
        if (user == null || doc == null) {
            throw new IllegalArgumentException("User and document are required");
        }

        if (user.getRole() != UserRole.ADMIN && !user.canAccessCategory(doc.getCategory())) {
            throw new SecurityException("User cannot access this document");
        }

        if (user.getRole() == UserRole.USER) {
            return doc.getLatestVersionNumbers(1);
        }

        return doc.getLatestVersionNumbers(2);
    }

    // -------- Login notifications ----------

    /**
     * On user login, checks watched documents for updates and returns the ones
     * that have a newer version than the user's last known version.
     *
     * <p>After the check, the user's known-version map is updated to the current version.</p>
     *
     * @param user logged-in user
     * @return list of watched documents that have newer versions
     * @throws IllegalArgumentException if user is null
     */
    public List<Document> loginAndGetWatchedDocumentsWithNewVersion(UserAccount user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        String username = user.getUsername();
        Set<String> watchedIds = userWatchlist.getOrDefault(username, Set.of());
        Map<String, Integer> known = watchKnownVersion.computeIfAbsent(username, k -> new HashMap<>());

        List<Document> updates = new ArrayList<>();

        for (String docId : watchedIds) {
            Document doc = documents.stream()
                    .filter(d -> d.getId().equals(docId))
                    .findFirst()
                    .orElse(null);

            if (doc == null) {
                continue;
            }

            int current = doc.getLatestVersionNumber();
            int lastKnown = known.getOrDefault(docId, current);

            if (current > lastKnown) {
                updates.add(doc);
            }

            known.put(docId, current);
        }

        return updates;
    }

    // -------- Watchlists ----------

    /**
     * Adds a document to the user's watchlist and records the current version as known.
     *
     * @param user requesting user
     * @param doc target document
     * @throws IllegalArgumentException if user or document is null
     * @throws SecurityException if user is not admin and lacks category access
     */
    public void watchDocument(UserAccount user, Document doc) {
        if (user == null || doc == null) {
            throw new IllegalArgumentException("User and document are required");
        }

        if (user.getRole() != UserRole.ADMIN && !user.canAccessCategory(doc.getCategory())) {
            throw new SecurityException("User cannot watch document outside allowed categories");
        }

        userWatchlist
                .computeIfAbsent(user.getUsername(), k -> new HashSet<>())
                .add(doc.getId());

        watchKnownVersion
                .computeIfAbsent(user.getUsername(), k -> new HashMap<>())
                .put(doc.getId(), doc.getLatestVersionNumber());
    }

    /**
     * Removes a document from the user's watchlist and deletes the stored known-version entry.
     *
     * @param user requesting user
     * @param doc target document
     * @throws IllegalArgumentException if user or document is null
     */
    public void unwatchDocument(UserAccount user, Document doc) {
        if (user == null || doc == null) {
            throw new IllegalArgumentException("User and document are required");
        }

        Set<String> set = userWatchlist.get(user.getUsername());
        if (set != null) {
            set.remove(doc.getId());
        }

        Map<String, Integer> known = watchKnownVersion.get(user.getUsername());
        if (known != null) {
            known.remove(doc.getId());
        }
    }

    /**
     * Returns a read-only snapshot of watched document ids for a user.
     *
     * @param user requesting user
     * @return immutable set of document ids
     */
    public Set<String> getWatchedDocumentIdsReadOnly(UserAccount user) {
        return Set.copyOf(userWatchlist.getOrDefault(user.getUsername(), Set.of()));
    }

    /**
     * Returns a read-only snapshot of watched document ids for a username.
     *
     * @param username target username
     * @return immutable set of document ids
     */
    public Set<String> getWatchedDocumentIdsByUsernameReadOnly(String username) {
        return Set.copyOf(userWatchlist.getOrDefault(username, Set.of()));
    }

    // -------- Persistence bridge ----------

    /**
     * Loads full application state into memory (startup).
     *
     * @param state state loaded from JSON files
     * @throws IllegalArgumentException if state is null
     */
    public void loadFrom(AppState state) {
        if (state == null) {
            throw new IllegalArgumentException("State is required");
        }

        users.clear();
        documents.clear();
        documentCategories.clear();
        userWatchlist.clear();
        watchKnownVersion.clear();

        if (state.users != null) users.addAll(state.users);
        if (state.documents != null) documents.addAll(state.documents);
        if (state.categories != null) documentCategories.addAll(state.categories);
        if (state.watchlists != null) userWatchlist.putAll(state.watchlists);
        if (state.watchKnownVersion != null) watchKnownVersion.putAll(state.watchKnownVersion);

        boolean hasDefaultAdmin = users.stream().anyMatch(u -> "medialab".equals(u.getUsername()));
        if (!hasDefaultAdmin) {
            users.add(new UserAccount(
                    "Default",
                    "Admin",
                    "medialab",
                    "medialab_2025",
                    UserRole.ADMIN,
                    Set.of()
            ));
        }
    }

    /**
     * Exports full in-memory application state for persistence (shutdown).
     *
     * @return populated {@link AppState} snapshot
     */
    public AppState exportState() {
        AppState state = new AppState();

        state.users = new ArrayList<>(users);
        state.documents = new ArrayList<>(documents);
        state.categories = new HashSet<>(documentCategories);

        Map<String, Set<String>> wl = new HashMap<>();
        for (var e : userWatchlist.entrySet()) {
            wl.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        state.watchlists = wl;

        Map<String, Map<String, Integer>> kv = new HashMap<>();
        for (var e : watchKnownVersion.entrySet()) {
            kv.put(e.getKey(), new HashMap<>(e.getValue()));
        }
        state.watchKnownVersion = kv;

        return state;
    }

    // -------- Helpers ----------

    /**
     * Ensures the given user is an administrator.
     *
     * @param user acting user
     * @throws SecurityException if user is null or not admin
     */
    private static void requireAdmin(UserAccount user) {
        if (user == null || user.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Admin privileges required");
        }
    }

    /**
     * Ensures the given user is either AUTHOR or ADMIN.
     *
     * @param user acting user
     * @throws SecurityException if user is null or not AUTHOR/ADMIN
     */
    private static void requireAuthorOrAdmin(UserAccount user) {
        if (user == null || (user.getRole() != UserRole.AUTHOR && user.getRole() != UserRole.ADMIN)) {
            throw new SecurityException("Author/Admin privileges required");
        }
    }

    /**
     * Ensures a string value is not null/blank.
     *
     * @param s input string
     * @throws IllegalArgumentException if null or blank
     */
    private static void requireNonBlank(String s) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("Value cannot be blank");
        }
    }
}