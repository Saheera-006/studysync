package com.studysync.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Subject {
    private String id;
    private String name;
    private LocalDate examDate;
    private String color;
    private List<Topic> topics;

    private static final String[] COLORS = {
        "#7F77DD","#1D9E75","#D85A30","#378ADD",
        "#BA7517","#D4537E","#639922","#E24B4A"
    };
    private static int colorIndex = 0;

    public Subject() {}

    public Subject(String name, LocalDate examDate) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Subject name cannot be empty");
        this.id = UUID.randomUUID().toString();
        this.name = name.trim();
        this.examDate = examDate;
        this.topics = new ArrayList<>();
        this.color = COLORS[colorIndex % COLORS.length];
        colorIndex++;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public List<Topic> getTopics() { return topics; }
    public void setTopics(List<Topic> topics) { this.topics = topics; }

    public void addTopic(Topic topic) {
        if (topic == null || topic.getName() == null || topic.getName().trim().isEmpty())
            throw new IllegalArgumentException("Topic name cannot be empty");
        topics.add(topic);
    }

    public boolean removeTopic(String topicId) {
        return topics.removeIf(t -> t.getId().equals(topicId));
    }

    public long daysUntilExam() {
        if (examDate == null) return -1;
        return ChronoUnit.DAYS.between(LocalDate.now(), examDate);
    }

    public int getCompletionPercentage() {
        if (topics.isEmpty()) return 0;
        long done = topics.stream().filter(t -> t.getStatus() == TopicStatus.DONE).count();
        return (int) Math.round((done * 100.0) / topics.size());
    }
}
