package com.wsllclabs.memorialops.shared.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "reference_counter")
@IdClass(ReferenceCounter.Key.class)
public class ReferenceCounter {

    @Id
    private String kind;

    @Id
    private int year;

    @Column(name = "last_value", nullable = false)
    private int lastValue;

    protected ReferenceCounter() {}

    public ReferenceCounter(String kind, int year) {
        this.kind = kind;
        this.year = year;
        this.lastValue = 0;
    }

    public int nextValue() {
        this.lastValue += 1;
        return this.lastValue;
    }

    public int getLastValue() {
        return lastValue;
    }

    public static final class Key implements Serializable {
        private String kind;
        private int year;

        public Key() {}

        public Key(String kind, int year) {
            this.kind = kind;
            this.year = year;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key key)) {
                return false;
            }
            return year == key.year && Objects.equals(kind, key.kind);
        }

        @Override
        public int hashCode() {
            return Objects.hash(kind, year);
        }
    }
}
