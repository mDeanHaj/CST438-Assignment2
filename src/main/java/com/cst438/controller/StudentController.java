package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.security.Principal;

import static java.lang.Integer.parseInt;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    UserRepository userRepository;

   // student gets transcript showing list of all enrollments
   // studentId will be temporary until Login security is implemented
   //example URL  /transcript?studentId=19803
//   @GetMapping("/transcripts")
//   public List<EnrollmentDTO> getTranscript(@RequestParam("studentId") int studentId) {
//
//       User student = userRepository.findById(studentId).orElse(null);
//       if(student == null) {
//           throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("ERROR: Student with id %s not found.", studentId));
//       }

    @GetMapping("/transcripts")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<EnrollmentDTO> getTranscript(
            Principal principal) {

        int studentId = userRepository.findByEmail(principal.getName()).getId();

        User student = userRepository.findById(studentId).orElse(null);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user id not found");
        }

       List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentIdOrderByTermId(studentId);
       List<EnrollmentDTO> enrollmentDTOs = new ArrayList<>();

       for (Enrollment e : enrollments) {
           EnrollmentDTO dto = new EnrollmentDTO(
                   e.getEnrollmentId(),
                   e.getGrade(),
                   e.getUser().getId(),
                   e.getUser().getName(),
                   e.getUser().getEmail(),
                   e.getSection().getCourse().getCourseId(),
                   e.getSection().getCourse().getTitle(),
                   e.getSection().getSectionNo(),
                   e.getSection().getSecId(),
                   e.getSection().getBuilding(),
                   e.getSection().getRoom(),
                   e.getSection().getTimes(),
                   e.getSection().getCourse().getCredits(),
                   e.getSection().getTerm().getYear(),
                   e.getSection().getTerm().getSemester()
           );
           enrollmentDTOs.add(dto);
       }

       return enrollmentDTOs;
   }

    // student gets a list of their enrollments for the given year, semester
    // user must be student
    // studentId will be temporary until Login security is implemented
   @GetMapping("/enrollments")
   @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
   public List<EnrollmentDTO> getSchedule(
           @RequestParam("year") int year,
           @RequestParam("semester") String semester,
           Principal principal) {

        String studentId = principal.getName();
        User student = userRepository.findByEmail(studentId);
       if(student == null) {
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("ERROR: Student with id %s not found.", studentId));
       }

       if (year < 2000 || year > 2100 || !(semester.equalsIgnoreCase("Spring") || semester.equalsIgnoreCase("Summer") || semester.equalsIgnoreCase("Fall") || semester.equalsIgnoreCase("Winter"))) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid year or semester");
       }

       List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(year, semester, student.getId());
       List<EnrollmentDTO> enrollmentDTOs = new ArrayList<>();

       for (Enrollment e : enrollments) {
           EnrollmentDTO dto = new EnrollmentDTO(
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
           );
           enrollmentDTOs.add(dto);
       }
       return enrollmentDTOs;
   }


    // student adds enrollment into a section
    // user must be student
    // return EnrollmentDTO with enrollmentId generated by database
    @PostMapping("/enrollments/sections/{sectionNo}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public EnrollmentDTO addCourse(
		    @PathVariable int sectionNo,
            @RequestParam("studentId") int studentId,
            Principal principal) {

        User student = userRepository.findById(studentId).orElse(null);
        if(student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("ERROR: Student with id %s not found.", studentId));
        }

        Optional<Section> sectionOptional = sectionRepository.findById(sectionNo);
        if (sectionOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Section not found");
        }

        Section section = sectionOptional.get();
        Date currentDate = new Date();
        if (currentDate.before(section.getTerm().getAddDate()) || currentDate.after(section.getTerm().getAddDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR: The current date is not within the enrollment period; enrollment started " + section.getTerm().getAddDate() + " and ended " + section.getTerm().getAddDeadline() + ".");
        }

        Enrollment existingEnrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);
        if (existingEnrollment != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student is already enrolled in this section");
        }

        Enrollment newEnrollment = new Enrollment();
        newEnrollment.setUser(student);
        newEnrollment.setSection(section);
        enrollmentRepository.save(newEnrollment);

        return new EnrollmentDTO(
                newEnrollment.getEnrollmentId(),
                newEnrollment.getGrade(),
                newEnrollment.getUser().getId(),
                newEnrollment.getUser().getName(),
                newEnrollment.getUser().getEmail(),
                newEnrollment.getSection().getCourse().getCourseId(),
                newEnrollment.getSection().getCourse().getTitle(),
                newEnrollment.getSection().getSecId(),
                newEnrollment.getSection().getSectionNo(),
                newEnrollment.getSection().getBuilding(),
                newEnrollment.getSection().getRoom(),
                newEnrollment.getSection().getTimes(),
                newEnrollment.getSection().getCourse().getCredits(),
                newEnrollment.getSection().getTerm().getYear(),
                newEnrollment.getSection().getTerm().getSemester()
        );
    }

    // student drops a course
    // user must be student
   @DeleteMapping("/enrollments/{enrollmentId}")
   @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
   public void dropCourse(@PathVariable("enrollmentId") int enrollmentId) {

       Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(() ->
               new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enrollment not found"));

       Date currentDate = new Date();
       if (currentDate.after(enrollment.getSection().getTerm().getDropDeadline())) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current date is past the drop deadline");
       }

       enrollmentRepository.delete(enrollment);
   }
}
