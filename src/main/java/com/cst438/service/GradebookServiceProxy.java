package com.cst438.service;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.CourseDTO;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.cst438.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class GradebookServiceProxy {


    Queue gradebookServiceQueue = new Queue("gradebook_service", true);
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Bean
    public Queue createQueue() {
        return new Queue("registrar_service", true);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    public void addCourse(CourseDTO course) {
        sendMessage("addCourse " + asJsonString(course));
    }

    public void updateCourse(CourseDTO course) {
        sendMessage("updateCourse " + asJsonString(course));
    }

    public void deleteCourse(String courseId) {
        sendMessage("deleteCourse " + courseId);
    }

    public void addSection(SectionDTO section) {
        sendMessage("addSection " + asJsonString(section));
    }

    public void updateSection(SectionDTO section) {
        sendMessage("updateSection " + asJsonString(section));
    }

    public void deleteSection(int sectionNo) {
        sendMessage("deleteSection " + sectionNo);
    }

    public void createUser(UserDTO user) {
        sendMessage("createUser " + asJsonString(user));
    }

    public void updateUser(UserDTO user) {
        sendMessage("updateUser " + asJsonString(user));
    }

    public void deleteUser(int userId) {
        sendMessage("deleteUser " + userId);
    }

    public void addCourse(EnrollmentDTO enrollment) {
        sendMessage("addCourse " + asJsonString(enrollment));
    }

    public void dropCourse(int enrollmentId) {
        sendMessage("dropCourse " + enrollmentId);
    }




    @RabbitListener(queues = "registrar_service")
    public void receiveFromGradebook(String message)  {
        try {
            System.out.println("Receive from Gradebook: " + message);
          String[] parts = message.split(" ", 2);
          if(parts[0].equals("updateEnrollment")) {
              EnrollmentDTO dto = fromJsonString(parts[1], EnrollmentDTO.class);
              Enrollment enrollment = enrollmentRepository.findById(dto.enrollmentId()).orElse(null);
              if(enrollment == null) {
                  System.out.println("ERROR: receivedFromGradebook Enrollment " + dto.enrollmentId() + " not found");
              } //if
              else {
                  enrollment.setGrade(dto.grade());
                  enrollmentRepository.save(enrollment);
              } //else
          } //if
        } //try
        catch (Exception e) {
            System.out.println("ERROR: Exception in receivedFromGradebook " + e.getMessage());
        } //catch
    }

    private void sendMessage(String s) {
        System.out.println("Registrar to Gradebook: " + s);
        rabbitTemplate.convertAndSend(gradebookServiceQueue.getName(), s);
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