package com.cst438.service;

import com.cst438.domain.*;
import com.cst438.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class RegistrarServiceProxy {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private TermRepository termRepository;

    Queue registrarServiceQueue = new Queue("registrar_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("gradebook_service", true);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "gradebook_service")
    public void receiveFromRegistrar(String message) {
        try {
            String[] parts = message.split(" ", 2);
            String action = parts[0];

            switch (action) {
                case "addCourse":
                    CourseDTO courseDTO = fromJsonString(parts[1], CourseDTO.class);
                    Course course = new Course();
                    course.setCourseId(courseDTO.courseId());
                    course.setTitle(courseDTO.title());
                    course.setCredits(courseDTO.credits());
                    courseRepository.save(course);
                    break;

                case "updateCourse":
                    CourseDTO updateCourseDTO = fromJsonString(parts[1], CourseDTO.class);
                    Course existingCourse = courseRepository.findById(updateCourseDTO.courseId()).orElse(null);
                    if (existingCourse != null) {
                        existingCourse.setTitle(updateCourseDTO.title());
                        existingCourse.setCredits(updateCourseDTO.credits());
                        courseRepository.save(existingCourse);
                    }
                    break;

                case "deleteCourse":
                    courseRepository.deleteById(parts[1]);
                    break;

                case "addSection":
                    SectionDTO sectionDTO = fromJsonString(parts[1], SectionDTO.class);
                    Section section = new Section();
                    section.setSectionNo(sectionDTO.secNo());
                    section.setSecId(sectionDTO.secId());
                    section.setBuilding(sectionDTO.building());
                    section.setRoom(sectionDTO.room());
                    section.setTimes(sectionDTO.times());
                    section.setInstructor_email(sectionDTO.instructorEmail());
                    section.setCourse(courseRepository.findById(sectionDTO.courseId()).orElse(null));
                    section.setTerm(termRepository.findByYearAndSemester(sectionDTO.year(), sectionDTO.semester()));
                    sectionRepository.save(section);
                    break;

                case "updateSection":
                    SectionDTO updateSectionDTO = fromJsonString(parts[1], SectionDTO.class);
                    Section existingSection = sectionRepository.findById(updateSectionDTO.secNo()).orElse(null);
                    if (existingSection != null) {
                        existingSection.setSecId(updateSectionDTO.secId());
                        existingSection.setBuilding(updateSectionDTO.building());
                        existingSection.setRoom(updateSectionDTO.room());
                        existingSection.setTimes(updateSectionDTO.times());
                        existingSection.setInstructor_email(updateSectionDTO.instructorEmail());
                        sectionRepository.save(existingSection);
                    }
                    break;

                case "deleteSection":
                    sectionRepository.deleteById(Integer.parseInt(parts[1]));
                    break;

                case "createUser":
                    UserDTO userDTO = fromJsonString(parts[1], UserDTO.class);
                    User user = new User();
                    user.setId(userDTO.id());
                    user.setName(userDTO.name());
                    user.setEmail(userDTO.email());
                    user.setType(userDTO.type());
                    userRepository.save(user);
                    break;

                case "updateUser":
                    UserDTO updateUserDTO = fromJsonString(parts[1], UserDTO.class);
                    User existingUser = userRepository.findById(updateUserDTO.id()).orElse(null);
                    if (existingUser != null) {
                        existingUser.setName(updateUserDTO.name());
                        existingUser.setEmail(updateUserDTO.email());
                        existingUser.setType(updateUserDTO.type());
                        userRepository.save(existingUser);
                    }
                    break;

                case "deleteUser":
                    userRepository.deleteById(Integer.parseInt(parts[1]));
                    break;

                case "enrollCourse": // Updated from addCourse
                    EnrollmentDTO enrollmentDTO = fromJsonString(parts[1], EnrollmentDTO.class);
                    Enrollment enrollment = new Enrollment();
                    enrollment.setEnrollmentId(enrollmentDTO.enrollmentId());
                    enrollment.setGrade(enrollmentDTO.grade());
                    enrollment.setUser(userRepository.findById(enrollmentDTO.studentId()).orElse(null));
                    enrollment.setSection(sectionRepository.findById(enrollmentDTO.sectionNo()).orElse(null));
                    enrollmentRepository.save(enrollment);
                    break;

                case "dropCourse":
                    enrollmentRepository.deleteById(Integer.parseInt(parts[1]));
                    break;

                default:
                    throw new IllegalArgumentException("Unknown action: " + action);
            }
        } catch (Exception e) {
            System.err.println("ERROR: Exception in receiveFromRegistrar " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendMessage(String s) {
        rabbitTemplate.convertAndSend(registrarServiceQueue.getName(), s);
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJsonString(String str, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
