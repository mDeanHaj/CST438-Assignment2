package com.cst438.controller;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.GradeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;

/*
 * example of unit test to add a section to an existing course
 */

@AutoConfigureMockMvc
@SpringBootTest
public class AssignmentControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Test
    public void createAssignment() throws Exception {

        MockHttpServletResponse response;

        // create DTO with data for new section.
        // the primary key, id, is set to 0. it will be
        // set by the database when the assignment is inserted.
        AssignmentDTO assignment = new AssignmentDTO(
                0,
                "Assignment 4",
                Date.valueOf("2024-03-08"),
                "cst438",
                1,
                10
        );

        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert assignment to String data and set as request content
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignment)))
                        .andReturn()
                        .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        AssignmentDTO result = fromJsonString(response.getContentAsString(), AssignmentDTO.class);

        // primary key should have a non zero value from the database
        assertNotEquals(0, result.id());
        // check other fields of the DTO for expected values
        assertEquals("cst438", result.courseId());
        assertEquals(1, result.secId());
        assertEquals(10, result.secNo());

        // check the database
        Assignment a = assignmentRepository.findById(result.secNo()).orElse(null);
        assertNotNull(a);
        assertEquals("cst438", a.getSection().getCourse().getCourseId());
        assertEquals(1, a.getSection().getSecId());
        assertEquals(10, a.getSection().getSectionNo());

        // clean up after test. delete the assignment from repository.
        assignmentRepository.delete(a);
        // check database for delete
        a = assignmentRepository.findById(result.id()).orElse(null);
        assertNull(a); // section should not be found after delete
    }

    @Test
    public void createAssignmentFailsBadDueDate( ) throws Exception {

        MockHttpServletResponse response;

        // date "2024-07-15" is beyond the Spring 2024 term's end date.
        AssignmentDTO assignment = new AssignmentDTO(
                0,
                "Assignment 4",
                Date.valueOf("2024-07-15"),
                "cst438",
                1,
                10
        );

        // issue the POST request
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignment)))
                .andReturn()
                .getResponse();

        // response should be 422, UNPROCESSIBLE_ENTITY
        assertEquals(422, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("ERROR: Due date 2024-07-14 is invalid. The due date must be between 2024-01-15 and 2024-05-17.", message);

    }

    @Test
    public void createAssignmentFailsBadSecNo( ) throws Exception {

        MockHttpServletResponse response;

        // secNo -1 is not possible as the section number cannot be below 0.
        AssignmentDTO assignment = new AssignmentDTO(
                0,
                "Assignment 4",
                Date.valueOf("2024-03-08"),
                "cst438",
                1,
                -1
        );

        // issue the POST request
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignment)))
                .andReturn()
                .getResponse();

        // response should be 404, NOT_FOUND
        assertEquals(404, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("ERROR: Section -1 not found.", message);

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
