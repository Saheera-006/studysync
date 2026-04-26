package com.studysync.service;

import com.studysync.model.Subject;
import com.studysync.model.TopicStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProgressTracker {

    public int overallProgress(List<Subject> subjects) {
        if (subjects == null || subjects.isEmpty()) return 0;
        long total = subjects.stream().mapToLong(s -> s.getTopics().size()).sum();
        if (total == 0) return 0;
        long done = subjects.stream()
                .flatMap(s -> s.getTopics().stream())
                .filter(t -> t.getStatus() == TopicStatus.DONE)
                .count();
        int pct = (int) Math.round((done * 100.0) / total);
        return Math.min(100, Math.max(0, pct));
    }

    public long nextExamDays(List<Subject> subjects) {
        return subjects.stream()
                .filter(s -> s.getExamDate() != null && s.daysUntilExam() >= 0)
                .mapToLong(Subject::daysUntilExam)
                .min()
                .orElse(-1);
    }
}
