package gr.medialab.ui.controllers;

import gr.medialab.domain.UserAccount;
import gr.medialab.logic.MediaLabManager;
import gr.medialab.ui.AppContext;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Comparator;

public class CategoriesController {

    @FXML private TextField newCategoryField;
    @FXML private TextField renameCategoryField;
    @FXML private ListView<String> categoriesList;
    @FXML private Label infoLabel;

    @FXML
    private void initialize() {
        refreshCategories();
    }

    @FXML
    private void onAddCategory() {
        MediaLabManager manager = AppContext.manager();
        UserAccount admin = AppContext.currentUser();

        try {
            manager.addDocumentCategory(admin, newCategoryField.getText());
            newCategoryField.clear();
            refreshCategories();
            AppContext.mainController().refreshSummary();
            infoLabel.setText("Category added.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
        var dc = AppContext.documentsController();
        if (dc != null) dc.refreshCategoriesPublic();

        var sc = AppContext.searchController();
        if (sc != null) sc.reloadCategoriesPublic();
    }

    @FXML
    private void onRenameSelected() {
        MediaLabManager manager = AppContext.manager();
        UserAccount admin = AppContext.currentUser();

        String oldName = categoriesList.getSelectionModel().getSelectedItem();
        if (oldName == null) {
            infoLabel.setText("Select a category first.");
            return;
        }

        try {
            manager.renameDocumentCategory(admin, oldName, renameCategoryField.getText());
            renameCategoryField.clear();
            refreshCategories();
            AppContext.mainController().refreshSummary();
            infoLabel.setText("Category renamed.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
        var dc = AppContext.documentsController();
        if (dc != null) dc.refreshCategoriesPublic();

        var sc = AppContext.searchController();
        if (sc != null) sc.reloadCategoriesPublic();
    }

    @FXML
    private void onDeleteSelected() {
        MediaLabManager manager = AppContext.manager();
        UserAccount admin = AppContext.currentUser();

        String name = categoriesList.getSelectionModel().getSelectedItem();
        if (name == null) {
            infoLabel.setText("Select a category first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Category");
        confirm.setHeaderText("Delete category: " + name + " ?");
        confirm.setContentText("All documents in this category will be deleted.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            manager.deleteDocumentCategory(admin, name);
            refreshCategories();
            AppContext.mainController().refreshSummary();
            infoLabel.setText("Category deleted.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
        var dc = AppContext.documentsController();
        if (dc != null) dc.refreshCategoriesPublic();

        var sc = AppContext.searchController();
        if (sc != null) sc.reloadCategoriesPublic();

        var wl = AppContext.watchlistController();
        if (wl != null) wl.refreshTablePublic();
    }

    private void refreshCategories() {
        MediaLabManager manager = AppContext.manager();

        var items = manager.getDocumentCategoriesReadOnly().stream()
                .sorted(Comparator.naturalOrder())
                .toList();

        categoriesList.setItems(FXCollections.observableArrayList(items));
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText("Operation failed");
        a.setContentText(msg);
        a.showAndWait();
    }
}