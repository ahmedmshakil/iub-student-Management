package com.iub.studentmanagement.repository;

import com.iub.studentmanagement.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Student entity that provides CRUD operations and custom query methods.
 * Extends JpaRepository to inherit basic CRUD operations and pagination support.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    /**
     * Find all students belonging to a specific department.
     * 
     * @param department the department name to search for
     * @return a list of students in the specified department
     */
    List<Student> findByDepartment(String department);
    
    /**
     * Find a student by their email address.
     * 
     * @param email the email address to search for
     * @return an Optional containing the student if found, or empty if not found
     */
    Optional<Student> findByEmail(String email);
    
    /**
     * Check if a student with the given email exists.
     * Used for email uniqueness validation.
     * 
     * @param email the email address to check
     * @return true if a student with the email exists, false otherwise
     */
    boolean existsByEmail(String email);
}