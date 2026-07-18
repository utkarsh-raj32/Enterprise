package com.enterprise.hrm.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * ============================================================
 * BASE ENTITY — Audit Fields Superclass
 * ============================================================
 *
 * WHY have a BaseEntity?
 *   Every table in an enterprise system needs audit columns:
 *   created_at, updated_at. Instead of repeating these fields
 *   in every entity, we use inheritance via @MappedSuperclass.
 *
 * @MappedSuperclass
 *   Tells Hibernate: "This class is NOT a standalone table.
 *   Its fields should be included in any @Entity that extends it."
 *   Unlike @Entity, there is no 'base_entity' table created.
 *
 * @EntityListeners(AuditingEntityListener.class)
 *   Registers Spring Data's auditing listener on this class.
 *   The listener intercepts @PrePersist and @PreUpdate Hibernate
 *   lifecycle events and automatically sets @CreatedDate and
 *   @LastModifiedDate. @EnableJpaAuditing in EhrApplication
 *   activates this mechanism globally.
 *
 * HIBERNATE ENTITY LIFECYCLE (for interview):
 *   TRANSIENT → MANAGED → DETACHED → REMOVED
 *
 *   • TRANSIENT  : new Employee() — not tracked by Hibernate
 *   • MANAGED    : after save() — Hibernate tracks changes (dirty checking)
 *   • DETACHED   : after transaction ends — snapshot of managed state
 *   • REMOVED    : after delete() — will be deleted on flush/commit
 *
 * COLUMN updatable=false on createdAt:
 *   Prevents Hibernate from ever UPDATE-ing the creation timestamp,
 *   even if someone accidentally sets it. It can only be set on INSERT.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)   // Required for @CreatedDate / @LastModifiedDate
public abstract class BaseEntity {

    /**
     * @CreatedDate — Spring Data sets this automatically on first persist.
     * @Column(updatable = false) — Once set, this value never changes.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * @LastModifiedDate — Spring Data updates this on every merge/save.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
