package com.cst438.domain;

import jakarta.persistence.*;

@Entity
public class Grade {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="grade_id")
    private int gradeId;

    // add additional attribute for score
    // add relationship between grade and assignment entities
    // add relationship between grade and enrollment entities
    // add getter/setter methods
    private Integer score;
    @ManyToOne
    @JoinColumn(name="assignment_id", nullable=false)
    private Assignment assignment;
    @ManyToOne
    @JoinColumn(name="enrollment_id", nullable=false)
    private Enrollment enrollment;

    public int getGradeId() {
        return gradeId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

}
