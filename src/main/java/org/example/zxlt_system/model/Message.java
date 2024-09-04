package org.example.zxlt_system.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String receiver;
    private String content;
    private long timestamp;


    public void setSender(String userId) {
        this.sender = userId;
    }

    public void setReceiver(String s) {
        this.receiver = s;
    }

    public void setContent(String payload) {
        this.content = payload;
    }

    public void setTimestamp(long l) {
        this.timestamp = l;
    }

    // Getters and Setters
}
