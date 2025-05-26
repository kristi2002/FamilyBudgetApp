package it.unicam.cs.mpgc.jbudget120002.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Mixin class to configure Jackson serialization for JPA entities.
 * This helps prevent infinite recursion with bidirectional relationships
 * and ensures proper handling of entity references.
 */
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
public abstract class JpaEntityMixin {
    // This is a mixin class, no implementation needed
} 