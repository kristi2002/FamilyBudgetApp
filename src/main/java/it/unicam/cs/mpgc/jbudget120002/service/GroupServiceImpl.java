package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Group;
import it.unicam.cs.mpgc.jbudget120002.model.User;
import it.unicam.cs.mpgc.jbudget120002.repository.GroupRepository;
import it.unicam.cs.mpgc.jbudget120002.repository.UserRepository;
import jakarta.persistence.EntityManager;

import java.util.Collections;
import java.util.List;

public class GroupServiceImpl extends BaseService implements GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupServiceImpl(EntityManager entityManager, GroupRepository groupRepository, UserRepository userRepository) {
        super(entityManager);
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Group findById(Long id) {
        return groupRepository.findById(id).orElse(null);
    }

    @Override
    public List<Group> findAll() {
        return groupRepository.findAll();
    }

    @Override
    public void save(Group group) {
        executeInTransaction(() -> {
            groupRepository.save(group);
            em.flush(); // Ensure the data is written to the database
        });
    }

    @Override
    public void delete(Group group) {
        executeInTransaction(() -> {
            groupRepository.delete(group);
            em.flush(); // Ensure the data is written to the database
        });
    }

    @Override
    public Group findGroupByName(String name) {
        return groupRepository.findByName(name).orElse(null);
    }

    @Override
    public void addUserToGroup(Long groupId, Long userId) {
        executeInTransaction(() -> {
            Group group = findById(groupId);
            User user = userRepository.findById(userId).orElse(null);
            if (group != null && user != null) {
                group.addUser(user);
                groupRepository.save(group);
                em.flush();
            }
        });
    }

    @Override
    public void removeUserFromGroup(Long groupId, Long userId) {
        executeInTransaction(() -> {
            Group group = findById(groupId);
            User user = userRepository.findById(userId).orElse(null);
            if (group != null && user != null) {
                group.removeUser(user);
                groupRepository.save(group);
                em.flush();
            }
        });
    }

    @Override
    public List<User> getUsersInGroup(Long groupId) {
        Group group = findById(groupId);
        if (group != null) {
            return userRepository.findByGroupId(groupId);
        }
        return Collections.emptyList();
    }
} 