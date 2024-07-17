package com.cst438.controller;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
 * example of unit test to add a section to an existing course
 */

@AutoConfigureMockMvc
@SpringBootTest
public class EnrollmentControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Test
    public void enrollIntoSection() throws Exception {
        MockHttpServletResponse response;

        int sectionNo = 12; // existing sectionNo
        int studentId = 3; // existing studentId

        EnrollmentDTO enrollmentdto = new EnrollmentDTO(
                0,
                null,
                studentId,
                "thomas edison",
                "tedison@csumb.edu",
                "cst363", "Introduction to Database", 1, sectionNo, "052","104","M W 10:00-11:50",4,2025, "Spring"
        );

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/"+ sectionNo + "?studentId=" + studentId)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(enrollmentdto)))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        EnrollmentDTO result = fromJsonString(response.getContentAsString(), EnrollmentDTO.class);

        assertNotNull(result);
        assertEquals(sectionNo, result.sectionNo());
        assertEquals(studentId, result.studentId());

        // Clean up after test
        Enrollment enrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);
        enrollmentRepository.delete(enrollment);
    }

    @Test
    public void enrollDuplicateCourse() throws Exception {
        MockHttpServletResponse response;

        int sectionNo = 1; // existing sectionNo
        int studentId = 1; // existing studentId already enrolled

        EnrollmentDTO enrollmentdto = new EnrollmentDTO(
                0,
                null,
                studentId,
                "thomas edison",
                "tedison@csumb.edu",
                "cst363", "Introduction to Database", 1, sectionNo, "052","104","M W 10:00-11:50",4,2025, "Spring"
        );

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/"+ sectionNo + "?studentId=" + studentId)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(enrollmentdto)))
                .andReturn()
                .getResponse();

        assertEquals(400, response.getStatus());
        String errorMessage = response.getErrorMessage();
        assertEquals("ERROR: The current date is not within the enrollment period; enrollment started 2023-05-01 and ended 2023-08-30.", errorMessage);
    }

    @Test
    public void enrollBadSectionNumber() throws Exception {
        MockHttpServletResponse response;

        int sectionNo = 9999; // non-existent sectionNo
        int studentId = 1; // existing studentId

        EnrollmentDTO enrollmentdto = new EnrollmentDTO(
                0,
                null,
                studentId,
                "thomas edison",
                "tedison@csumb.edu",
                "cst363", "Introduction to Database", 1, sectionNo, "052","104","M W 10:00-11:50",4,2025, "Spring"
        );

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/"+ sectionNo + "?studentId=" + studentId)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(enrollmentdto)))
                .andReturn()
                .getResponse();

        assertEquals(400, response.getStatus());
        String errorMessage = response.getErrorMessage();
        assertEquals("Section not found", errorMessage);
    }

    @Test
    public void enrollPastDeadline() throws Exception {
        MockHttpServletResponse response;

        int sectionNo = 2; // sectionNo with passed deadline
        int studentId = 1; // existing studentId

        EnrollmentDTO enrollmentdto = new EnrollmentDTO(
                0,
                null,
                studentId,
                "thomas edison",
                "tedison@csumb.edu",
                "cst363", "Introduction to Database", 1, sectionNo, "052","104","M W 10:00-11:50",4,2025, "Spring"
        );

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/"+ sectionNo + "?studentId=" + studentId)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(enrollmentdto)))
                .andReturn()
                .getResponse();

        assertEquals(400, response.getStatus());
        String errorMessage = response.getErrorMessage();
        assertEquals("ERROR: The current date is not within the enrollment period; enrollment started 2023-05-01 and ended 2023-08-30.", errorMessage);
    }

    @Test
    public void updateGrade() throws Exception {

        MockHttpServletResponse response;

        // issue a http get request to SpringTestServer
        response = mvc.perform(
                MockMvcRequestBuilders
                        .get("/sections/10/enrollments")
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        EnrollmentDTO[] result = fromJsonString(response.getContentAsString(), EnrollmentDTO[].class);
        EnrollmentDTO t = result[0];

        // primary key should have a non zero value from the database
        assertNotEquals(0, t.enrollmentId());
        // check other fields of the DTO for expected values
        assertEquals("cst438", t.courseId());

        // check the database
        Enrollment e = enrollmentRepository.findById(t.enrollmentId()).orElse(null);
        assertNotNull(e);
        assertEquals(3, e.getEnrollmentId());

        //assign new grade to enrollment and test http post
        String ogGrade = t.grade(); //save old grade to revert after test
        String newGrade = "C";
        EnrollmentDTO enrollment = new EnrollmentDTO(
                t.enrollmentId(),
                newGrade,
                t.studentId(),
                t.name(),
                t.email(),
                t.courseId(),t.title(),t.sectionId(),t.sectionNo(),t.building(),t.room(),t.times(),t.credits(),t.year(),t.semester()
        );
        List<EnrollmentDTO> dlist = new ArrayList<>();
        dlist.add(enrollment);

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/enrollments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(dlist)))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        //check database for updated grade in enrollment
        e = enrollmentRepository.findById(t.enrollmentId()).orElse(null);
        assertNotNull(e);
        assertEquals(3, e.getEnrollmentId());
        assertEquals("C", e.getGrade());

        //clean up by returning enrollment grade to original value
        enrollment = new EnrollmentDTO(
                t.enrollmentId(),
                ogGrade,
                t.studentId(),
                t.name(),
                t.email(),
                t.courseId(),t.title(),t.sectionId(),t.sectionNo(),t.building(),t.room(),t.times(),t.credits(),t.year(),t.semester()
        );
        dlist = new ArrayList<>();
        dlist.add(enrollment);
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/enrollments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(dlist)))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        //check database again for restored grade value
        e = enrollmentRepository.findById(t.enrollmentId()).orElse(null);
        assertNotNull(e);
        assertEquals(3, e.getEnrollmentId());
        assertEquals(ogGrade, e.getGrade());

    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
