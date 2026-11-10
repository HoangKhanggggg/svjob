package vn.vlu.svjob.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
@NoArgsConstructor
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String school;
    private String major;

    @Column(name = "year_of_study")
    private Integer yearOfStudy;

    private String skills;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "cv_url")
    private String cvUrl;

    public StudentProfile(User user) {
        this.user = user;
    }
}
