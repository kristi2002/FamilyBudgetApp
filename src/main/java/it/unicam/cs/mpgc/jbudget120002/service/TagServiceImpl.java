package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TagServiceImpl implements TagService {
    private final EntityManager entityManager;

    public TagServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Tag createTag(String name, Long parentId) {
        entityManager.getTransaction().begin();
        try {
            Tag tag = new Tag(name);
            if (parentId != null) {
                Tag parent = entityManager.find(Tag.class, parentId);
                if (parent != null) {
                    tag.setParent(parent);
                }
            }
            entityManager.persist(tag);
            entityManager.getTransaction().commit();
            return tag;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public Optional<Tag> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Tag.class, id));
    }

    @Override
    public List<Tag> findRootTags() {
        TypedQuery<Tag> query = entityManager.createQuery(
            "SELECT t FROM Tag t WHERE t.parent IS NULL", Tag.class);
        return query.getResultList();
    }

    @Override
    public List<Tag> findChildTags(Long parentId) {
        TypedQuery<Tag> query = entityManager.createQuery(
            "SELECT t FROM Tag t WHERE t.parent.id = :parentId", Tag.class);
        query.setParameter("parentId", parentId);
        return query.getResultList();
    }

    @Override
    public void updateTag(Long id, String newName, Long newParentId) {
        entityManager.getTransaction().begin();
        try {
            Tag tag = entityManager.find(Tag.class, id);
            if (tag != null) {
                tag.setName(newName);
                if (newParentId != null) {
                    Tag newParent = entityManager.find(Tag.class, newParentId);
                    if (newParent != null && !isCircularReference(tag, newParent)) {
                        tag.setParent(newParent);
                    }
                } else {
                    tag.setParent(null);
                }
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public void deleteTag(Long id) {
        entityManager.getTransaction().begin();
        try {
            Tag tag = entityManager.find(Tag.class, id);
            if (tag != null) {
                // Remove parent reference
                tag.setParent(null);
                
                // Set children's parent to null
                for (Tag child : tag.getChildren()) {
                    child.setParent(null);
                }
                
                entityManager.remove(tag);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public Set<Tag> getAllDescendants(Long tagId) {
        Tag tag = entityManager.find(Tag.class, tagId);
        if (tag == null) {
            return Set.of();
        }
        return getAllDescendantsRecursive(tag);
    }

    private Set<Tag> getAllDescendantsRecursive(Tag tag) {
        return tag.getChildren().stream()
            .flatMap(child -> {
                Set<Tag> descendants = getAllDescendantsRecursive(child);
                descendants.add(child);
                return descendants.stream();
            })
            .collect(Collectors.toSet());
    }

    private boolean isCircularReference(Tag tag, Tag newParent) {
        Tag current = newParent;
        while (current != null) {
            if (current.getId().equals(tag.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    @Override
    public List<Tag> searchTags(String query) {
        TypedQuery<Tag> searchQuery = entityManager.createQuery(
            "SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(:query)", Tag.class);
        searchQuery.setParameter("query", "%" + query + "%");
        return searchQuery.getResultList();
    }

    @Override
    public List<Tag> findAll() {
        TypedQuery<Tag> query = entityManager.createQuery(
            "SELECT t FROM Tag t", Tag.class);
        return query.getResultList();
    }
}
