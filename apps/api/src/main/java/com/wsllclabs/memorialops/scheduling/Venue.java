package com.wsllclabs.memorialops.scheduling;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "venue")
public class Venue {

    @Id
    private UUID id;

    @Column(name = "cemetery_id", nullable = false)
    private UUID cemeteryId;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int capacity;

    protected Venue() {}

    public UUID getId() {
        return id;
    }

    public UUID getCemeteryId() {
        return cemeteryId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }
}
