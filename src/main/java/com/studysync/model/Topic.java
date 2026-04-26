package com.studysync.model;

import java.util.UUID;

public class Topic {
    private String id;
    private String name;
    private TopicStatus status;

    public Topic() {}

    public Topic(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Topic name cannot be empty");
        this.id = UUID.randomUUID().toString();
        this.name = name.trim();
        this.status = TopicStatus.PENDING;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TopicStatus getStatus() { return status; }
    public void setStatus(TopicStatus status) { this.status = status; }

    public void cycleStatus() {
        switch (status) {
            case PENDING: status = TopicStatus.TODAY; break;
            case TODAY:   status = TopicStatus.DONE;  break;
            case DONE:    status = TopicStatus.PENDING; break;
        }
    }
}
