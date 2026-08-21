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

   @Column(name = "regel_request_id", nullable = false)
   UUID regelRequestId;

   @Column(name = "aktivitet_id", nullable = false)
   UUID aktivitetId;

   @Column(name = "reply_to", nullable = false)
   String replyTo;

   @Column(name = "type", nullable = false)
   String type;

   @Column(name = "kogitorootprocid", nullable = false)
   String kogitorootprocid;

   @Column(name = "kogitorootprociid", nullable = false)
   UUID kogitorootprociid;

   @Column(name = "kogitoparentprociid", nullable = false)
   UUID kogitoparentprociid;

   @Column(name = "kogitoprocid", nullable = false)
   String kogitoprocid;

   @Column(name = "kogitoprocinstanceid", nullable = false)
   UUID kogitoprocinstanceid;

   @Column(name = "kogitoprocist", nullable = false)
   String kogitoprocist;

   @Column(name = "kogitoprocversion", nullable = false)
   String kogitoprocversion;

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
