package gr.medialab.ui.viewmodels;

import gr.medialab.domain.UserRole;

public class UserRow {
    private final String firstName;
    private final String lastName;
    private final String username;
    private final UserRole role;
    private final String allowedCategoriesText;

    public UserRow(String firstName, String lastName, String username, UserRole role, String allowedCategoriesText) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.role = role;
        this.allowedCategoriesText = allowedCategoriesText;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUsername() { return username; }
    public UserRole getRole() { return role; }
    public String getAllowedCategoriesText() { return allowedCategoriesText; }
}