package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service interface for managing categorization tags in the Family Budget App.
 * 
 * <p>This interface defines the contract for tag management operations, including
 * CRUD operations, hierarchical organization, and search functionality. It provides
 * comprehensive tag categorization capabilities for organizing financial data.</p>
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
public interface TagService {
    
    // ==================== CRUD OPERATIONS ====================
    
    /**
     * Creates a new tag with the specified name and optional parent.
     * 
     * @param name the tag name
     * @param parentId the parent tag ID, or null for root tags
     * @return the created tag
     * @throws IllegalArgumentException if name is null or empty, or if parent is not found
     * @throws RuntimeException if tag creation fails
     */
    Tag createTag(String name, Long parentId);
    
    /**
     * Finds a tag by its unique identifier.
     * 
     * @param id the tag ID
     * @return an Optional containing the tag if found, empty otherwise
     * @throws IllegalArgumentException if id is null
     */
    Optional<Tag> findById(Long id);
    
    /**
     * Updates an existing tag's name and parent.
     * 
     * @param id the tag ID to update
     * @param newName the new tag name
     * @param newParentId the new parent tag ID, or null to make it a root tag
     * @throws IllegalArgumentException if id or newName is null/empty, or if parent is not found
     * @throws RuntimeException if tag update fails or would create circular reference
     */
    void updateTag(Long id, String newName, Long newParentId);
    
    /**
     * Deletes a tag and handles its relationships.
     * 
     * @param id the tag ID to delete
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException if tag is not found or deletion fails
     */
    void deleteTag(Long id);
    
    // ==================== HIERARCHICAL OPERATIONS ====================
    
    /**
     * Finds all root tags (tags without parents).
     * 
     * @return a list of root tags, ordered by full path
     */
    List<Tag> findRootTags();
    
    /**
     * Finds all child tags of a specific parent.
     * 
     * @param parentId the parent tag ID
     * @return a list of child tags, ordered by full path
     * @throws IllegalArgumentException if parentId is null
     */
    List<Tag> findChildTags(Long parentId);
    
    /**
     * Gets all descendant tags of a specific tag (including children, grandchildren, etc.).
     * 
     * @param tagId the tag ID to get descendants for
     * @return a set of all descendant tags
     * @throws IllegalArgumentException if tagId is null
     * @throws RuntimeException if tag is not found
     */
    Set<Tag> getAllDescendants(Long tagId);
    
    /**
     * Finds a tag and all its descendants.
     * 
     * @param parent the parent tag
     * @return a list containing the parent tag and all its descendants
     * @throws IllegalArgumentException if parent is null
     */
    List<Tag> findTagAndDescendants(Tag parent);
    
    // ==================== SEARCH AND QUERY OPERATIONS ====================
    
    /**
     * Searches for tags by name or full path.
     * 
     * @param query the search query
     * @return a list of matching tags, ordered by full path
     * @throws IllegalArgumentException if query is null or empty
     */
    List<Tag> searchTags(String query);
    
    /**
     * Retrieves all tags in the system.
     * 
     * @return a list of all tags, ordered by full path
     */
    List<Tag> findAll();
}
