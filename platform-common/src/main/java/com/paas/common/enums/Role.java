package com.paas.common.enums;

public enum Role {
    OWNER,        // Full access to organization
    ADMIN,        // Can manage most settings
    DEVELOPER,    // Can deploy and manage applications
    VIEWER        // Read-only access
}
