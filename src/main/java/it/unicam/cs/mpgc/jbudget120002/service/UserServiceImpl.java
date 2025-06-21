package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Role;
import it.unicam.cs.mpgc.jbudget120002.model.User;
import it.unicam.cs.mpgc.jbudget120002.repository.UserRepository;
import jakarta.persistence.EntityManager;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class UserServiceImpl extends BaseService implements UserService {

    private final UserRepository repository;

    public UserServiceImpl(EntityManager entityManager, UserRepository repository) {
        super(entityManager);
        this.repository = repository;
    }

    @Override
    public User findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll();
    }

    @Override
    public void save(User user) {
        repository.save(user);
    }

    @Override
    public void saveUser(User user) {
        save(user);
    }

    @Override
    public void delete(User user) {
        repository.delete(user);
    }

    @Override
    public User findUserByName(String username) {
        return repository.findByUsername(username).orElse(null);
    }

    @Override
    public List<User> findUsersByGroup(Long groupId) {
        return repository.findByGroupId(groupId);
    }

    @Override
    public void assignRoleToUser(Long userId, Role role) {
        User user = findById(userId);
        if (user != null) {
            user.getRoles().add(role);
            save(user);
        }
    }

    @Override
    public void revokeRoleFromUser(Long userId, Role role) {
        User user = findById(userId);
        if (user != null) {
            user.getRoles().remove(role);
            save(user);
        }
    }

    @Override
    public Set<Role> getUserRoles(Long userId) {
        User user = findById(userId);
        if (user != null) {
            return user.getRoles();
        }
        return Collections.emptySet();
    }
} 