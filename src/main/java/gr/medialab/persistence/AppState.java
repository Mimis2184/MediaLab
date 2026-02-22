package gr.medialab.persistence;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppState {
    // Όλοι οι χρήστες του συστήματος
    public List<UserAccount> users;

    // Όλες οι κατηγορίες εγγράφων
    public Set<String> categories;

    // Όλα τα έγγραφα (μαζί με τις εκδόσεις τους)
    public List<Document> documents;

    // username -> watched documentIds
    public Map<String, Set<String>> watchlists;

    // username -> (documentId -> lastKnownVersionAtLogin)
    public Map<String, Map<String, Integer>> watchKnownVersion;
}
