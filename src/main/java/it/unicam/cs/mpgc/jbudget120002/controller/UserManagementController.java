package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.Group;
import it.unicam.cs.mpgc.jbudget120002.model.Role;
import it.unicam.cs.mpgc.jbudget120002.model.User;
import it.unicam.cs.mpgc.jbudget120002.service.GroupService;
import it.unicam.cs.mpgc.jbudget120002.service.UserService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;

public class UserManagementController extends BaseController {

    @FXML private TreeView<Group> groupTreeView;
    @FXML private TextField groupNameField;
    @FXML private TextArea groupDescriptionArea;
    @FXML private ComboBox<Group> parentGroupComboBox;
    @FXML private ListView<User> userListView;
    @FXML private TextField userNameField;
    @FXML private TextField userSurnameField;
    @FXML private Label selectedUserNameLabel;
    @FXML private Label selectedUserSurnameLabel;
    @FXML private ComboBox<Group> groupAssignmentComboBox;
    @FXML private CheckBox adminRoleCheckBox;
    @FXML private CheckBox memberRoleCheckBox;
    @FXML
    private TableView<User> userTable;

    private UserService userService;
    private GroupService groupService;
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @Override
    public void initializeServices() {
        this.userService = serviceFactory.getUserService(false);
        this.groupService = serviceFactory.getGroupService(false);
    }

    @Override
    protected void setupUI() {
        setupUserListView();
        userListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        populateUserDetails(newSelection);
                    } else {
                        clearUserDetails();
                    }
                });
        groupTreeView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null && newSelection.getValue() != null) {
                        populateGroupDetails(newSelection.getValue());
                    } else {
                        clearGroupDetails();
                    }
                });
    }

    @Override
    protected void loadData() {
        List<Group> groups = groupService.findAll();
        loadGroupTreeView(groups);
        userListView.setItems(FXCollections.observableArrayList(userService.findAll()));
        groupAssignmentComboBox.setItems(FXCollections.observableArrayList(groups));
        parentGroupComboBox.setItems(FXCollections.observableArrayList(groups));
        clearUserDetails();
    }

    private void loadGroupTreeView(List<Group> groups) {
        Group rootGroup = new Group("All Groups");
        rootGroup.setId(0L); // A virtual root
        TreeItem<Group> rootItem = new TreeItem<>(rootGroup);
        rootItem.setExpanded(true);

        Map<Long, TreeItem<Group>> treeItemMap = new HashMap<>();
        treeItemMap.put(0L, rootItem);

        for (Group group : groups) {
            treeItemMap.put(group.getId(), new TreeItem<>(group));
        }

        for (Group group : groups) {
            TreeItem<Group> parentItem = (group.getParent() != null)
                    ? treeItemMap.get(group.getParent().getId())
                    : rootItem;
            parentItem.getChildren().add(treeItemMap.get(group.getId()));
        }

        groupTreeView.setRoot(rootItem);
        groupTreeView.setShowRoot(false);
        
        // Set custom cell factory to display only group names
        groupTreeView.setCellFactory(param -> new TreeCell<Group>() {
            @Override
            protected void updateItem(Group item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
    }

    private void setupUserListView() {
        userListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName() + " " + (item.getSurname() != null ? item.getSurname() : ""));
            }
        });

        groupAssignmentComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Group item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });

        groupAssignmentComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Group item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
    }

    private void populateUserDetails(User user) {
        if (user != null) {
            selectedUserNameLabel.setText(user.getName());
            selectedUserSurnameLabel.setText(user.getSurname());
            // Temp fix: Show the first group. A better UI is needed for multiple groups.
            groupAssignmentComboBox.setValue(user.getGroups().stream().findFirst().orElse(null));
            adminRoleCheckBox.setSelected(user.getRoles().contains(Role.ADMIN));
            memberRoleCheckBox.setSelected(user.getRoles().contains(Role.MEMBER));
        } else {
            clearUserDetails();
        }
    }
    
    private void populateGroupDetails(Group group) {
        if (group != null && group.getId() != 0L) {
            groupNameField.setText(group.getName());
            groupDescriptionArea.setText(group.getDescription());
            parentGroupComboBox.setValue(group.getParent());
        } else {
            clearGroupDetails();
        }
    }

    private void clearUserDetails() {
        selectedUserNameLabel.setText("");
        selectedUserSurnameLabel.setText("");
        groupAssignmentComboBox.setValue(null);
        adminRoleCheckBox.setSelected(false);
        memberRoleCheckBox.setSelected(false);
    }
    
    private void clearGroupDetails() {
        groupNameField.clear();
        groupDescriptionArea.clear();
        parentGroupComboBox.setValue(null);
    }

    @FXML
    void handleAddGroup() {
        String groupName = groupNameField.getText();
        if (groupName != null && !groupName.trim().isEmpty()) {
            // Check if group with this name already exists
            Group existingGroup = groupService.findGroupByName(groupName.trim());
            
            if (existingGroup != null) {
                showError("Group Creation Error", "A group with the name '" + groupName + "' already exists!");
                return;
            }
            
            try {
                Group newGroup = new Group(groupName.trim());
                newGroup.setDescription(groupDescriptionArea.getText());
                newGroup.setParent(parentGroupComboBox.getValue());
                groupService.save(newGroup);
                loadData();
                clearGroupDetails();
                showInfo("Success", "Group '" + groupName + "' created successfully!");
            } catch (Exception e) {
                showError("Group Creation Error", "Failed to create group: " + e.getMessage());
            }
        } else {
            showError("Invalid Input", "Please enter a group name.");
        }
    }

    @FXML
    void handleDeleteGroup() {
        TreeItem<Group> selectedItem = groupTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Group selectedGroup = selectedItem.getValue();
            groupService.delete(selectedGroup);
            loadData();
        }
    }

    @FXML
    void handleAddUser() {
        String name = userNameField.getText();
        String surname = userSurnameField.getText();
        if (name != null && !name.isEmpty()) {
            // Create a proper User with all required fields
            String fullName = name + (surname != null && !surname.isEmpty() ? " " + surname : "");
            String username = name.toLowerCase() + (surname != null && !surname.isEmpty() ? surname.toLowerCase() : "");
            String email = username + "@example.com"; // Default email
            String password = "password123"; // Default password
            
            User newUser = new User(username, password, email, fullName);
            userService.save(newUser);
            loadData();
            userNameField.clear();
            userSurnameField.clear();
        }
    }

    @FXML
    void handleDeleteUser() {
        User selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            userService.delete(selectedUser);
            loadData();
        }
    }

    @FXML
    void handleUpdateUser() {
        User selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // Temp fix: This will overwrite all groups with a single selection.
            Group selectedGroup = groupAssignmentComboBox.getValue();
            selectedUser.getGroups().clear();
            if (selectedGroup != null) {
                selectedUser.getGroups().add(selectedGroup);
            }

            Set<Role> roles = new HashSet<>();
            if (adminRoleCheckBox.isSelected()) {
                roles.add(Role.ADMIN);
            }
            if (memberRoleCheckBox.isSelected()) {
                roles.add(Role.MEMBER);
            }
            selectedUser.setRoles(roles);
            userService.save(selectedUser);
            loadData();
        }
    }
} 