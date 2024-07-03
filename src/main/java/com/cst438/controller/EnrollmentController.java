package com.cst438.controller;


import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EnrollmentController {

    @Autowired
    EnrollmentRepository enrollmentRepository;
    @Autowired
    private SectionRepository sectionRepository;

    // instructor downloads student enrollments for a section, ordered by student name
    // user must be instructor for the section
    @GetMapping("/sections/{sectionNo}/enrollments")
    public List<EnrollmentDTO> getEnrollments(
            @PathVariable("sectionNo") int sectionNo ) {

        Optional<Section> section = sectionRepository.findById(sectionNo);
        if (section.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, sectionNo + " is not found.");
        }

        List<Enrollment> enrollments= enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(sectionNo);
        List<EnrollmentDTO> dto_list = new ArrayList<>();

        for(Enrollment e : enrollments) {
            dto_list.add(new EnrollmentDTO(
                    e.getEnrollmentId(),
                    e.getGrade(),
                    e.getUser().getId(),
                    e.getUser().getName(),
                    e.getUser().getEmail(),
                    e.getSection().getCourse().getCourseId(),
                    e.getSection().getCourse().getTitle(),
                    e.getSection().getSecId(),
                    e.getSection().getSectionNo(),
                    e.getSection().getBuilding(),
                    e.getSection().getRoom(),
                    e.getSection().getTimes(),
                    e.getSection().getCourse().getCredits(),
                    e.getSection().getTerm().getYear(),
                    e.getSection().getTerm().getSemester()
            ));
        }

        return dto_list;
    }

    // instructor uploads enrollments with the final grades for the section
    // user must be instructor for the section
    @PutMapping("/enrollments")
    public void updateEnrollmentGrade(@RequestBody List<EnrollmentDTO> dlist) {

        // For each EnrollmentDTO in the list
        //  find the Enrollment entity using enrollmentId
        //  update the grade and save back to database
        for(EnrollmentDTO d : dlist) {
            Enrollment e = enrollmentRepository.findById(d.enrollmentId()).orElse(null);
            if(e == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "enrollment not found " +d.enrollmentId() );
            }
            e.setGrade(d.grade());

            enrollmentRepository.save(e);
        }

    }

}
