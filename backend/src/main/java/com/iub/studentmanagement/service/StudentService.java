package com.iub.studentmanagement.service;

import com.iub.studentmanagement.exception.DuplicateEmailException;
import com.iub.studentmanagement.exception.StudentNotFoundException;
import com.iub.studentmanagement.model.Student;
import com.iub.studentmanagement.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class that implements business logic for student management operations.
 * This class acts as an intermediary between the controller and repository layers.
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * Retrieves all students from the database.
     *
     * @return a list of all students
     */
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    /**
     * Retrieves students by department.
     *
     * @param department the department to filter by
     * @return a list of students in the specified department
     */
    public List<Student> getStudentsByDepartment(String department) {
        return studentRepository.findByDepartment(department);
    }

    /**
     * Retrieves a student by ID.
     *
     * @param id the ID of the student to retrieve
     * @return the student with the specified ID
     * @throws StudentNotFoundException if no student is found with the given ID
     */
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    /**
     * Retrieves a student by email.
     *
     * @param email the email of the student to retrieve
     * @return the student with the specified email
     * @throws StudentNotFoundException if no student is found with the given email
     */
    public Student getStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with email: " + email));
    }

    /**
     * Creates a new student.
     *
     * @param student the student to create
     * @return the created student with generated ID and timestamps
     * @throws DuplicateEmailException if a student with the same email already exists
     */
    @Transactional
    public Student createStudent(Student student) {
        // Check if email already exists
        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new DuplicateEmailException(student.getEmail());
        }
        
        return studentRepository.save(student);
    }

    /**
     * Updates an existing student.
     *
     * @param id the ID of the student to update
     * @param studentDetails the updated student details
     * @return the updated student
     * @throws StudentNotFoundException if no student is found with the given ID
     * @throws DuplicateEmailException if the new email already exists for another student
     */
    @Transactional
    public Student updateStudent(Long id, Student studentDetails) {
        Student student = getStudentById(id);
        
        // Check if email is being changed and if new email already exists
        if (!student.getEmail().equals(studentDetails.getEmail()) && 
            studentRepository.existsByEmail(studentDetails.getEmail())) {
            throw new DuplicateEmailException(studentDetails.getEmail());
        }
        
        // Update student fields
        student.setName(studentDetails.getName());
        student.setEmail(studentDetails.getEmail());
        student.setDepartment(studentDetails.getDepartment());
        
        return studentRepository.save(student);
    }

    /**
     * Deletes a student by ID.
     *
     * @param id the ID of the student to delete
     * @throws StudentNotFoundException if no student is found with the given ID
     */
    @Transactional
    public void deleteStudent(Long id) {
        // Check if student exists
        if (!studentRepository.existsById(id)) {
            throw new StudentNotFoundException(id);
        }
        
        studentRepository.deleteById(id);
    }

    /**
     * Checks if a student exists by ID.
     *
     * @param id the ID to check
     * @return true if a student with the ID exists, false otherwise
     */
    public boolean existsById(Long id) {
        return studentRepository.existsById(id);
    }

    /**
     * Checks if a student exists by email.
     *
     * @param email the email to check
     * @return true if a student with the email exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        return studentRepository.existsByEmail(email);
    }
}