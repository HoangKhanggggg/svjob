package vn.vlu.svjob.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employer_profiles")
@Getter
@Setter
@NoArgsConstructor
public class EmployerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    private String address;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "contact_phone", length = 15)
    private String contactPhone;

    @Column(name = "logo_url")
    private String logoUrl;

    public EmployerProfile(User user, String companyName) {
        this.user = user;
        this.companyName = companyName;
    }
}
