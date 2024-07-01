package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.CourseDTO;
import com.cst438.dto.GradeDTO;
import org.springframework.expression.spel.ast.Assign;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AssignmentController {

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    GradeRepository gradeRepository;

    // instructor lists assignments for a section.  Assignments ordered by due date.
    // logged in user must be the instructor for the section
    // Haris
    @GetMapping("/sections/{secNo}/assignments")
    public List<AssignmentDTO> getAssignments(
            @PathVariable("secNo") int secNo) {
        List<Assignment> assignmentList = assignmentRepository.findBySectionNoOrderByDueDate(secNo);
        List<AssignmentDTO> assignmentDTOList = new ArrayList<>();

        for (Assignment assignment : assignmentList) {
            assignmentDTOList.add(new AssignmentDTO(
                    assignment.getAssignmentId(),
                    assignment.getTitle(),
                    assignment.getDueDate(),
                    assignment.getSection().getCourse().getCourseId(),
                    assignment.getSection().getSecId(),
                    assignment.getSection().getSectionNo()
                    ));
        }

        return assignmentDTOList;
    }

    // add assignment
    // user must be instructor of the section
    // return AssignmentDTO with assignmentID generated by database
    // Haris
    @PostMapping("/assignments")
    public AssignmentDTO createAssignment(
            @RequestBody AssignmentDTO dto) {
        Section section = sectionRepository.findById(dto.secNo()).orElse(null);
        if (section == null ){
            throw new ResponseStatusException( HttpStatus.NOT_FOUND, "ERROR: Section " + dto.secNo() + " not found.");
        } //if

        Assignment a = new Assignment();
        a.setTitle(dto.title());
        a.setDueDate(dto.dueDate());
        a.setSection(section);
        assignmentRepository.save(a);

        return new AssignmentDTO(
                a.getAssignmentId(),
                a.getTitle(),
                a.getDueDate(),
                a.getSection().getCourse().getCourseId(),
                a.getSection().getSecId(),
                a.getSection().getSectionNo()
        );
    }

    // update assignment for a section.  Only title and dueDate may be changed.
    // user must be instructor of the section
    // return updated AssignmentDTO
    // Haris
    @PutMapping("/assignments")
    public AssignmentDTO updateAssignment(@RequestBody AssignmentDTO dto) {

        // TODO remove the following line when done
        Assignment a = assignmentRepository.findById(dto.id()).orElse(null);
        if (a == null){
            throw new ResponseStatusException( HttpStatus.NOT_FOUND, "ERROR: Assignment " + dto.title() + " not found.");
        }

        a.setTitle(dto.title());
        a.setDueDate(dto.dueDate());
        assignmentRepository.save(a);

        return new AssignmentDTO(
                a.getAssignmentId(),
                a.getTitle(),
                a.getDueDate(),
                a.getSection().getCourse().getCourseId(),
                a.getSection().getSecId(),
                a.getSection().getSectionNo()
        );
    }

    // delete assignment for a section
    // logged in user must be instructor of the section
    // Haris
    @DeleteMapping("/assignments/{assignmentId}")
    public void deleteAssignment(@PathVariable("assignmentId") int assignmentId) {
        Assignment a = assignmentRepository.findById(assignmentId).orElse(null);
        // if course does not exist, do nothing.
        if (a != null) {
            assignmentRepository.delete(a);
        }
    }

    // instructor gets grades for assignment ordered by student name
    // user must be instructor for the section
    // David
    @GetMapping("/assignments/{assignmentId}/grades")
    public List<GradeDTO> getAssignmentGrades(@PathVariable("assignmentId") int assignmentId) {


        // get the list of enrollments for the section related to this assignment.
		// hint: use te enrollment repository method findEnrollmentsBySectionOrderByStudentName
        Assignment a = assignmentRepository.findById(assignmentId).orElse(null);
        if(a == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "assignment not found" + assignmentId);
        }
        int secNo = a.getSection().getSectionNo();
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(secNo);
        // for each enrollment, get the grade related to the assignment and enrollment
		//   hint: use the gradeRepository findByEnrollmentIdAndAssignmentId method.
        //   if the grade does not exist, create a grade entity and set the score to NULL
        //   and then save the new entity
        List<GradeDTO> gradeDTOs = new ArrayList<>();
        for (Enrollment e : enrollments) {
            Grade g = gradeRepository.findByEnrollmentIdAndAssignmentId(e.getEnrollmentId(), assignmentId);
            gradeDTOs.add(new GradeDTO(
                    g.getGradeId(),
                    g.getEnrollment().getUser().getName(),
                    g.getEnrollment().getUser().getEmail(),
                    g.getAssignment().getTitle(),
                    g.getAssignment().getSection().getCourse().getCourseId(),
                    g.getAssignment().getSection().getSecId(),
                    (g.getScore() != null) ? g.getScore() : null
            ));
        }

        return gradeDTOs;
    }

    // instructor uploads grades for assignment
    // user must be instructor for the section
    // David
    @PutMapping("/grades")
    public void updateGrades(@RequestBody List<GradeDTO> dlist) {

        // for each grade in the GradeDTO list, retrieve the grade entity
        // update the score and save the entity
        for (GradeDTO g : dlist) {
            Grade grade = gradeRepository.findById(g.gradeId()).orElse(null);
            if(grade == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "grade entity not found" +g.gradeId());
            }
            grade.setScore(g.score());
            gradeRepository.save(grade);
        }

    }



    // student lists their assignments/grades for an enrollment ordered by due date
    // student must be enrolled in the section
    // Dean
    @GetMapping("/assignments")
    public List<AssignmentStudentDTO> getStudentAssignments(
            @RequestParam("studentId") int studentId,
            @RequestParam("year") int year,
            @RequestParam("semester") String semester) {


        // return a list of assignments and (if they exist) the assignment grade
        //  for all sections that the student is enrolled for the given year and semester
		//  hint: use the assignment repository method findByStudentIdAndYearAndSemesterOrderByDueDate
        List<AssignmentStudentDTO> dto_list = new ArrayList<>();


        return null;
    }
}
