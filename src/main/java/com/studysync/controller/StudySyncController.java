package com.studysync.controller;

import com.studysync.model.Subject;
import com.studysync.model.Topic;
import com.studysync.model.TopicStatus;
import com.studysync.service.ProgressTracker;
import com.studysync.service.SubjectStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class StudySyncController {

    @Autowired
    private SubjectStore store;

    @Autowired
    private ProgressTracker tracker;

    // ── GET all subjects ──────────────────────────────────────────
    @GetMapping("/subjects")
    public List<Subject> getSubjects() {
        return store.getAll();
    }

    // ── GET dashboard stats ───────────────────────────────────────
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        List<Subject> subjects = store.getAll();
        Map<String, Object> stats = new HashMap<>();
        stats.put("subjectCount", subjects.size());
        stats.put("overallProgress", tracker.overallProgress(subjects));
        stats.put("nextExamDays", tracker.nextExamDays(subjects));
        return stats;
    }

    // ── ADD subject ───────────────────────────────────────────────
    @PostMapping("/subjects")
    public ResponseEntity<?> addSubject(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String dateStr = body.get("examDate");
        if (name == null || name.trim().isEmpty())
            return ResponseEntity.badRequest().body("Subject name is required");
        LocalDate date = (dateStr != null && !dateStr.isEmpty()) ? LocalDate.parse(dateStr) : null;
        Subject subject = new Subject(name.trim(), date);
        store.add(subject);
        return ResponseEntity.ok(subject);
    }

    // ── DELETE subject ────────────────────────────────────────────
    @DeleteMapping("/subjects/{subjectId}")
    public ResponseEntity<?> removeSubject(@PathVariable String subjectId) {
        boolean removed = store.remove(subjectId);
        if (!removed) return ResponseEntity.notFound().build();
        return ResponseEntity.ok("Deleted");
    }

    // ── ADD topic to subject ──────────────────────────────────────
    @PostMapping("/subjects/{subjectId}/topics")
    public ResponseEntity<?> addTopic(@PathVariable String subjectId,
                                      @RequestBody Map<String, String> body) {
        Optional<Subject> opt = store.findById(subjectId);
        if (!opt.isPresent()) return ResponseEntity.notFound().build();
        String topicName = body.get("name");
        if (topicName == null || topicName.trim().isEmpty())
            return ResponseEntity.badRequest().body("Topic name is required");
        Topic topic = new Topic(topicName.trim());
        opt.get().addTopic(topic);
        return ResponseEntity.ok(topic);
    }

    // ── DELETE topic ──────────────────────────────────────────────
    @DeleteMapping("/subjects/{subjectId}/topics/{topicId}")
    public ResponseEntity<?> removeTopic(@PathVariable String subjectId,
                                         @PathVariable String topicId) {
        Optional<Subject> opt = store.findById(subjectId);
        if (!opt.isPresent()) return ResponseEntity.notFound().build();
        opt.get().removeTopic(topicId);
        return ResponseEntity.ok("Deleted");
    }

    // ── CYCLE topic status ────────────────────────────────────────
    @PatchMapping("/subjects/{subjectId}/topics/{topicId}/cycle")
    public ResponseEntity<?> cycleTopicStatus(@PathVariable String subjectId,
                                              @PathVariable String topicId) {
        Optional<Subject> opt = store.findById(subjectId);
        if (!opt.isPresent()) return ResponseEntity.notFound().build();
        opt.get().getTopics().stream()
                .filter(t -> t.getId().equals(topicId))
                .findFirst()
                .ifPresent(Topic::cycleStatus);
        return ResponseEntity.ok("Updated");
    }
}
