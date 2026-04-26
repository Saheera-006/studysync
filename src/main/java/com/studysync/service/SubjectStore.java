package com.studysync.service;

import com.studysync.model.Subject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class SubjectStore {
    private final List<Subject> subjects = new ArrayList<>();

    public List<Subject> getAll() { return subjects; }

    public void add(Subject subject) { subjects.add(subject); }

    public Optional<Subject> findById(String id) {
        return subjects.stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public boolean remove(String id) {
        return subjects.removeIf(s -> s.getId().equals(id));
    }
}
