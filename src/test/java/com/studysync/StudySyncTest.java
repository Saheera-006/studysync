package com.studysync;

import com.studysync.model.Subject;
import com.studysync.model.Topic;
import com.studysync.model.TopicStatus;
import com.studysync.service.PlanValidator;
import com.studysync.service.ProgressTracker;
import com.studysync.service.SubjectStore;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class StudySyncTest {

    private Subject maths;
    private Subject physics;

    @BeforeEach
    void setUp() {
        maths = new Subject("Maths", LocalDate.now().plusDays(10));
        maths.addTopic(new Topic("Calculus"));
        maths.addTopic(new Topic("Algebra"));

        physics = new Subject("Physics", LocalDate.now().plusDays(7));
        physics.addTopic(new Topic("Waves"));
    }

    @Test
    void testAddTopicToSubject() {
        assertEquals(2, maths.getTopics().size());
    }

    @Test
    void testAddEmptyTopicThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Topic(""));
    }

    @Test
    void testRemoveTopicById() {
        String id = maths.getTopics().get(0).getId();
        assertTrue(maths.removeTopic(id));
        assertEquals(1, maths.getTopics().size());
    }

    @Test
    void testTopicStatusCycle() {
        Topic t = new Topic("Test");
        assertEquals(TopicStatus.PENDING, t.getStatus());
        t.cycleStatus(); assertEquals(TopicStatus.TODAY, t.getStatus());
        t.cycleStatus(); assertEquals(TopicStatus.DONE, t.getStatus());
        t.cycleStatus(); assertEquals(TopicStatus.PENDING, t.getStatus());
    }

    @Test
    void testCompletionPercentage() {
        maths.getTopics().get(0).setStatus(TopicStatus.DONE);
        assertEquals(50, maths.getCompletionPercentage());
    }

    @Test
    void testCompletionPercentageNoTopics() {
        Subject empty = new Subject("Empty", LocalDate.now().plusDays(5));
        assertEquals(0, empty.getCompletionPercentage());
    }

    @Test
    void testDaysUntilExam() {
        assertTrue(maths.daysUntilExam() > 0);
    }

    @Test
    void testProgressTrackerOverall() {
        maths.getTopics().get(0).setStatus(TopicStatus.DONE);
        ProgressTracker tracker = new ProgressTracker();
        int pct = tracker.overallProgress(List.of(maths, physics));
        assertTrue(pct >= 0 && pct <= 100);
    }

    @Test
    void testProgressTrackerEmpty() {
        ProgressTracker tracker = new ProgressTracker();
        assertEquals(0, tracker.overallProgress(Collections.emptyList()));
    }

    @Test
    void testProgressTrackerAllDone() {
        maths.getTopics().forEach(t -> t.setStatus(TopicStatus.DONE));
        physics.getTopics().forEach(t -> t.setStatus(TopicStatus.DONE));
        ProgressTracker tracker = new ProgressTracker();
        assertEquals(100, tracker.overallProgress(List.of(maths, physics)));
    }

    @Test
    void testNextExamDays() {
        ProgressTracker tracker = new ProgressTracker();
        long days = tracker.nextExamDays(List.of(maths, physics));
        assertTrue(days >= 0);
    }

    @Test
    void testValidatorPassesGoodData() {
        PlanValidator validator = new PlanValidator();
        assertTrue(validator.isValid(List.of(maths, physics)));
    }

    @Test
    void testValidatorCatchesPastDate() {
        Subject past = new Subject("History", LocalDate.now().minusDays(1));
        past.addTopic(new Topic("WW2"));
        PlanValidator validator = new PlanValidator();
        List<String> errors = validator.validate(List.of(past));
        assertTrue(errors.stream().anyMatch(e -> e.contains("past")));
    }

    @Test
    void testValidatorCatchesEmptyList() {
        PlanValidator validator = new PlanValidator();
        assertFalse(validator.isValid(Collections.emptyList()));
    }

    @Test
    void testSubjectStoreAddAndFind() {
        SubjectStore store = new SubjectStore();
        store.add(maths);
        assertTrue(store.findById(maths.getId()).isPresent());
    }

    @Test
    void testSubjectStoreRemove() {
        SubjectStore store = new SubjectStore();
        store.add(maths);
        store.remove(maths.getId());
        assertFalse(store.findById(maths.getId()).isPresent());
    }

    @Test
    void testSubjectNameCannotBeEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> new Subject("", LocalDate.now().plusDays(5)));
    }
}
