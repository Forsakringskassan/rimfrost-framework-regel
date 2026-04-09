package se.fk.rimfrost.framework.regel.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Idtyp;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.PutHandlaggningRequest;
import java.util.*;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;

/**
 * WireMock-based test resource for mocking and verifying
 * Handläggning API interactions in tests.
 *
 * <p>This class manages the lifecycle of a shared {@link WireMockServer},
 * provides common endpoint mappings, and exposes helper methods for
 * verifying HTTP requests sent to mocked Handläggning services.</p>
 *
 * <p>Subclasses may extend configuration by overriding
 * {@link #customMapping(WireMockServer)}.</p>
 */
@SuppressWarnings("unused")
@SuppressFBWarnings(value =
{
      "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
      "MS_EXPOSE_REP",
      "MS_PKGPROTECT"
}, justification = "WireMock test resource shared across tests")
public abstract class WireMockHandlaggning implements QuarkusTestResourceLifecycleManager
{
   /**
    * Endpoint path used in handläggning-related tests.
    */
   public static final String handlaggningEndpoint = "/handlaggning/";

   /**
    * Shared WireMock server instance used across tests.
    */
   protected static WireMockServer server;

   /**
    * JSON mapper configured for Java time support.
    */
   private static final ObjectMapper mapper = new ObjectMapper()
         .registerModule(new JavaTimeModule())
         .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

   /**
    * Returns the active WireMock server instance.
    *
    * @return shared WireMock server
    */
   public static WireMockServer getWireMockServer()
   {
      return server;
   }

   /**
    * Starts the WireMock server and initializes test configuration.
    *
    * <p>Includes base mappings and any subclass-specific mappings.</p>
    *
    * @return configuration properties for Quarkus tests
    */
   @Override
   public Map<String, String> start()
   {
      server = new WireMockServer(
            options()
                  .dynamicPort()
                  .usingFilesUnderDirectory("src/test/resources"));
      server.start();
      Map<String, String> config = new HashMap<>(baseMapping(server));
      config.putAll(customMapping(server));
      return config;
   }

   /**
    * Stops the WireMock server if running.
    */
   @Override
   public void stop()
   {
      if (server != null)
      {
         server.stop();
      }
   }

   /**
    * Returns base configuration mappings for handlaggning since all rules access the handlaggning API.
    *
    * @param server active WireMock server
    * @return base property mappings
    */
   protected Map<String, String> baseMapping(WireMockServer server)
   {
      return Map.of(
            "quarkus.rest-client.handlaggning.url", server.baseUrl(),
            "handlaggning.api.base-url", server.baseUrl());
   }

   /**
    * Allows subclasses to provide additional custom mappings.
    *
    * @param server active WireMock server
    * @return extra property mappings
    */
   protected Map<String, String> customMapping(WireMockServer server)
   {
      return Map.of();
   }

   /**
    * Retrieves the latest Handläggning update payload from
    * the last PUT request for a specific handläggning.
    *
    * @param handlaggningId handläggning identifier
    * @return extracted Handläggning update object
    */
   protected se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.HandlaggningUpdate getLastPutHandlaggningUpdate(
         String handlaggningId)
   {

      var request = getLastPutHandlaggningRequest(handlaggningId);

      try
      {
         PutHandlaggningRequest putHandlaggningRequest = mapper.readValue(request.getBodyAsString(),
               PutHandlaggningRequest.class);

         return putHandlaggningRequest.getHandlaggning();
      }
      catch (JsonProcessingException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Waits until the specified minimum number of requests
    * matching a URL pattern have been captured.
    *
    * @param urlRegex URL regex to match
    * @param minRequests minimum number of requests expected
    * @return matching logged requests
    */
   public static List<LoggedRequest> waitForRequest(
         String urlRegex,
         int minRequests)
   {
      List<LoggedRequest> requests = Collections.emptyList();
      int retries = 20;
      long sleepMs = 250;

      for (int i = 0; i < retries; i++)
      {
         requests = server.findAll(anyRequestedFor(urlMatching(urlRegex)));

         if (requests.size() >= minRequests)
         {
            return requests;
         }

         try
         {
            Thread.sleep(sleepMs);
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                  "Interrupted while waiting for WireMock request",
                  e);
         }
      }

      return requests;
   }

   /**
    * Retrieves the most recent PUT request for a given handläggning.
    *
    * @param handlaggningId handläggning identifier
    * @return latest PUT request
    */
   public static LoggedRequest getLastPutHandlaggningRequest(String handlaggningId)
   {
      var requests = waitForRequest(handlaggningEndpoint + handlaggningId, 1);

      return requests.stream()
            .filter(r -> r.getMethod().equals(RequestMethod.PUT))
            .reduce((first, second) -> second)
            .orElseThrow();
   }

   /**
    * Verifies that exactly one GET request was made for
    * the specified handläggning.
    *
    * @param handlaggningId handläggning identifier
    */
   public static void verifyGetHandlaggningProduced(String handlaggningId)
   {
      var requests = waitForRequest(handlaggningEndpoint + handlaggningId, 1);

      assertEquals(
            1,
            requests.stream()
                  .filter(p -> p.getMethod().equals(RequestMethod.GET))
                  .count());
   }

   /**
    * Verifies that exactly one PUT request was made for
    * the specified handläggning.
    *
    * @param handlaggningId handläggning identifier
    */
   public static void verifyPutHandlaggningProduced(String handlaggningId)
   {
      var requests = waitForRequest(handlaggningEndpoint + handlaggningId, 1);

      assertEquals(
            1,
            requests.stream()
                  .filter(p -> p.getMethod().equals(RequestMethod.PUT))
                  .count());
   }

   /**
    * Verifies the content of the PUT request sent to update
    * a handläggning.
    *
    * <p>Checks performer ID and expected task status.</p>
    *
    * @param handlaggningId handläggning identifier
    * @param utforarId expected performer identifier
    * @param expectedUppgiftStatus expected task status
    * @throws Exception if request body parsing fails
    */
   public static void verifyPutHandlaggningContent(
         String handlaggningId,
         Idtyp utforarId,
         String expectedUppgiftStatus) throws Exception
   {

      var request = getLastPutHandlaggningRequest(handlaggningId);
      var dto = mapper.readValue(
            request.getBodyAsString(),
            PutHandlaggningRequest.class);

      var uppgift = dto.getHandlaggning().getUppgift();

      assertEquals(expectedUppgiftStatus, uppgift.getUppgiftStatus());
      assertEquals(utforarId, uppgift.getUtforarId());
   }

   /**
    * Counts requests of a specific HTTP method.
    *
    * @param requests request collection
    * @param method HTTP method to count
    * @return number of matching requests
    */
   public static long countRequests(
         List<LoggedRequest> requests,
         RequestMethod method)
   {
      return requests.stream()
            .filter(r -> r.getMethod().equals(method))
            .count();
   }
}
