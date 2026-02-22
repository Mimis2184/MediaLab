package gr.medialab.ui.controllers;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.logic.MediaLabManager;
import gr.medialab.ui.AppContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void onLogin() throws Exception {
        String u = usernameField.getText();
        String p = passwordField.getText();

        MediaLabManager manager = AppContext.manager();
        UserAccount user = manager.authenticate(u, p);

        if (user == null) {
            errorLabel.setText("Invalid credentials.");
            return;
        }

        AppContext.setCurrentUser(user);

        List<Document> updatedWatched = manager.loginAndGetWatchedDocumentsWithNewVersion(user);
        if (!updatedWatched.isEmpty()) {
            String titles = updatedWatched.stream()
                    .map(Document::getTitle)
                    .collect(Collectors.joining("\n"));

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Updates");
            alert.setHeaderText("Watched documents have new versions:");
            alert.setContentText(titles);
            alert.showAndWait();
        }

        Stage stage = (Stage) usernameField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gr/medialab/ui/fxml/main.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);
        stage.setScene(scene);
        stage.setTitle("MediaLab Documents");
        stage.show();
    }
}