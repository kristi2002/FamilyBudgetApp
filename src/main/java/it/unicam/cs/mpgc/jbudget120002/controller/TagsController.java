package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.service.TagService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class TagsController extends BaseController {
    @FXML private TextField tfName;
    @FXML private ComboBox<Tag> cbParent;
    @FXML private TableView<Tag> table;
    @FXML private TableColumn<Tag, String> colName;
    @FXML private TableColumn<Tag, String> colParent;
    @FXML private TreeView<Tag> treeView;

    private TagService tagService;
    private ObservableList<Tag> tags;

    @Override
    protected void initializeServices() {
        tagService = serviceFactory.getTagService();
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
        cbParent.setItems(FXCollections.observableArrayList(tagService.findAll()));
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
        } catch (Exception e) {
            showError("Error", "Failed to create tag: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteTag() {
        Tag selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                tagService.deleteTag(selected.getId());
                tags.remove(selected);
                cbParent.getItems().remove(selected);
                updateParentComboBox();
                updateTreeView();
            } catch (Exception e) {
                showError("Error", "Failed to delete tag: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleUpdateTag() {
        Tag selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String name = tfName.getText().trim();
            Tag parent = cbParent.getValue();

            if (name.isEmpty()) {
                showWarning("Invalid Input", "Please enter a tag name.");
                return;
            }

            try {
                tagService.updateTag(selected.getId(), name, parent != null ? parent.getId() : null);
                refreshData();
                clearForm();
            } catch (Exception e) {
                showError("Error", "Failed to update tag: " + e.getMessage());
            }
        }
    }

    private void clearForm() {
        tfName.clear();
        cbParent.setValue(null);
    }

    @FXML
    private void handleTableSelection() {
        Tag selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            tfName.setText(selected.getName());
            cbParent.setValue(selected.getParent());
        }
    }
} 