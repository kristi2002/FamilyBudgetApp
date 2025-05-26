package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagService {
    Tag createTag(String name, Long parentId);
    
    Optional<Tag> findById(Long id);
    
    List<Tag> findRootTags();
    
    List<Tag> findChildTags(Long parentId);
    
    void updateTag(Long id, String newName, Long newParentId);
    
    void deleteTag(Long id);
    
    Set<Tag> getAllDescendants(Long tagId);
    
    List<Tag> searchTags(String query);

    List<Tag> findAll();
}
