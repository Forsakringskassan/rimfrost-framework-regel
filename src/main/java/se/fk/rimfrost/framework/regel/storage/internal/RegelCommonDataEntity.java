package se.fk.rimfrost.framework.regel.storage.internal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "common_data")
@SuppressFBWarnings("UUF_UNUSED_FIELD")
public class RegelCommonDataEntity
{
   @Id
   UUID handlaggningId;

   UUID uppgiftId;
   int uppgiftVersion;
   Instant uppgiftSkapadTs;
   Instant uppgiftUtfordTs;
   Instant uppgiftPlaneradTs;
   String uppgiftUtforarIdTypId;
   String uppgiftUtforarIdVarde;
   String uppgiftStatus;
   UUID uppgiftAktivitetId;
   String uppgiftFssaInformation;
   UUID uppgiftSpecifikationId;
   Integer uppgiftSpecifikationVersion;

   @Column(name = "oul_uppgift_id")
   UUID oulUppgiftId;

   @Version
   long version;

   @Column(nullable = false, updatable = false)
   Instant createdAt;

   @Column(nullable = false)
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
