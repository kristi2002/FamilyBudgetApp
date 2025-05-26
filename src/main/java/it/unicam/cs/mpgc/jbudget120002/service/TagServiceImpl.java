package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.*;
import java.util.stream.Collectors;

public class TagServiceImpl extends BaseService implements TagService {
    
    public TagServiceImpl(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public Tag createTag(String name, Long parentId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be empty");
        }

        entityManager.getTransaction().begin();
        try {
            Tag tag = new Tag(name.trim());
            
            if (parentId != null) {
                Tag parent = findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent tag not found"));
                tag.setParent(parent);
            }
            
            entityManager.persist(tag);
            entityManager.getTransaction().commit();
            return tag;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new IllegalArgumentException("Failed to create tag: " + e.getMessage());
        }
    }

    @Override
    public Optional<Tag> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(entityManager.find(Tag.class, id));
    }

    @Override
    public List<Tag> findRootTags() {
        TypedQuery<Tag> query = entityManager.createQuery(
            "SELECT t FROM Tag t WHERE t.parent IS NULL ORDER BY t.fullPath", Tag.class);
        return query.getResultList();
    }

    @Override
    public List<Tag> findChildTags(Long parentId) {
        if (parentId == null) {
            return Collections.emptyList();
        }
        
        TypedQuery<Tag> query = entityManager.createQuery(
            "SELECT t FROM Tag t WHERE t.parent.id = :parentId ORDER BY t.fullPath", Tag.class);
        query.setParameter("parentId", parentId);
        return query.getResultList();
    }

    @Override
    public void updateTag(Long id, String newName, Long newParentId) {
        if (id == null) {
            throw new IllegalArgumentException("Tag ID cannot be null");
        }
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be empty");
        }

        entityManager.getTransaction().begin();
        try {
            Tag tag = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));
            
            // Check if new parent would create a cycle
            if (newParentId != null) {
                Tag newParent = findById(newParentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent tag not found"));
                
                // Check for circular reference
                Tag current = newParent;
                while (current != null) {
                    if (current.getId().equals(id)) {
                        throw new IllegalArgumentException("Cannot create circular reference in tag hierarchy");
                    }
                    current = current.getParent();
                }
                
                tag.setParent(newParent);
            } else {
                tag.setParent(null);
            }
            
            tag.setName(newName.trim());
            entityManager.merge(tag);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new IllegalArgumentException("Failed to update tag: " + e.getMessage());
        }
    }

    @Override
    public void deleteTag(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Tag ID cannot be null");
        }

        entityManager.getTransaction().begin();
        try {
            Tag tag = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));
            
            // Remove this tag from all transactions
            tag.getTransactions().forEach(t -> t.getTags().remove(tag));
            
            // Update children's parent to null (this will update their fullPaths)
            tag.getChildren().forEach(child -> child.setParent(null));
            
            entityManager.remove(tag);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new IllegalArgumentException("Failed to delete tag: " + e.getMessage());
        }
    }

    @Override
    public Set<Tag> getAllDescendants(Long tagId) {
        if (tagId == null) {
            return Collections.emptySet();
        }
        
        Tag tag = findById(tagId)
            .orElseThrow(() -> new IllegalArgumentException("Tag not found"));
            
        Set<Tag> descendants = new HashSet<>();
        collectDescendants(tag, descendants);
        return descendants;
    }

    private void collectDescendants(Tag tag, Set<Tag> descendants) {
        for (Tag child : tag.getChildren()) {
            descendants.add(child);
            collectDescendants(child, descendants);
        }
    }

    @Override
    public List<Tag> searchTags(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        TypedQuery<Tag> searchQuery = entityManager.createQuery(
            "SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(:query) OR LOWER(t.fullPath) LIKE LOWER(:query) ORDER BY t.fullPath", Tag.class);
        searchQuery.setParameter("query", "%" + query.trim() + "%");
        return searchQuery.getResultList();
    }

    @Override
    public List<Tag> findAll() {
        return entityManager.createQuery("SELECT t FROM Tag t ORDER BY t.fullPath", Tag.class)
            .getResultList();
    }

    @Override
    public List<Tag> findTagAndDescendants(Tag parent) {
        List<Tag> result = new ArrayList<>();
        if (parent == null) {
            return result;
        }
        
        result.add(parent);
        if (parent.getChildren() != null) {
            for (Tag child : parent.getChildren()) {
                result.addAll(findTagAndDescendants(child));
            }
        }
        return result;
    }
} 