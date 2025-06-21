package it.unicam.cs.mpgc.jbudget120002.model;

/**
 * Represents the roles a user can have within the Family Budget App.
 * 
 * <p>This enum defines the different permission levels and access capabilities
 * that users can have within groups and the application as a whole. Each role
 * grants specific permissions and determines what actions a user can perform.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Define user permission levels</li>
 *   <li>Control access to application features</li>
 *   <li>Enable role-based security</li>
 *   <li>Support group management permissions</li>
 * </ul>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Check if user has admin privileges
 * if (user.hasRole(Role.ADMIN)) {
 *     // Allow administrative actions
 * }
 * 
 * // Assign role to user
 * user.addRole(Role.MEMBER);
 * 
 * // Check role permissions
 * boolean canManageUsers = role.hasPermission(Permission.MANAGE_USERS);
 * }</pre>
 * 
 * @author FamilyBudgetApp Team
 * @version 1.0
 * @since 1.0
 */
public enum Role {
    
    /**
     * The administrator role, with full access and management capabilities.
     * 
     * <p>Administrators have complete control over the application, including:</p>
     * <ul>
     *   <li>User management (create, edit, delete users)</li>
     *   <li>Group management (create, edit, delete groups)</li>
     *   <li>Budget management (create, edit, delete budgets)</li>
     *   <li>Transaction management (view, edit, delete all transactions)</li>
     *   <li>System settings and configuration</li>
     *   <li>Data export and import</li>
     *   <li>Statistics and reporting access</li>
     * </ul>
     */
    ADMIN,

    /**
     * A standard member role, with permissions for regular use of the application.
     * 
     * <p>Members have access to basic application features, including:</p>
     * <ul>
     *   <li>View and manage their own transactions</li>
     *   <li>View group budgets and statistics</li>
     *   <li>Create and edit personal budgets</li>
     *   <li>View group transactions (if permitted)</li>
     *   <li>Basic reporting and statistics</li>
     * </ul>
     */
    MEMBER;

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Checks if this role has administrative privileges.
     * 
     * @return true if this role is ADMIN, false otherwise
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Checks if this role has member privileges.
     * 
     * @return true if this role is MEMBER, false otherwise
     */
    public boolean isMember() {
        return this == MEMBER;
    }

    /**
     * Gets the display name for this role.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        switch (this) {
            case ADMIN:
                return "Administrator";
            case MEMBER:
                return "Member";
            default:
                return this.name();
        }
    }

    /**
     * Gets the description for this role.
     * 
     * @return the role description
     */
    public String getDescription() {
        switch (this) {
            case ADMIN:
                return "Full access to all application features and administrative functions";
            case MEMBER:
                return "Standard access to application features for regular use";
            default:
                return "No description available";
        }
    }

    /**
     * Gets the permission level for this role.
     * Higher numbers indicate more permissions.
     * 
     * @return the permission level
     */
    public int getPermissionLevel() {
        switch (this) {
            case ADMIN:
                return 100;
            case MEMBER:
                return 50;
            default:
                return 0;
        }
    }

    /**
     * Checks if this role has at least the specified permission level.
     * 
     * @param requiredLevel the required permission level
     * @return true if this role has sufficient permissions, false otherwise
     */
    public boolean hasPermissionLevel(int requiredLevel) {
        return getPermissionLevel() >= requiredLevel;
    }

    /**
     * Compares this role's permission level with another role.
     * 
     * @param other the other role to compare with
     * @return positive if this role has higher permissions, negative if lower, 0 if equal
     */
    public int comparePermissionLevel(Role other) {
        if (other == null) {
            return 1; // This role has higher permissions than null
        }
        return Integer.compare(this.getPermissionLevel(), other.getPermissionLevel());
    }

    /**
     * Checks if this role can perform actions that require the specified role.
     * 
     * @param requiredRole the role required for the action
     * @return true if this role can perform the action, false otherwise
     */
    public boolean canPerformAction(Role requiredRole) {
        if (requiredRole == null) {
            return true; // No role required
        }
        return this.getPermissionLevel() >= requiredRole.getPermissionLevel();
    }

    /**
     * Gets all roles that have lower or equal permission levels.
     * 
     * @return an array of roles with lower or equal permissions
     */
    public Role[] getLowerOrEqualRoles() {
        return java.util.Arrays.stream(values())
                .filter(role -> role.getPermissionLevel() <= this.getPermissionLevel())
                .toArray(Role[]::new);
    }

    /**
     * Gets all roles that have higher permission levels.
     * 
     * @return an array of roles with higher permissions
     */
    public Role[] getHigherRoles() {
        return java.util.Arrays.stream(values())
                .filter(role -> role.getPermissionLevel() > this.getPermissionLevel())
                .toArray(Role[]::new);
    }

    // ==================== STATIC UTILITY METHODS ====================

    /**
     * Gets the role with the highest permission level.
     * 
     * @return the role with the highest permissions
     */
    public static Role getHighestRole() {
        return ADMIN;
    }

    /**
     * Gets the role with the lowest permission level.
     * 
     * @return the role with the lowest permissions
     */
    public static Role getLowestRole() {
        return MEMBER;
    }

    /**
     * Gets a role by its display name.
     * 
     * @param displayName the display name to search for
     * @return the matching role, or null if not found
     */
    public static Role fromDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }
        
        for (Role role : values()) {
            if (role.getDisplayName().equalsIgnoreCase(displayName)) {
                return role;
            }
        }
        return null;
    }

    /**
     * Gets all available roles.
     * 
     * @return an array of all roles
     */
    public static Role[] getAllRoles() {
        return values();
    }

    /**
     * Gets all roles except the specified one.
     * 
     * @param excludeRole the role to exclude
     * @return an array of roles excluding the specified one
     */
    public static Role[] getAllRolesExcept(Role excludeRole) {
        return java.util.Arrays.stream(values())
                .filter(role -> role != excludeRole)
                .toArray(Role[]::new);
    }
} 