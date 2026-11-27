package se.fk.rimfrost.framework.regel;

import io.quarkus.arc.DefaultBean;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.handlaggning.adapter.HandlaggningAdapter;
import se.fk.rimfrost.framework.handlaggning.exception.HandlaggningException;
import se.fk.rimfrost.framework.handlaggning.model.Handlaggning;
import se.fk.rimfrost.framework.handlaggning.model.HandlaggningUpdate;
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
import static se.fk.rimfrost.framework.regel.RegelTestData.HANDLAGGNING_INSTANCE_BASE_URL;

@QuarkusTest
public class RegelRequestHandlerBaseTest extends RegelTestBase
{
   @Inject
   TestRegelRequestHandler testRegelRequestHandler;

   @InjectMock
   HandlaggningAdapter handlaggningAdapter;

   @ConfigProperty(name = "mp.messaging.outgoing.regel-responses.topic")
   String responseTopic;

   @ConfigProperty(name = "kafka.source")
   String kafkaSource;

   @BeforeEach
   public void regelResetState()
   {
      super.regelResetState();
   }

   @Test
   @DisplayName("FRALL-FR-07.1: handlaggningInstanceBaseUrl vidarebefordras till adaptern vid läsning av handläggning")
   public void get_handlaggning_should_forward_base_url_to_adapter() throws HandlaggningException
   {
      var handlaggningId = UUID.randomUUID();
      var mockHandlaggning = Mockito.mock(Handlaggning.class);
      Mockito.when(handlaggningAdapter.readHandlaggning(HANDLAGGNING_INSTANCE_BASE_URL, handlaggningId))
            .thenReturn(mockHandlaggning);

      var result = testRegelRequestHandler.getHandlaggning(
            HANDLAGGNING_INSTANCE_BASE_URL, handlaggningId, createCloudEventData());

      assertEquals(mockHandlaggning, result);
      Mockito.verify(handlaggningAdapter).readHandlaggning(HANDLAGGNING_INSTANCE_BASE_URL, handlaggningId);
   }

   @Test
   @DisplayName("FRALL-FR-07.1: handlaggningInstanceBaseUrl vidarebefordras till adaptern vid uppdatering av handläggning")
   public void update_handlaggning_should_forward_base_url_to_adapter() throws HandlaggningException
   {
      var handlaggningId = UUID.randomUUID();
      var mockHandlaggning = Mockito.mock(Handlaggning.class);
      var handlaggningUpdate = Mockito.mock(HandlaggningUpdate.class);
      Mockito.when(handlaggningUpdate.handlaggning()).thenReturn(mockHandlaggning);
      Mockito.when(mockHandlaggning.id()).thenReturn(handlaggningId);

      testRegelRequestHandler.updateHandlaggning(
            HANDLAGGNING_INSTANCE_BASE_URL, handlaggningUpdate, createCloudEventData(), UUID.randomUUID());

      Mockito.verify(handlaggningAdapter).updateHandlaggning(HANDLAGGNING_INSTANCE_BASE_URL, handlaggningUpdate);
   }

   @Test
   @DisplayName("FRALL-FR-04.1: Regelsvar formateras som CloudEvent med handläggningsid och Kogito-processkorrelationsdata")
   public void create_cloud_event_should_create_cloud_event_data_from_request_data()
   {
      var regelDataRequest = ImmutableRegelDataRequest.builder()
            .id(UUID.randomUUID())
            .handlaggningId(UUID.randomUUID())
            .aktivitetId(UUID.randomUUID())
            .handlaggningInstanceBaseUrl(HANDLAGGNING_INSTANCE_BASE_URL)
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
   @DisplayName("FRALL-FR-03.2, FRALL-FR-03.3: Alla utfall stöds")
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
   @DisplayName("FRALL-FR-03.4: Felutfall inkluderar felkod och felmeddelande och utfallet sätts till ERROR")
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

      public Handlaggning getHandlaggning(String baseUrl, UUID handlaggningId, CloudEventData cloudEventData)
      {
         return super.getHandlaggning(baseUrl, handlaggningId, cloudEventData);
      }

      public void updateHandlaggning(String baseUrl, HandlaggningUpdate update,
            CloudEventData cloudEventData, UUID uppgiftId)
      {
         super.updateHandlaggning(baseUrl, update, cloudEventData, uppgiftId);
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
