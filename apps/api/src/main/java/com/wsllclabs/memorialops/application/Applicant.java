package com.wsllclabs.memorialops.application;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "applicant")
public class Applicant {

    @Id
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String email;

    private String phone;

    private String organization;

    @Column(name = "registry_reference")
    private String registryReference;

    @Version
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Applicant() {}

    public static Applicant create(String fullName, Instant now) {
        Applicant applicant = new Applicant();
        applicant.id = UUID.randomUUID();
        applicant.fullName = fullName;
        applicant.createdAt = now;
        applicant.updatedAt = now;
        return applicant;
    }

    public void update(String fullName, String email, String phone, String organization, Instant now) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.organization = organization;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getOrganization() {
        return organization;
    }

    public String getRegistryReference() {
        return registryReference;
    }
}
