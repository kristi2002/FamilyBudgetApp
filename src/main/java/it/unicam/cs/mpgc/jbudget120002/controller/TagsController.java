package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.model.User;
import it.unicam.cs.mpgc.jbudget120002.service.TagService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

/**
 * Controller class managing category tags in the Family Budget App.
 * This class handles the creation, organization, and management of
 * categorization tags used throughout the application.
 *
 * Responsibilities:
 * - Create and manage category tags
 * - Organize tags in hierarchical structures
 * - Handle tag relationships and dependencies
 * - Provide tag selection functionality
 * - Coordinate with TagService for persistence
 *
 * Usage:
 * Used by MainController to manage the tags tab and provide
 * category management functionality to users.
 */
public class TagsController extends BaseController {
    @FXML private TextField tfName;
    @FXML private ComboBox<Tag> cbParent;
    @FXML private TableView<Tag> table;
    @FXML private TableColumn<Tag, String> colName;
    @FXML private TableColumn<Tag, String> colParent;
    @FXML private TreeView<Tag> treeView;

    private TagService tagService;
    private ObservableList<Tag> tags;
    private User currentUser;

    @Override
    protected void initializeServices() {
        tagService = serviceFactory.getTagService(false);
        tags = FXCollections.observableArrayList();
    }

    @Override
    protected void setupUI() {
        // Setup table columns
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colParent.setCellValueFactory(cellData -> {
            Tag parent = cellData.getValue().getParent();
            return new SimpleStringProperty(parent != null ? parent.getName() : "");
        });

        // Setup parent combobox
        cbParent.setItems(FXCollections.observableArrayList(tagService.findRootTags()));
        
        // Setup tree view
        treeView.setRoot(new TreeItem<>(new Tag("Categories")));
        treeView.setShowRoot(true);
        treeView.getRoot().setExpanded(true);

        table.setItems(tags);
    }

    @Override
    protected void loadData() {
        refreshData();
    }

    public void refreshData() {
        List<Tag> allTags = tagService.findAll();
        tags.setAll(allTags);
        updateParentComboBox();
        updateTreeView();
    }

    private void updateParentComboBox() {
        List<Tag> allTags = tagService.findAll();
        Tag noneOption = new Tag("None");
        noneOption.setId(null);
        allTags.add(0, noneOption);
        cbParent.setItems(FXCollections.observableArrayList(allTags));
    }

    private void updateTreeView() {
        TreeItem<Tag> root = treeView.getRoot();
        root.getChildren().clear();
        
        List<Tag> rootTags = tagService.findRootTags();
        for (Tag tag : rootTags) {
            TreeItem<Tag> item = createTreeItem(tag);
            root.getChildren().add(item);
        }
    }

    private TreeItem<Tag> createTreeItem(Tag tag) {
        TreeItem<Tag> item = new TreeItem<>(tag);
        List<Tag> children = tagService.findChildTags(tag.getId());
        for (Tag child : children) {
            item.getChildren().add(createTreeItem(child));
        }
        return item;
    }

    @FXML
    private void handleAddTag() {
        String name = tfName.getText().trim();
        Tag parent = cbParent.getValue();
        if (parent != null && "None".equals(parent.getName())) {
            parent = null;
        }

        if (name.isEmpty()) {
            showWarning("Invalid Input", "Please enter a tag name.");
            return;
        }

        try {
            Tag tag = tagService.createTag(name, parent != null ? parent.getId() : null);
            tags.add(tag);
            cbParent.getItems().add(tag);
            updateParentComboBox();
            updateTreeView();
            clearForm();
            refreshTransactionTags();
        } catch (Exception e) {
            showError("Error", "Failed to create tag: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteTag() {
        Tag selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Delete Error", "No tag is selected. Please select a tag to delete.");
            return;
        }
        if (selected.getId() == null) {
            showWarning("Delete Error", "The selected tag has no ID and cannot be deleted. This may indicate a data issue. Try restarting the app or recreating the tag.");
            return;
        }
        try {
            tagService.deleteTag(selected.getId());
            tags.remove(selected);
            cbParent.getItems().remove(selected);
            updateParentComboBox();
            updateTreeView();
            refreshTransactionTags();
        } catch (Exception e) {
            showError("Error", "Failed to delete tag: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateTag() {
        Tag selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String name = tfName.getText().trim();
            Tag parent = cbParent.getValue();
            if (parent != null && "None".equals(parent.getName())) {
                parent = null;
            }

            if (name.isEmpty()) {
                showWarning("Invalid Input", "Please enter a tag name.");
                return;
            }

            try {
                tagService.updateTag(selected.getId(), name, parent != null ? parent.getId() : null);
                refreshData();
                clearForm();
                refreshTransactionTags();
            } catch (Exception e) {
                showError("Error", "Failed to update tag: " + e.getMessage());
            }
        }
    }

    private void clearForm() {
        tfName.clear();
        cbParent.setValue(cbParent.getItems().isEmpty() ? null : cbParent.getItems().get(0));
    }

    @FXML
    private void handleTableSelection() {
        Tag selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            tfName.setText(selected.getName());
            Tag parent = selected.getParent();
            if (parent == null && !cbParent.getItems().isEmpty()) {
                cbParent.setValue(cbParent.getItems().get(0)); // None
            } else {
                cbParent.setValue(parent);
            }
        }
    }

    private void refreshTransactionTags() {
        if (mainController != null) {
            // Refresh TransactionsController
            TransactionsController transactionsController = mainController.getTransactionsViewController();
            if (transactionsController != null) {
                transactionsController.refreshTags();
            }
            
            // Force refresh of all controllers that use category filters
            // This ensures tags appear immediately in all category dropdowns
            try {
                mainController.refreshAllViews();
            } catch (Exception e) {
                System.err.println("Error refreshing controllers: " + e.getMessage());
            }
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
} 