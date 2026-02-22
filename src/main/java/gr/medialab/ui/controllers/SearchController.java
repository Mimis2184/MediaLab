package gr.medialab.ui.controllers;

import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;
import gr.medialab.domain.UserRole;
import gr.medialab.logic.MediaLabManager;
import gr.medialab.ui.AppContext;
import gr.medialab.ui.viewmodels.DocumentSearchRow;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SearchController {

    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField titleField;
    @FXML private TextField authorField;

    @FXML private TableView<DocumentSearchRow> resultsTable;
    @FXML private TableColumn<DocumentSearchRow, String> titleCol;
    @FXML private TableColumn<DocumentSearchRow, String> authorCol;
    @FXML private TableColumn<DocumentSearchRow, String> categoryCol;
    @FXML private TableColumn<DocumentSearchRow, String> createdAtCol;
    @FXML private TableColumn<DocumentSearchRow, Integer> versionCol;

    private static final String ANY = "(Any)";
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        AppContext.setSearchController(this);

        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        createdAtCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        versionCol.setCellValueFactory(new PropertyValueFactory<>("version"));

        loadCategoryOptions();
    }

    // Χρησιμοποιείται από CategoriesController για auto-refresh
    public void reloadCategoriesPublic() {
        loadCategoryOptions();
    }

    private void loadCategoryOptions() {
        MediaLabManager manager = AppContext.manager();
        UserAccount user = AppContext.currentUser();

        Set<String> accessibleCategories = (user.getRole() == UserRole.ADMIN)
                ? manager.getDocumentCategoriesReadOnly()
                : user.getAllowedCategoriesReadOnly();

        List<String> items = accessibleCategories.stream()
                .sorted(Comparator.naturalOrder())
                .toList();

        var all = FXCollections.<String>observableArrayList();
        all.add(ANY);
        all.addAll(items);

        categoryCombo.setItems(all);
        categoryCombo.getSelectionModel().select(ANY);
    }

    @FXML
    private void onSearch() {
        MediaLabManager manager = AppContext.manager();
        UserAccount user = AppContext.currentUser();

        String selectedCategory = categoryCombo.getValue();
        String titleQ = normalize(titleField.getText());
        String authorQ = normalize(authorField.getText());

        List<Document> accessibleDocs = manager.getAccessibleDocumentsForUser(user);

        List<DocumentSearchRow> rows = accessibleDocs.stream()
                .filter(d -> selectedCategory == null || ANY.equals(selectedCategory) || selectedCategory.equals(d.getCategory()))
                .filter(d -> titleQ.isEmpty() || normalize(d.getTitle()).contains(titleQ))
                .filter(d -> authorQ.isEmpty() || normalize(d.getAuthorName()).contains(authorQ))
                .map(d -> new DocumentSearchRow(
                        d.getTitle(),
                        d.getAuthorName(),
                        d.getCategory(),
                        d.getCreatedAt().format(dtf),
                        d.getLatestVersionNumber()
                ))
                .toList();

        resultsTable.setItems(FXCollections.observableArrayList(rows));
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase(Locale.ROOT);
    }
}