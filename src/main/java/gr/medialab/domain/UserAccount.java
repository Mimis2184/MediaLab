package gr.medialab.domain;

import java.util.HashSet;
import java.util.Set;

public class UserAccount {
    private final String firstName;
    private final String lastName;
    private final String username;
    private final String password;
    private final UserRole role;

    private final Set<String> allowedCategories = new HashSet<>();

    public UserAccount(String firstName,
                       String lastName,
                       String username,
                       String password,
                       UserRole role,
                       Set<String> allowedCategories) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.role = role;

        if (allowedCategories != null) {
            this.allowedCategories.addAll(allowedCategories);
        }
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }

    public Set<String> getAllowedCategoriesReadOnly() {
        return Set.copyOf(allowedCategories);
    }

    public boolean canAccessCategory(String categoryName) {
        return allowedCategories.contains(categoryName);
    }

    public void replaceAllowedCategory(String oldName, String newName) {
        if (allowedCategories.remove(oldName)) {
            allowedCategories.add(newName);
        }
    }

    public void removeAllowedCategory(String categoryName) {
        allowedCategories.remove(categoryName);
    }
}
