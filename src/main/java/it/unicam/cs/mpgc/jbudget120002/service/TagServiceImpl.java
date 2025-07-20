package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the TagService interface for managing categorization tags.
 * 
 * <p>This class provides comprehensive tag management functionality including
 * CRUD operations, hierarchical organization, and search capabilities. It ensures
 * data integrity by preventing circular references and maintaining proper tag
 * relationships.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Create, read, update, and delete tags</li>
 *   <li>Manage hierarchical tag relationships</li>
 *   <li>Prevent circular references in tag hierarchies</li>
 *   <li>Search and filter tags by various criteria</li>
 *   <li>Handle tag dependencies and cascading operations</li>
 *   <li>Support tag-based categorization of transactions and budgets</li>
 *   <li>Maintain tag integrity and relationships</li>
 * </ul>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Create a root tag
 * Tag food = tagService.createTag("Food", null);
 * 
 * // Create a child tag
 * Tag groceries = tagService.createTag("Groceries", food.getId());
 * 
 * // Find all root tags
 * List<Tag> rootTags = tagService.findRootTags();
 * 
 * // Search for tags
 * List<Tag> results = tagService.searchTags("food");
 * 
 * // Get all descendants of a tag
 * Set<Tag> descendants = tagService.getAllDescendants(food.getId());
 * }</pre>
 * 
 * @author FamilyBudgetApp Team
 * @version 1.0
 * @since 1.0
 */
public class TagServiceImpl extends BaseService implements TagService {
    
    // ==================== CONSTRUCTORS ====================

    /**
     * Creates a new TagServiceImpl with the required EntityManager.
     * 
     * @param entityManager the EntityManager for database operations
     * @throws IllegalArgumentException if entityManager is null
     */
    public TagServiceImpl(EntityManager entityManager) {
        super(entityManager);
        if (entityManager == null) {
            throw new IllegalArgumentException("EntityManager cannot be null");
        }
    }

    // ==================== CRUD OPERATIONS ====================

    @Override
    public Tag createTag(String name, Long parentId) {
        validateCreateTagParams(name);
        
        return executeInTransaction(() -> {
            Tag tag = new Tag(name.trim());
            
            if (parentId != null) {
                Tag parent = findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent tag not found"));
                tag.setParent(parent);
            }
            
            em.persist(tag);
            em.flush(); // Ensure the data is written to the database
            return tag;
        });
    }

    @Override
    public Optional<Tag> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(em.find(Tag.class, id));
    }

    @Override
    public List<Tag> findRootTags() {
        TypedQuery<Tag> query = em.createQuery(
            "SELECT t FROM Tag t WHERE t.parent IS NULL ORDER BY t.fullPath", Tag.class);
        return query.getResultList();
    }

    @Override
    public List<Tag> findChildTags(Long parentId) {
        if (parentId == null) {
            throw new IllegalArgumentException("Parent ID cannot be null");
        }
        
        TypedQuery<Tag> query = em.createQuery(
            "SELECT t FROM Tag t WHERE t.parent.id = :parentId ORDER BY t.fullPath", Tag.class);
        query.setParameter("parentId", parentId);
        return query.getResultList();
    }

    @Override
    public void updateTag(Long id, String newName, Long newParentId) {
        validateUpdateTagParams(id, newName);
        
        executeInTransaction(() -> {
            Tag tag = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));
            
            // Check if new parent would create a cycle
            if (newParentId != null) {
                Tag newParent = findById(newParentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent tag not found"));
                
                validateNoCircularReference(id, newParent);
                tag.setParent(newParent);
            } else {
                tag.setParent(null);
            }
            
            tag.setName(newName.trim());
            em.merge(tag);
            em.flush(); // Ensure the data is written to the database
        });
    }

    @Override
    public void deleteTag(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Tag ID cannot be null");
        }

        executeInTransaction(() -> {
            Tag tag = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));
            
            // Remove this tag from all transactions
            tag.getTransactions().forEach(t -> t.getTags().remove(tag));
            
            // Update children's parent to null (this will update their fullPaths)
            tag.getChildren().forEach(child -> child.setParent(null));
            
            em.remove(tag);
            em.flush(); // Ensure the data is written to the database
        });
    }

    @Override
    public Set<Tag> getAllDescendants(Long tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("Tag ID cannot be null");
        }
        
        Tag tag = findById(tagId)
            .orElseThrow(() -> new IllegalArgumentException("Tag not found"));
            
        Set<Tag> descendants = new HashSet<>();
        collectDescendants(tag, descendants);
        return descendants;
    }

    @Override
    public List<Tag> searchTags(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be null or empty");
        }
        
        TypedQuery<Tag> searchQuery = em.createQuery(
            "SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(:query) OR LOWER(t.fullPath) LIKE LOWER(:query) ORDER BY t.fullPath", Tag.class);
        searchQuery.setParameter("query", "%" + query.trim() + "%");
        return searchQuery.getResultList();
    }

    @Override
    public List<Tag> findAll() {
        return em.createQuery("SELECT t FROM Tag t ORDER BY t.fullPath", Tag.class)
            .getResultList();
    }

    @Override
    public List<Tag> findTagAndDescendants(Tag parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent tag cannot be null");
        }
        
        List<Tag> result = new ArrayList<>();
        result.add(parent);
        if (parent.getChildren() != null) {
            for (Tag child : parent.getChildren()) {
                result.addAll(findTagAndDescendants(child));
            }
        }
        return result;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validates parameters for creating a tag.
     * 
     * @param name the tag name
     * @throws IllegalArgumentException if name is invalid
     */
    private void validateCreateTagParams(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be null or empty");
        }
    }

    /**
     * Validates parameters for updating a tag.
     * 
     * @param id the tag ID
     * @param newName the new tag name
     * @throws IllegalArgumentException if parameters are invalid
     */
    private void validateUpdateTagParams(Long id, String newName) {
        if (id == null) {
            throw new IllegalArgumentException("Tag ID cannot be null");
        }
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be null or empty");
        }
    }

    /**
     * Validates that setting a new parent would not create a circular reference.
     * 
     * @param tagId the tag ID being updated
     * @param newParent the new parent tag
     * @throws IllegalArgumentException if circular reference would be created
     */
    private void validateNoCircularReference(Long tagId, Tag newParent) {
        Tag current = newParent;
        while (current != null) {
            if (current.getId().equals(tagId)) {
                throw new IllegalArgumentException("Cannot create circular reference in tag hierarchy");
            }
            current = current.getParent();
        }
    }

    /**
     * Recursively collects all descendant tags of a given tag.
     * 
     * @param tag the tag to collect descendants for
     * @param descendants the set to add descendants to
     */
    private void collectDescendants(Tag tag, Set<Tag> descendants) {
        for (Tag child : tag.getChildren()) {
            descendants.add(child);
            collectDescendants(child, descendants);
        }
    }
} 