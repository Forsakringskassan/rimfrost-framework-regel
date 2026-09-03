package se.fk.rimfrost.framework.regel.logic;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.framework.handlaggning.adapter.HandlaggningAdapter;
import se.fk.rimfrost.framework.handlaggning.exception.HandlaggningException;
import se.fk.rimfrost.framework.handlaggning.model.*;
import se.fk.rimfrost.framework.regel.RegelErrorInformation;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.error.RegelFelkod;
import se.fk.rimfrost.framework.regel.integration.config.RegelConfigProviderYaml;
import se.fk.rimfrost.framework.regel.integration.kafka.RegelKafkaProducer;
import se.fk.rimfrost.framework.regel.logic.config.RegelConfig;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.entity.*;

@SuppressWarnings("unused")
public abstract class RegelRequestHandlerBase
{
   private static final Logger LOGGER = LoggerFactory.getLogger(RegelRequestHandlerBase.class);

   @ConfigProperty(name = "kafka.source")
   protected String kafkaSource;

   @ConfigProperty(name = "mp.messaging.outgoing.regel-responses.topic")
   protected String responseTopic;

   @Inject
   protected RegelMapper regelMapper;

   @Inject
   protected HandlaggningAdapter handlaggningAdapter;

   @Inject
   protected RegelConfigProviderYaml regelConfigProvider;

   @Inject
   protected RegelKafkaProducer regelKafkaProducer;

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
}
