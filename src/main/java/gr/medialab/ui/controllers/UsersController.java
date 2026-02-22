package gr.medialab.ui.controllers;

import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import gr.medialab.ui.AppContext;
import gr.medialab.ui.viewmodels.UserRow;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UsersController {

    @FXML private TableView<UserRow> usersTable;
    @FXML private TableColumn<UserRow, String> firstNameCol;
    @FXML private TableColumn<UserRow, String> lastNameCol;
    @FXML private TableColumn<UserRow, String> usernameCol;
    @FXML private TableColumn<UserRow, UserRole> roleCol;
    @FXML private TableColumn<UserRow, String> allowedCol;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<UserRole> roleBox;
    @FXML private ListView<String> categoriesList;

    @FXML private Label infoLabel;

    @FXML
    private void initialize() {
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        allowedCol.setCellValueFactory(new PropertyValueFactory<>("allowedCategoriesText"));

        roleBox.setItems(FXCollections.observableArrayList(UserRole.values()));
        categoriesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        refreshAll();
    }

    @FXML
    private void onRefresh() {
        refreshAll();
        infoLabel.setText("Refreshed.");
    }

    @FXML
    private void onLoadSelected() {
        UserRow row = usersTable.getSelectionModel().getSelectedItem();
        if (row == null) {
            infoLabel.setText("Select a user first.");
            return;
        }

        MediaLabManager manager = AppContext.manager();
        UserAccount u = manager.getUserByUsername(row.getUsername());

        firstNameField.setText(u.getFirstName());
        lastNameField.setText(u.getLastName());
        usernameField.setText(u.getUsername());
        passwordField.setText(u.getPassword());
        roleBox.getSelectionModel().select(u.getRole());

        categoriesList.getSelectionModel().clearSelection();
        for (String c : u.getAllowedCategoriesReadOnly()) {
            int idx = categoriesList.getItems().indexOf(c);
            if (idx >= 0) categoriesList.getSelectionModel().select(idx);
        }

        infoLabel.setText("Loaded: " + u.getUsername());
    }

    @FXML
    private void onAddUser() {
        MediaLabManager manager = AppContext.manager();
        UserAccount admin = AppContext.currentUser();

        try {
            Set<String> allowed = new HashSet<>(categoriesList.getSelectionModel().getSelectedItems());

            manager.addUser(
                    admin,
                    firstNameField.getText(),
                    lastNameField.getText(),
                    usernameField.getText(),
                    passwordField.getText(),
                    roleBox.getValue(),
                    allowed
            );

            refreshAll();
            AppContext.mainController().refreshSummary();
            infoLabel.setText("User added.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onSaveChanges() {
        MediaLabManager manager = AppContext.manager();
        UserAccount admin = AppContext.currentUser();

        UserRow selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            infoLabel.setText("Select a user first.");
            return;
        }

        try {
            Set<String> allowed = new HashSet<>(categoriesList.getSelectionModel().getSelectedItems());

            manager.updateUser(
                    admin,
                    selected.getUsername(),     // target user (παλιό username)
                    firstNameField.getText(),
                    lastNameField.getText(),
                    usernameField.getText(),    // νέο username
                    passwordField.getText(),
                    roleBox.getValue(),
                    allowed
            );

            refreshAll();
            AppContext.mainController().refreshSummary();
            infoLabel.setText("User updated.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onDeleteSelected() {
        MediaLabManager manager = AppContext.manager();
        UserAccount admin = AppContext.currentUser();

        UserRow row = usersTable.getSelectionModel().getSelectedItem();
        if (row == null) {
            infoLabel.setText("Select a user first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete User");
        confirm.setHeaderText("Delete user: " + row.getUsername() + " ?");
        confirm.setContentText("User will be removed from the system.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            manager.deleteUser(admin, row.getUsername());
            refreshAll();
            AppContext.mainController().refreshSummary();
            infoLabel.setText("User deleted.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshAll() {
        MediaLabManager manager = AppContext.manager();

        var cats = manager.getDocumentCategoriesReadOnly().stream()
                .sorted(Comparator.naturalOrder())
                .toList();
        categoriesList.setItems(FXCollections.observableArrayList(cats));

        var rows = manager.getUsersReadOnly().stream()
                .sorted(Comparator.comparing(UserAccount::getUsername))
                .map(u -> new UserRow(
                        u.getFirstName(),
                        u.getLastName(),
                        u.getUsername(),
                        u.getRole(),
                        u.getAllowedCategoriesReadOnly().stream().sorted().collect(Collectors.joining(", "))
                ))
                .toList();

        usersTable.setItems(FXCollections.observableArrayList(rows));
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText("Operation failed");
        a.setContentText(msg);
        a.showAndWait();
    }
}