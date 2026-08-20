package se.fk.rimfrost.framework.regel.logic.storage.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity backing {@code KompletteringStorage}. Primary key is {@code handlaggningId}.
 * Table is isolated per regel service via {@code quarkus.flyway.default-schema}.
 */
@Entity
@Table(name = "komplettering_tillstand")
public class KompletteringTillstandEntity
{
   @Id
   @Column(name = "handlaggning_id")
   UUID handlaggningId;

   @Column(name = "oul_uppgift_id", nullable = false)
   UUID oulUppgiftId;

   @Column(name = "reply_to", nullable = false)
   String replyTo;

   @Column(name = "cloud_event_data", nullable = false, columnDefinition = "text")
   String cloudEventData;

   @Version
   @Column(name = "version")
   long version;

   @Column(name = "created_at", nullable = false, updatable = false)
   Instant createdAt;

   @Column(name = "updated_at", nullable = false)
   Instant updatedAt;

   @PrePersist
   void onCreate()
   {
      createdAt = Instant.now();
      updatedAt = createdAt;
   }

   @PreUpdate
   void onUpdate()
   {
      updatedAt = Instant.now();
   }
}
