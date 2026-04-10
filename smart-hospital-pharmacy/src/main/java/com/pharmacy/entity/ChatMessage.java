package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String senderName;

    @Column(nullable = false)
    private String senderEmail;

    @Column(nullable = false)
    private String senderRole; // ADMIN or STAFF

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    private MessageType messageType = MessageType.TEXT;

    private boolean isAnnouncement = false;

    private LocalDateTime sentAt;

    // Default constructor
    public ChatMessage() {}

    // Constructor with all fields
    public ChatMessage(Long id, String senderName, String senderEmail, String senderRole, String message, MessageType messageType, boolean isAnnouncement, LocalDateTime sentAt) {
        this.id = id;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.senderRole = senderRole;
        this.message = message;
        this.messageType = messageType;
        this.isAnnouncement = isAnnouncement;
        this.sentAt = sentAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    public boolean isAnnouncement() { return isAnnouncement; }
    public void setAnnouncement(boolean announcement) { isAnnouncement = announcement; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }

    public enum MessageType {
        TEXT, ALERT, SYSTEM, ANNOUNCEMENT
    }
}
