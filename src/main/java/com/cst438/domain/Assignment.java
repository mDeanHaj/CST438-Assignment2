package com.cst438.domain;

import jakarta.persistence.*;

import java.sql.Date;

@Entity
public class Assignment {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="assignment_id")
    private int assignmentId;

    // add additional attributes for title, dueDate
    // add relationship between assignment and section entities
    // add getter and setter methods
    private String title;
    @Column(name="due_date")
    private Date dueDate;
    @ManyToOne
    @JoinColumn(name="section_no", nullable=false)
    private Section section;

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }
}
