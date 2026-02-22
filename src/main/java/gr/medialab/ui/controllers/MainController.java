package gr.medialab.ui.controllers;

import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import gr.medialab.ui.AppContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MainController {

    @FXML private Label categoriesCountLabel;
    @FXML private Label documentsCountLabel;
    @FXML private Label watchedCountLabel;

    @FXML private TabPane tabPane;
    @FXML private Tab categoriesTab;
    @FXML private Tab usersTab;

    @FXML private Label loggedInAsLabel;

    @FXML
    private void initialize() {
        AppContext.setMainController(this);
        refreshSummary();

        UserAccount user = AppContext.currentUser();
        loggedInAsLabel.setText("Logged in as: " + user.getUsername() + " (" + user.getRole() + ")");

        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        if (!isAdmin) {
            tabPane.getTabs().removeAll(categoriesTab, usersTab);
        }
    }

    public void refreshSummary() {
        MediaLabManager manager = AppContext.manager();
        UserAccount user = AppContext.currentUser();

        categoriesCountLabel.setText(String.valueOf(manager.getDocumentCategoriesReadOnly().size()));
        documentsCountLabel.setText(String.valueOf(manager.getDocumentsReadOnly().size()));
        watchedCountLabel.setText(String.valueOf(manager.getWatchedDocumentIdsReadOnly(user).size()));
    }

    @FXML
    private void onLogout() throws Exception {
        AppContext.setCurrentUser(null);

        Stage stage = (Stage) tabPane.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gr/medialab/ui/fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);
        stage.setScene(scene);
        stage.setTitle("MediaLab Documents");
        stage.show();
    }
}