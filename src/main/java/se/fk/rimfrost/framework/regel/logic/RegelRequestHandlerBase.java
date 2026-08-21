package se.fk.rimfrost.framework.regel.logic;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.framework.handlaggning.adapter.HandlaggningAdapter;
import se.fk.rimfrost.framework.handlaggning.exception.HandlaggningException;
import se.fk.rimfrost.framework.handlaggning.model.*;
import se.fk.rimfrost.framework.oul.adapter.OulAdapter;
import se.fk.rimfrost.framework.oul.exception.OulException;
import se.fk.rimfrost.framework.oul.model.CreateOperativUppgiftRequest;
import se.fk.rimfrost.framework.oul.model.Erbjudande;
import se.fk.rimfrost.framework.oul.model.ImmutableErbjudande;
import se.fk.rimfrost.framework.oul.model.OperativUppgift;
import se.fk.rimfrost.framework.regel.RegelErrorInformation;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.error.RegelFelkod;
import se.fk.rimfrost.framework.regel.integration.config.RegelConfigProviderYaml;
import se.fk.rimfrost.framework.regel.integration.kafka.RegelKafkaProducer;
import se.fk.rimfrost.framework.regel.logic.config.RegelConfig;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.entity.*;
import se.fk.rimfrost.framework.regel.storage.CloudEventDataStorage;
import se.fk.rimfrost.framework.regel.storage.ProcessTopicInfoStorage;
import se.fk.rimfrost.framework.regel.storage.RegelCommonDataStorage;
import se.fk.rimfrost.framework.regel.storage.entity.ProcessTopicInfo;
import se.fk.rimfrost.framework.regel.storage.entity.RegelCommonData;

@SuppressWarnings("unused")
public abstract class RegelRequestHandlerBase
{
   Logger LOGGER = LoggerFactory.getLogger(RegelRequestHandlerBase.class);

   @ConfigProperty(name = "kafka.source")
   protected String kafkaSource;

   @ConfigProperty(name = "mp.messaging.outgoing.regel-responses.topic")
   protected String responseTopic;

   @ConfigProperty(name = "kafka.subtopic")
   protected String oulReplyToSubTopic;

   @Inject
   protected RegelMapper regelMapper;

   @Inject
   protected HandlaggningAdapter handlaggningAdapter;

   @Inject
   protected RegelConfigProviderYaml regelConfigProvider;

   @Inject
   protected RegelKafkaProducer regelKafkaProducer;

   @Inject
   protected OulAdapter oulAdapter;

   @Inject
   protected CloudEventDataStorage cloudEventDataStorage;

   @Inject
   protected ProcessTopicInfoStorage processTopicInfoStorage;

   @Inject
   RegelCommonDataStorage dataStorage;

   protected RegelConfig regelConfig;

   /*
   * Note: The name of the @PostConstruct method should if
   * possible be kept as init<classname> in order to avoid
   * being shadowed by any @PostConstruct methods in any
   * inheriting class that happens to have the same method
   * name.
   */
   @PostConstruct
   private void initRegelRequestHandlerBase()
   {
      this.regelConfig = regelConfigProvider.getConfig();
   }

   protected CloudEventData createCloudEvent(RegelDataRequest request)
   {
      return ImmutableCloudEventData.builder()
            .id(request.id())
            .kogitoparentprociid(request.kogitoparentprociid())
            .kogitoprocid(request.kogitoprocid())
            .kogitoprocinstanceid(request.kogitoprocinstanceid())
            .kogitoprocist(request.kogitoprocist())
            .kogitoprocversion(request.kogitoprocversion())
            .kogitorootprocid(request.kogitorootprocid())
            .kogitorootprociid(request.kogitorootprociid())
            .type(responseTopic)
            .source(kafkaSource)
            .build();
   }

   protected void sendResponse(UUID handlaggningId, CloudEventData cloudEventData, Utfall utfall, String replyTo)
   {
      try
      {
         var regelResponse = regelMapper.toRegelResponse(handlaggningId, cloudEventData, utfall);
         regelKafkaProducer.sendRegelResponse(regelResponse, Objects.requireNonNull(replyTo));
      }
      catch (IllegalStateException e)
      {
         LOGGER.error("Failed to send regel response for handlaggning. handlaggningId: {}, utfall: {}", handlaggningId, utfall,
               e);
      }
   }

   protected void sendResponse(UUID handlaggningId, CloudEventData cloudEventData, RegelErrorInformation errorInformation,
         String replyTo)
   {
      try
      {
         var regelResponse = regelMapper.toRegelResponse(handlaggningId, cloudEventData, errorInformation);
         regelKafkaProducer.sendRegelResponse(regelResponse, Objects.requireNonNull(replyTo));
      }
      catch (IllegalStateException e)
      {
         LOGGER.error("Failed to send regel response for handlaggning. handlaggningId: {}, regelErrorInformation: {}",
               handlaggningId, errorInformation, e);
      }
   }

   protected void tryEndOperativUppgift(UUID uppgiftId, String reason)
   {
      try
      {
         oulAdapter.endOperativUppgift(uppgiftId, reason);
      }
      catch (OulException e)
      {
         LOGGER.error("Could not end operativ uppgift with id {}", uppgiftId);
      }
   }

   protected OperativUppgift createOperativUppgift(CreateOperativUppgiftRequest oulRequest, CloudEventData cloudEventData)
   {
      try
      {
         return oulAdapter.createOperativUppgift(oulRequest);
      }
      catch (OulException e)
      {
         var message = String.format(
               "Failed to create operativ uppgift. handlaggningId: %s, kogitoprocId: %s reason: %s",
               oulRequest.getHandlaggningId(), cloudEventData.kogitoprocinstanceid(), e.getMessage());
         var regelErrorInformation = createRegelErrorInformation(RegelFelkod.RIMFROST_OTHER, message);
         throw new RegelCancelledException(regelErrorInformation, message, e);
      }
   }

   protected Erbjudande createErbjudande(String id, String namn)
   {
      return ImmutableErbjudande.builder()
            .id(id)
            .namn(namn)
            .build();
   }

   protected Status toHttpStatus(OulException e) {
      return switch (e.getErrorType()) {
         case NOT_FOUND -> Response.Status.NOT_FOUND;
         case BAD_REQUEST -> Response.Status.BAD_REQUEST;
         case SERVICE_UNAVAILABLE -> Response.Status.SERVICE_UNAVAILABLE;
         default -> Response.Status.INTERNAL_SERVER_ERROR;
      };
   }

   protected static Response.Status toHttpStatus(HandlaggningException e) {
      return switch (e.getErrorType()) {
         case NOT_FOUND -> Response.Status.NOT_FOUND;
         case BAD_REQUEST -> Response.Status.BAD_REQUEST;
         case SERVICE_UNAVAILABLE -> Response.Status.SERVICE_UNAVAILABLE;
         default -> Response.Status.INTERNAL_SERVER_ERROR;
      };
   }

   protected HandlaggningUpdate createHandlaggningUpdate(Handlaggning handlaggning, Uppgift uppgift, UUID kogitoprocInstanceId,
         int version)
   {
      return ImmutableHandlaggningUpdate.builder()
            .id(handlaggning.id())
            .version(version)
            .yrkande(handlaggning.yrkande())
            .processInstansId(kogitoprocInstanceId)
            .skapadTS(handlaggning.skapadTS())
            .avslutadTS(handlaggning.avslutadTS())
            .handlaggningspecifikationId(handlaggning.handlaggningspecifikationId())
            .uppgift(uppgift)
            .build();
   }

   protected Uppgift createUppgift(UUID aktivitetId, String status)
   {
      return ImmutableUppgift.builder()
            .id(UUID.randomUUID())
            .version(1)
            .aktivitetId(aktivitetId)
            .skapadTs(OffsetDateTime.now())
            .uppgiftStatus(status)
            .fSSAinformation("FSSAinformation.HANDLAGGNING_PAGAR") // TODO
            .uppgiftSpecifikation(createUppgiftSpecifikation())
            .build();
   }

   protected UppgiftSpecifikation createUppgiftSpecifikation()
   {
      return ImmutableUppgiftSpecifikation.builder()
            .id(regelConfig.getSpecifikation().getId())
            .version(regelConfig.getSpecifikation().getVersion())
            .build();
   }

   protected Idtyp toHandlaggningModelIdtyp(se.fk.rimfrost.framework.oul.logic.dto.Idtyp idtyp)
   {
      return ImmutableIdtyp.builder()
            .typId(idtyp.typId())
            .varde(idtyp.varde())
            .build();
   }

   protected void sendErrorResponse(UUID handlaggningId, CloudEventData cloudEventData,
         RegelErrorInformation regelErrorInformation, String replyTo)
   {
      if (handlaggningId == null || cloudEventData == null || regelErrorInformation == null)
      {
         LOGGER.warn(
               "Could not send error response. Missing one or more required parameters. handlaggningId: {}, cloudEventData: {}, regelErrorInformation: {}",
               handlaggningId, cloudEventData, regelErrorInformation);
         return;
      }

      sendResponse(handlaggningId, cloudEventData, regelErrorInformation, replyTo);
   }

   protected RegelErrorInformation createRegelErrorInformation(String felkod, String meddelande)
   {
      RegelErrorInformation regelErrorInformation = new RegelErrorInformation();
      regelErrorInformation.setFelkod(felkod);
      regelErrorInformation.setFelmeddelande(meddelande);

      return regelErrorInformation;
   }

   protected Handlaggning getHandlaggning(UUID handlaggningId, CloudEventData cloudEventData)
   {
      try
      {
         return handlaggningAdapter.readHandlaggning(handlaggningId);
      }
      catch (HandlaggningException e)
      {
         var message = String.format(
               "Failed to read handlaggning. handlaggningId: %s, kogitoprocId: %s", handlaggningId,
               cloudEventData.kogitoprocinstanceid());
         var regelErrorInformation = createRegelErrorInformation(RegelFelkod.RIMFROST_HANDLAGGNING_READ_FAILURE, message);
         throw new RegelCancelledException(regelErrorInformation, message, e);
      }
   }

   protected void updateHandlaggning(HandlaggningUpdate handlaggningUpdate,
         CloudEventData cloudEventData, UUID uppgiftId)
   {
      try
      {
         handlaggningAdapter.updateHandlaggning(handlaggningUpdate);
      }
      catch (HandlaggningException e)
      {
         var message = String.format(
               "Failed to write handlaggning update. handlaggningId: %s, kogitoprocId: %s",
               handlaggningUpdate.id(), cloudEventData.kogitoprocinstanceid());
         var regelErrorInformation = createRegelErrorInformation(RegelFelkod.RIMFROST_HANDLAGGNING_WRITE_FAILURE, message);
         throw new RegelCancelledException(regelErrorInformation, message, e);
      }
   }

   protected void writeCloudEventData(UUID handlaggningId,
         CloudEventData cloudEventData, UUID uppgiftId)
   {
      try
      {
         this.cloudEventDataStorage.setCloudEventData(handlaggningId, cloudEventData);
      }
      catch (Exception e)
      {
         var message = String.format(
               "Failed to write CloudEventData to correlation storage. handlaggningId: %s, kogitoprocId: %s",
               handlaggningId, cloudEventData.kogitoprocinstanceid());
         var regelErrorInformation = createRegelErrorInformation(RegelFelkod.RIMFROST_CLOUD_EVENT_DATA_WRITE_FAILURE, message);
         throw new RegelCancelledException(regelErrorInformation, message, e);
      }
   }

   protected CloudEventData readCloudEventData(UUID handlaggningId)
   {
      try
      {
         return cloudEventDataStorage.getCloudEventData(handlaggningId);
      }
      catch (Exception e)
      {
         LOGGER.error("Failed to read CloudEventData from correlation storage. handlaggningId: {}", handlaggningId, e);
         return null;
      }
   }

   protected void tryDeleteCloudEventData(UUID handlaggningId)
   {
      try
      {
         this.cloudEventDataStorage.deleteCloudEventData(handlaggningId);
      }
      catch (Exception e)
      {
         LOGGER.error("Could not delete cloud event data. handlaggningId: {}", handlaggningId, e);
      }
   }

   protected void writeManuellRegelCommonData(UUID handlaggningId, UUID uppgiftId, RegelCommonData regelCommonData)
   {
      try
      {
         dataStorage.setRegelCommonData(handlaggningId, regelCommonData);
      }
      catch (Exception e)
      {
         var message = String.format(
               "Failed to write RegelCommonData update to data storage. handlaggningId: %s",
               handlaggningId);
         var regelErrorInformation = createRegelErrorInformation(RegelFelkod.RIMFROST_MANUELL_REGEL_COMMON_DATA_WRITE_FAILURE,
               message);
         throw new RegelCancelledException(regelErrorInformation, message, e);
      }
   }

   protected RegelCommonData readRegelCommonData(UUID handlaggningId)
   {
      try
      {
         return dataStorage.getRegelCommonData(handlaggningId);
      }
      catch (Exception e)
      {
         var message = String.format(
               "Failed to read RegelCommonData from data storage. handlaggningId: %s",
               handlaggningId);
         var regelErrorInformation = createRegelErrorInformation(RegelFelkod.RIMFROST_MANUELL_REGEL_COMMON_DATA_READ_FAILURE,
               message);
         throw new RegelCancelledException(regelErrorInformation, message, e);
      }
   }

   protected void tryDeleteRegelCommonData(UUID handlaggningId)
   {
      try
      {
         this.dataStorage.deleteRegelCommonData(handlaggningId);
      }
      catch (Exception e)
      {
         LOGGER.error("Could not delete RegelCommonData from data storage. handlaggningId: {}", handlaggningId, e);
      }
   }

   protected void writeProcessTopicInfo(UUID handlaggningId,
         ProcessTopicInfo processTopicInfo)
   {
      try
      {
         this.processTopicInfoStorage.setProcessTopicInfo(handlaggningId, processTopicInfo);
      }
      catch (Exception e)
      {
         var message = String.format(
               "Failed to write ProcessTopicInfo to correlation storage. handlaggningId: %s",
               handlaggningId);
         var regelErrorInformation = createRegelErrorInformation(RegelFelkod.RIMFROST_PROCESS_TOPIC_INFO_WRITE_FAILURE, message);
         throw new RegelCancelledException(regelErrorInformation, message, e);
      }
   }

   protected ProcessTopicInfo readProcessTopicInfo(UUID handlaggningId)
   {
      try
      {
         return processTopicInfoStorage.getProcessTopicInfo(handlaggningId);
      }
      catch (Exception e)
      {
         LOGGER.error("Failed to read ProcessTopicInfo from correlation storage. handlaggningId: {}", handlaggningId, e);
         return null;
      }
   }

   protected void tryDeleteProcessTopicInfo(UUID handlaggningId)
   {
      try
      {
         this.processTopicInfoStorage.deleteProcessTopicInfo(handlaggningId);
      }
      catch (Exception e)
      {
         LOGGER.error("Could not delete process topic info. handlaggningId: {}", handlaggningId, e);
      }
   }

}
