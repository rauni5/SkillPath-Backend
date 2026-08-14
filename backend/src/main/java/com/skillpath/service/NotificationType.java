package com.skillpath.service;

public enum NotificationType {
    /** The owner invited you to a project. */
    INVITE_RECEIVED("PROJECT_INVITE_RECEIVED"),
    
    /** You accepted an invite you received. */
    INVITE_ACCEPTED("PROJECT_INVITE_ACCEPTED"),
    
    /** You declined an invite you received. */
    INVITE_REJECTED("PROJECT_INVITE_REJECTED"),
    
    /** Someone asked to join your project. */
    JOIN_REQUEST_RECEIVED("PROJECT_JOIN_REQUEST_RECEIVED"),
    
    /** The owner accepted your join request. */
    JOIN_REQUEST_ACCEPTED("PROJECT_JOIN_REQUEST_ACCEPTED"),
    
    /** The owner declined your join request. */
    JOIN_REQUEST_REJECTED("PROJECT_JOIN_REQUEST_REJECTED"),

    /** Someone commented on your discussion post. */
    DISCUSSION_COMMENT_RECEIVED("PROJECT_DISCUSSION_COMMENT_RECEIVED");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
