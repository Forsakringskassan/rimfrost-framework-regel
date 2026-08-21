package se.fk.rimfrost.framework.regel.storage.internal;

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
@Table(name = "process_topic_info")
public class ProcessTopicInfoEntity
{
   @Id
   @Column(name = "handlaggning_id")
   UUID handlaggningId;

   @Column(name = "reply_topic", nullable = false)
   String replyTopic;

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
