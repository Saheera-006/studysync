package com.studysync.service;

import com.studysync.model.Subject;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlanValidator {

    public List<String> validate(List<Subject> subjects) {
        List<String> errors = new ArrayList<>();
        if (subjects == null || subjects.isEmpty()) {
            errors.add("No subjects added yet.");
            return errors;
        }
        LocalDate today = LocalDate.now();
        for (Subject s : subjects) {
            if (s.getName() == null || s.getName().trim().isEmpty())
                errors.add("A subject has an empty name.");
            if (s.getExamDate() != null && s.getExamDate().isBefore(today))
                errors.add("Exam date for '" + s.getName() + "' is in the past.");
        }
        return errors;
    }

    public boolean isValid(List<Subject> subjects) {
        return validate(subjects).isEmpty();
    }
}
