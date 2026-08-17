package com.luc.qa.module.registration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "student_directory")
@Getter
@Setter
public class StudentDirectory {

    @Id
    @Column(name = "file_number", length = 20)
    private String fileNumber;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "enrollment_year", nullable = false)
    private int enrollmentYear;

    @Column(nullable = false, length = 80)
    private String faculty;

    @Column(nullable = false, length = 120)
    private String major;

    @Column(name = "registered_at")
    private Instant registeredAt;

    public String fullName() {
        return firstName + " " + lastName;
    }

    public boolean isRegistered() {
        return registeredAt != null;
    }
}
