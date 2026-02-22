package gr.medialab.ui.controllers;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import gr.medialab.ui.AppContext;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class DocumentsController {

    @FXML private ComboBox<String> categoryBox;
    @FXML private TableView<Document> documentsTable;
    @FXML private TableColumn<Document, String> titleCol;
    @FXML private TableColumn<Document, String> authorCol;
    @FXML private TableColumn<Document, String> dateCol;
    @FXML private TableColumn<Document, Integer> versionCol;

    @FXML private ComboBox<Integer> versionBox;
    @FXML private TextArea textArea;

    @FXML private Button watchBtn;
    @FXML private Button createBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;

    @FXML private Label infoLabel;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        AppContext.setDocumentsController(this);

        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("authorName"));

        dateCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getCreatedAt().format(fmt)));

        versionCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getLatestVersionNumber()));

        documentsTable.getSelectionModel().selectedItemProperty().addListener((ignoredObs, ignoredOld, newV) ->
                onDocumentSelected(newV)
        );

        // Περιμένει να φορτωθεί το main.fxml και να έχει setMainController
        Platform.runLater(() -> {
            applyRoleVisibility();
            refreshCategories();
        });
    }

    // Χρησιμοποιείται από CategoriesController για auto-refresh
    public void refreshCategoriesPublic() {
        Platform.runLater(this::refreshCategories);
    }

    private void applyRoleVisibility() {
        UserAccount user = AppContext.currentUser();
        boolean canManageDocs = user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.AUTHOR;

        createBtn.setVisible(canManageDocs);
        createBtn.setManaged(canManageDocs);

        editBtn.setVisible(canManageDocs);
        editBtn.setManaged(canManageDocs);

        deleteBtn.setVisible(canManageDocs);
        deleteBtn.setManaged(canManageDocs);

        versionBox.setVisible(canManageDocs);
        versionBox.setManaged(canManageDocs);
    }

    private void refreshCategories() {
        MediaLabManager manager = AppContext.manager();
        UserAccount user = AppContext.currentUser();

        List<String> cats = manager.getDocumentCategoriesReadOnly().stream()
                .sorted(Comparator.naturalOrder())
                .filter(c -> user.getRole() == UserRole.ADMIN || user.canAccessCategory(c))
                .toList();

        categoryBox.setItems(FXCollections.observableArrayList(cats));

        if (!cats.isEmpty()) {
            categoryBox.getSelectionModel().select(0);
            refreshDocuments();
        } else {
            documentsTable.setItems(FXCollections.observableArrayList());
            textArea.clear();
            versionBox.getItems().clear();
            updateWatchButton(null);

            var mc = AppContext.mainController();
            if (mc != null) mc.refreshSummary();
        }
    }

    @FXML
    private void onCategoryChanged() {
        refreshDocuments();
    }

    private void refreshDocuments() {
        MediaLabManager manager = AppContext.manager();
        UserAccount user = AppContext.currentUser();

        String category = categoryBox.getValue();
        if (category == null) {
            documentsTable.setItems(FXCollections.observableArrayList());
            return;
        }

        List<Document> docs = manager.getAccessibleDocumentsForUser(user).stream()
                .filter(d -> category.equals(d.getCategory()))
                .sorted(Comparator.comparing(Document::getTitle))
                .toList();

        documentsTable.setItems(FXCollections.observableArrayList(docs));

        if (!docs.isEmpty()) {
            documentsTable.getSelectionModel().select(0);
        } else {
            textArea.clear();
            versionBox.getItems().clear();
            updateWatchButton(null);
        }

        var mc = AppContext.mainController();
        if (mc != null) mc.refreshSummary();
    }

    private void onDocumentSelected(Document doc) {
        if (doc == null) {
            textArea.clear();
            versionBox.getItems().clear();
            updateWatchButton(null);
            return;
        }

        updateWatchButton(doc);
        refreshVisibleVersions(doc);
        showTextForCurrentSelection(doc);
    }

    private void refreshVisibleVersions(Document doc) {
        MediaLabManager manager = AppContext.manager();
        UserAccount user = AppContext.currentUser();

        List<Integer> visible = manager.getVisibleVersionNumbers(user, doc);
        versionBox.setItems(FXCollections.observableArrayList(visible));

        if (!visible.isEmpty()) {
            versionBox.getSelectionModel().select(0);
        }
    }

    @FXML
    private void onVersionChanged() {
        Document doc = documentsTable.getSelectionModel().getSelectedItem();
        if (doc != null) showTextForCurrentSelection(doc);
    }

    private void showTextForCurrentSelection(Document doc) {
        UserAccount user = AppContext.currentUser();

        if (user.getRole() == UserRole.USER) {
            textArea.setText(doc.getLatestText());
            textArea.setEditable(false);
            return;
        }

        Integer v = versionBox.getValue();
        textArea.setText(v == null ? doc.getLatestText() : doc.getTextOfVersion(v));
        textArea.setEditable(false);
    }

    @FXML
    private void onToggleWatch() {
        Document doc = documentsTable.getSelectionModel().getSelectedItem();
        if (doc == null) return;

        MediaLabManager manager = AppContext.manager();
        UserAccount user = AppContext.currentUser();

        boolean watched = manager.getWatchedDocumentIdsReadOnly(user).contains(doc.getId());
        if (watched) manager.unwatchDocument(user, doc);
        else manager.watchDocument(user, doc);

        updateWatchButton(doc);

        var mc = AppContext.mainController();
        if (mc != null) mc.refreshSummary();

        var wl = AppContext.watchlistController();
        if (wl != null) wl.refreshTablePublic();
    }

    private void updateWatchButton(Document doc) {
        if (doc == null) {
            watchBtn.setText("Watch");
            return;
        }
        MediaLabManager manager = AppContext.manager();
        UserAccount user = AppContext.currentUser();

        boolean watched = manager.getWatchedDocumentIdsReadOnly(user).contains(doc.getId());
        watchBtn.setText(watched ? "Unwatch" : "Watch");
    }

    @FXML
    private void onCreate() {
        MediaLabManager manager = AppContext.manager();
        UserAccount actor = AppContext.currentUser();

        TextInputDialog t = new TextInputDialog();
        t.setTitle("Create Document");
        t.setHeaderText("Enter document title:");
        t.setContentText("Title:");
        String title = t.showAndWait().orElse(null);
        if (title == null) return;

        TextInputDialog a = new TextInputDialog(actor.getFirstName());
        a.setTitle("Create Document");
        a.setHeaderText("Enter author name:");
        a.setContentText("Author:");
        String author = a.showAndWait().orElse(null);
        if (author == null) return;

        Dialog<String> textDlg = new Dialog<>();
        textDlg.setTitle("Create Document");
        textDlg.setHeaderText("Enter initial text:");

        TextArea ta = new TextArea();
        ta.setWrapText(true);
        ta.setPrefHeight(260);
        textDlg.getDialogPane().setContent(ta);
        textDlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        textDlg.setResultConverter(btn -> btn == ButtonType.OK ? ta.getText() : null);
        String text = textDlg.showAndWait().orElse(null);
        if (text == null) return;

        try {
            manager.createDocument(actor, title, author, categoryBox.getValue(), text);
            refreshDocuments();
            infoLabel.setText("Document created.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onEditText() {
        MediaLabManager manager = AppContext.manager();
        UserAccount actor = AppContext.currentUser();
        Document doc = documentsTable.getSelectionModel().getSelectedItem();
        if (doc == null) return;

        Dialog<String> textDlg = new Dialog<>();
        textDlg.setTitle("Edit Document");
        textDlg.setHeaderText("New text (creates new version):");

        TextArea ta = new TextArea(doc.getLatestText());
        ta.setWrapText(true);
        ta.setPrefHeight(260);
        textDlg.getDialogPane().setContent(ta);
        textDlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        textDlg.setResultConverter(btn -> btn == ButtonType.OK ? ta.getText() : null);
        String newText = textDlg.showAndWait().orElse(null);
        if (newText == null) return;

        try {
            manager.editDocumentText(actor, doc, newText);
            refreshDocuments();
            infoLabel.setText("New version created.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        MediaLabManager manager = AppContext.manager();
        UserAccount actor = AppContext.currentUser();
        Document doc = documentsTable.getSelectionModel().getSelectedItem();
        if (doc == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Document");
        confirm.setHeaderText("Delete: " + doc.getTitle() + " ?");
        confirm.setContentText("Document and all versions will be removed.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            manager.deleteDocument(actor, doc);
            refreshDocuments();
            infoLabel.setText("Document deleted.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText("Operation failed");
        a.setContentText(msg);
        a.showAndWait();
    }
}