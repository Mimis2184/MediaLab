package gr.medialab.ui.controllers;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.logic.MediaLabManager;
import gr.medialab.ui.AppContext;
import gr.medialab.ui.viewmodels.WatchedDocumentRow;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Set;

public class WatchlistController {

    @FXML private TableView<WatchedDocumentRow> watchedTable;
    @FXML private TableColumn<WatchedDocumentRow, String> titleCol;
    @FXML private TableColumn<WatchedDocumentRow, String> authorCol;
    @FXML private TableColumn<WatchedDocumentRow, String> categoryCol;
    @FXML private TableColumn<WatchedDocumentRow, Integer> versionCol;

    @FXML private Label infoLabel;

    @FXML
    private void initialize() {
        // Register controller so other tabs (Documents) can trigger refresh.
        AppContext.setWatchlistController(this);

        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        versionCol.setCellValueFactory(new PropertyValueFactory<>("latestVersion"));

        refreshTablePublic();
    }

    @FXML
    private void onRefresh() {
        refreshTablePublic();
    }

    @FXML
    private void onUnwatchSelected() {
        WatchedDocumentRow row = watchedTable.getSelectionModel().getSelectedItem();
        if (row == null) {
            infoLabel.setText("Select a document first.");
            return;
        }

        MediaLabManager manager = AppContext.manager();
        UserAccount user = AppContext.currentUser();

        Document doc = manager.getDocumentsReadOnly().stream()
                .filter(d -> d.getId().equals(row.getDocumentId()))
                .findFirst()
                .orElse(null);

        if (doc != null) {
            manager.unwatchDocument(user, doc);
        }

        refreshTablePublic();
        AppContext.mainController().refreshSummary();
        infoLabel.setText("Unwatched: " + row.getTitle());
    }

    // Must be public so DocumentsController can call it.
    public void refreshTablePublic() {
        MediaLabManager manager = AppContext.manager();
        UserAccount user = AppContext.currentUser();

        Set<String> watchedIds = manager.getWatchedDocumentIdsReadOnly(user);

        List<WatchedDocumentRow> rows = manager.getDocumentsReadOnly().stream()
                .filter(d -> watchedIds.contains(d.getId()))
                .map(d -> new WatchedDocumentRow(
                        d.getId(),
                        d.getTitle(),
                        d.getAuthorName(),
                        d.getCategory(),
                        d.getLatestVersionNumber()
                ))
                .toList();

        watchedTable.setItems(FXCollections.observableArrayList(rows));
        infoLabel.setText("Watched count: " + rows.size());
    }
}