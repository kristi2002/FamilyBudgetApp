package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.model.Transaction;
import it.unicam.cs.mpgc.jbudget120002.service.TagService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TagController {
    private final TagService tagService;
    
    @FXML private TreeTableView<Tag> tagTreeTable;
    @FXML private TreeTableColumn<Tag, String> nameColumn;
    @FXML private TextField tagNameField;
    @FXML private Button addTagButton;
    @FXML private Button deleteTagButton;
    @FXML private VBox tagDetailsPane;
    @FXML private Label fullPathLabel;
    @FXML private Label childrenCountLabel;
    @FXML private Label transactionCountLabel;
    @FXML private Label totalAmountLabel;

    private TreeItem<Tag> draggedItem;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @FXML
    public void initialize() {
        setupTreeTable();
        setupDragAndDrop();
        setupAddTagButton();
        loadTags();
        
        // Disable buttons initially
        deleteTagButton.setDisable(true);
        
        // Enable/disable buttons based on selection
        tagTreeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            deleteTagButton.setDisable(newVal == null);
            if (newVal != null) {
                showTagDetails(newVal.getValue());
            } else {
                hideTagDetails();
            }
        });
    }

    private void setupAddTagButton() {
        // Enable add button only when there's text in the field
        addTagButton.setDisable(true);
        tagNameField.textProperty().addListener((obs, oldVal, newVal) -> 
            addTagButton.setDisable(newVal.trim().isEmpty()));
        
        // Add tag when Enter is pressed in the text field
        tagNameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !tagNameField.getText().trim().isEmpty()) {
                handleAddTag();
            }
        });
    }

    private void setupTreeTable() {
        nameColumn.setCellValueFactory(param -> 
            new SimpleStringProperty(param.getValue().getValue().getName()));
        
        // Add context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addMenuItem = new MenuItem("Add Child Tag");
        MenuItem deleteMenuItem = new MenuItem("Delete Tag");
        MenuItem renameMenuItem = new MenuItem("Rename Tag");
        
        addMenuItem.setOnAction(e -> {
            TreeItem<Tag> selected = tagTreeTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showAddTagDialog(selected.getValue().getId());
            }
        });
        
        deleteMenuItem.setOnAction(e -> deleteSelectedTag());
        
        renameMenuItem.setOnAction(e -> {
            TreeItem<Tag> selected = tagTreeTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showRenameDialog(selected.getValue());
            }
        });
        
        contextMenu.getItems().addAll(addMenuItem, renameMenuItem, new SeparatorMenuItem(), deleteMenuItem);
        tagTreeTable.setContextMenu(contextMenu);
    }

    private void setupDragAndDrop() {
        tagTreeTable.setOnDragDetected(event -> {
            TreeItem<Tag> selected = tagTreeTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                draggedItem = selected;
                Dragboard db = tagTreeTable.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(selected.getValue().getId().toString());
                db.setContent(content);
                event.consume();
            }
        });

        tagTreeTable.setOnDragOver(event -> {
            if (event.getGestureSource() != tagTreeTable && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        tagTreeTable.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString() && draggedItem != null) {
                TreeItem<Tag> target = tagTreeTable.getSelectionModel().getSelectedItem();
                if (target != null && !isAncestor(target, draggedItem)) {
                    try {
                        // Update the tag with new parent
                        tagService.updateTag(
                            draggedItem.getValue().getId(),
                            draggedItem.getValue().getName(),
                            target.getValue().getId()
                        );
                        success = true;
                        loadTags(); // Refresh the tree
                    } catch (IllegalArgumentException e) {
                        showError("Cannot move tag", e.getMessage());
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private boolean isAncestor(TreeItem<Tag> potential, TreeItem<Tag> item) {
        TreeItem<Tag> parent = potential.getParent();
        while (parent != null) {
            if (parent == item) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    @FXML
    private void handleAddTag() {
        String name = tagNameField.getText().trim();
        if (!name.isEmpty()) {
            TreeItem<Tag> selected = tagTreeTable.getSelectionModel().getSelectedItem();
            Long parentId = selected != null ? selected.getValue().getId() : null;
            try {
                tagService.createTag(name, parentId);
                tagNameField.clear();
                loadTags();
            } catch (IllegalArgumentException e) {
                showError("Cannot create tag", e.getMessage());
            }
        }
    }

    private void showAddTagDialog(Long parentId) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Tag");
        dialog.setHeaderText("Enter tag name:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                try {
                    tagService.createTag(name.trim(), parentId);
                    loadTags();
                } catch (IllegalArgumentException e) {
                    showError("Cannot create tag", e.getMessage());
                }
            }
        });
    }

    private void showRenameDialog(Tag tag) {
        TextInputDialog dialog = new TextInputDialog(tag.getName());
        dialog.setTitle("Rename Tag");
        dialog.setHeaderText("Enter new name for tag: " + tag.getName());
        dialog.setContentText("New name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty() && !newName.equals(tag.getName())) {
                try {
                    // Update the tag with new name, keeping the same parent
                    Long parentId = tag.getParent() != null ? tag.getParent().getId() : null;
                    tagService.updateTag(tag.getId(), newName.trim(), parentId);
                    loadTags();
                } catch (IllegalArgumentException e) {
                    showError("Cannot rename tag", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void deleteSelectedTag() {
        TreeItem<Tag> selected = tagTreeTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Tag");
            alert.setHeaderText("Delete " + selected.getValue().getName() + "?");
            alert.setContentText("This will also affect all child tags. Are you sure?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                tagService.deleteTag(selected.getValue().getId());
                loadTags();
            }
        }
    }

    private void loadTags() {
        List<Tag> rootTags = tagService.findRootTags();
        TreeItem<Tag> root = new TreeItem<>();
        root.setExpanded(true);
        
        for (Tag tag : rootTags) {
            root.getChildren().add(createTreeItem(tag));
        }
        
        tagTreeTable.setRoot(root);
        tagTreeTable.setShowRoot(false);
        
        // Restore selection if possible
        if (draggedItem != null) {
            selectTagById(draggedItem.getValue().getId());
            draggedItem = null;
        }
    }

    private TreeItem<Tag> createTreeItem(Tag tag) {
        TreeItem<Tag> item = new TreeItem<>(tag);
        for (Tag child : tag.getChildren()) {
            item.getChildren().add(createTreeItem(child));
        }
        item.setExpanded(true);
        return item;
    }

    private void selectTagById(Long tagId) {
        findTreeItemById(tagTreeTable.getRoot(), tagId)
            .ifPresent(item -> tagTreeTable.getSelectionModel().select(item));
    }

    private Optional<TreeItem<Tag>> findTreeItemById(TreeItem<Tag> root, Long tagId) {
        if (root == null) return Optional.empty();
        
        for (TreeItem<Tag> item : root.getChildren()) {
            if (item.getValue().getId().equals(tagId)) {
                return Optional.of(item);
            }
            Optional<TreeItem<Tag>> found = findTreeItemById(item, tagId);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    private void showTagDetails(Tag tag) {
        tagDetailsPane.setVisible(true);
        fullPathLabel.setText(tag.getFullPath());
        childrenCountLabel.setText(String.valueOf(tag.getChildren().size()));
        
        Set<Transaction> transactions = tag.getAllTransactions();
        transactionCountLabel.setText(String.valueOf(transactions.size()));
        
        BigDecimal totalAmount = transactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalAmountLabel.setText(String.format("€%.2f", totalAmount));
    }

    private void hideTagDetails() {
        tagDetailsPane.setVisible(false);
        fullPathLabel.setText("");
        childrenCountLabel.setText("");
        transactionCountLabel.setText("");
        totalAmountLabel.setText("");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 