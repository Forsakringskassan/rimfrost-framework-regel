package se.fk.rimfrost.framework.regel;

import io.quarkus.arc.DefaultBean;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import se.fk.rimfrost.framework.regel.error.RegelFelkod;
import se.fk.rimfrost.framework.regel.logic.RegelRequestHandlerBase;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableRegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.entity.CloudEventData;
import se.fk.rimfrost.framework.regel.logic.entity.ImmutableCloudEventData;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
public class RegelRequestHandlerBaseTest extends RegelTestBase
{
   @Inject
   TestRegelRequestHandler testRegelRequestHandler;

   @ConfigProperty(name = "mp.messaging.outgoing.regel-responses.topic")
   private String responseTopic;

   @ConfigProperty(name = "kafka.source")
   private String kafkaSource;

   @BeforeEach
   public void regelResetState()
   {
      super.regelResetState();
   }

   @Test
   public void create_cloud_event_should_create_cloud_event_data_from_request_data()
   {
      var regelDataRequest = ImmutableRegelDataRequest.builder()
            .id(UUID.randomUUID())
            .handlaggningId(UUID.randomUUID())
            .aktivitetId(UUID.randomUUID())
            .kogitorootprocid(UUID.randomUUID().toString())
            .kogitorootprociid(UUID.randomUUID())
            .kogitoparentprociid(UUID.randomUUID())
            .kogitoprocid(UUID.randomUUID().toString())
            .kogitoprocinstanceid(UUID.randomUUID())
            .kogitoprocist(UUID.randomUUID().toString())
            .kogitoprocversion("1")
            .type("test")
            .build();

      var expectedCloudEventData = ImmutableCloudEventData.builder()
            .id(regelDataRequest.id())
            .kogitorootprociid(regelDataRequest.kogitorootprociid())
            .kogitoparentprociid(regelDataRequest.kogitoparentprociid())
            .kogitoprocinstanceid(regelDataRequest.kogitoprocinstanceid())
            .kogitorootprocid(regelDataRequest.kogitorootprocid())
            .kogitoprocid(regelDataRequest.kogitoprocid())
            .kogitoprocist(regelDataRequest.kogitoprocist())
            .kogitoprocversion(regelDataRequest.kogitoprocversion())
            .type(responseTopic)
            .source(kafkaSource)
            .build();

      var cloudEventData = testRegelRequestHandler.createCloudEvent(regelDataRequest);
      assertEquals(expectedCloudEventData, cloudEventData);
   }

   @ParameterizedTest
   @EnumSource(Utfall.class)
   public void send_regel_response_should_support_all_utfall_values(Utfall utfall)
   {
      var handlaggningId = UUID.randomUUID();
      testRegelRequestHandler.sendResponse(handlaggningId, createCloudEventData(), utfall);
      var responses = regelKafkaConnector.waitForMessages(RegelKafkaConnector.regelResponsesChannel);
      assertEquals(1, responses.size());

      var response = responses.getFirst();
      assertInstanceOf(RegelResponseMessagePayload.class, response.getPayload());
      RegelResponseMessagePayload regelResponse = (RegelResponseMessagePayload) response.getPayload();

      assertEquals(handlaggningId.toString(), regelResponse.getData().getHandlaggningId());
      assertEquals(utfall, regelResponse.getData().getUtfall());
      assertNull(regelResponse.getData().getError());
   }

   @Test
   public void send_regel_response_should_support_regel_error_information()
   {
      var regelErrorInfo = new RegelErrorInformation();
      regelErrorInfo.setFelkod(RegelFelkod.RIMFROST_OTHER);
      regelErrorInfo.setFelmeddelande("Test");

      var handlaggningId = UUID.randomUUID();
      testRegelRequestHandler.sendResponse(handlaggningId, createCloudEventData(), regelErrorInfo);

      var responses = regelKafkaConnector.waitForMessages(RegelKafkaConnector.regelResponsesChannel);
      assertEquals(1, responses.size());

      var response = responses.getFirst();
      assertInstanceOf(RegelResponseMessagePayload.class, response.getPayload());
      RegelResponseMessagePayload regelResponse = (RegelResponseMessagePayload) response.getPayload();

      assertEquals(handlaggningId.toString(), regelResponse.getData().getHandlaggningId());
      assertEquals(Utfall.ERROR, regelResponse.getData().getUtfall());
      assertEquals(regelErrorInfo, regelResponse.getData().getError());
   }

   @ApplicationScoped
   @DefaultBean
   public static class TestRegelRequestHandler extends RegelRequestHandlerBase implements RegelRequestHandlerInterface
   {
      public CloudEventData createCloudEvent(RegelDataRequest request)
      {
         return super.createCloudEvent(request);
      }

      public void sendResponse(UUID handlaggningId, CloudEventData cloudEventData, Utfall utfall)
      {
         super.sendResponse(handlaggningId, cloudEventData, utfall);
      }

      public void sendResponse(UUID handlaggningId, CloudEventData cloudEventData, RegelErrorInformation errorInformation)
      {
         super.sendResponse(handlaggningId, cloudEventData, errorInformation);
      }

      @Override
      public void handleRegelRequest(RegelDataRequest request)
      {
         // NO-OP
      }
   }

   private static CloudEventData createCloudEventData()
   {
      return ImmutableCloudEventData.builder()
            .id(UUID.randomUUID())
            .kogitorootprociid(UUID.randomUUID())
            .kogitoparentprociid(UUID.randomUUID())
            .kogitoprocinstanceid(UUID.randomUUID())
            .kogitorootprocid(UUID.randomUUID().toString())
            .kogitoprocid(UUID.randomUUID().toString())
            .kogitoprocist(UUID.randomUUID().toString())
            .kogitoprocversion(UUID.randomUUID().toString())
            .type("test")
            .source("test")
            .build();
   }
}
