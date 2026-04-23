package se.fk.rimfrost.framework.regel;

import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.*;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.PutHandlaggningRequest;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Uppgift;

/**
 * WireMock-based test resource for mocking and verifying Handläggning API interactions in tests.
 *
 * <p>This class manages the lifecycle of a shared {@link WireMockServer}, provides common endpoint
 * mappings, and exposes helper methods for verifying HTTP requests sent to mocked Handläggning
 * services.</p>
 *
 * <p>Subclasses may extend configuration by overriding {@link #wiremockMapping(WireMockServer)}.</p>
 */
@SuppressWarnings("unused")
@SuppressFBWarnings(value =
{
      "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", "MS_EXPOSE_REP", "MS_PKGPROTECT"
}, justification = "WireMock test resource shared across tests")
public abstract class WireMockHandlaggning implements QuarkusTestResourceLifecycleManager
{
   /** Endpoint path used in handläggning-related tests. */
   public static final String handlaggningEndpoint = "/handlaggning/";

   /** Shared WireMock server instance used across tests. */
   protected static WireMockServer server;

   /** JSON mapper configured for Java time support. */
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
      server = new WireMockServer(options().dynamicPort().usingFilesUnderDirectory("src/test/resources"));
      server.start();
      return wiremockMapping(server);
   }

   /** Stops the WireMock server if running. */
   @Override
   public void stop()
   {
      if (server != null)
      {
         server.stop();
      }
   }

   /**
   * Defines wiremock mappings
   *
   * @param server active WireMock server
   * @return property mappings
   */
   protected Map<String, String> wiremockMapping(WireMockServer server)
   {
      Map<String, String> map = new HashMap<>();
      map.put("quarkus.rest-client.handlaggning.url", server.baseUrl());
      map.put("handlaggning.api.base-url", server.baseUrl());
      return map;
   }

   /**
   * Retrieves the latest Handläggning update payload from the last PUT request for a specific
   * handläggning.
   *
   * @param handlaggningId handläggning identifier
   * @return extracted Handläggning update object
   */
   protected se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.HandlaggningUpdate getLastPutHandlaggningUpdate(
         String handlaggningId)
   {

      var request = getLastHandlaggningRequest(handlaggningId, RequestMethod.PUT);

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
   * Waits until the specified minimum number of requests matching both a URL pattern and HTTP
   * requestMethod have been captured.
   *
   * @param urlRegex URL regex to match
   * @param requestMethod HTTP requestMethod to filter by
   * @param minRequests minimum number of matching requests expected
   * @return matching logged requests
   */
   public static List<LoggedRequest> waitForRequest(
         String urlRegex, RequestMethod requestMethod, int minRequests)
   {
      List<LoggedRequest> requests = Collections.emptyList();
      int retries = 20;
      long sleepMs = 250;

      for (int i = 0; i < retries; i++)
      {
         requests = server.findAll(anyRequestedFor(urlMatching(urlRegex))).stream()
               .filter(r -> r.getMethod().equals(requestMethod))
               .toList();

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
            throw new RuntimeException("Interrupted while waiting for WireMock request", e);
         }
      }

      return requests;
   }

   /**
   * Wait for at least a specified number of handläggning requests are received
   *
   * @param handlaggningId handläggning identifier
   * @param requestMethod the type of request (i.e. GET/PUT/POST/PATCH)
   * @param minRequests minimum number of handläggning requests
   * @return requests a list af received handläggning requests
   */
   public static List<LoggedRequest> waitForHandlaggningRequests(
         String handlaggningId, RequestMethod requestMethod, Integer minRequests)
   {
      return waitForRequest(handlaggningEndpoint + handlaggningId, requestMethod, minRequests);
   }

   /**
   * Retrieves the most recent request of specified RequestMethod for a given handläggning.
   *
   * @param handlaggningId handläggning identifier
   * @param requestMethod the type of request (i.e. GET/PUT/POST/PATCH)
   * @return latest handläggning request
   */
   public static LoggedRequest getLastHandlaggningRequest(
         String handlaggningId, RequestMethod requestMethod)
   {
      var handlaggningRequests = waitForHandlaggningRequests(handlaggningId, requestMethod, 1);
      return handlaggningRequests.stream().reduce((first, second) -> second).orElseThrow();
   }

   /**
   * Retrieves the last PUT handläggning request
   *
   * @param handlaggningId handläggning identifier
   * @return latest Put handläggning
   */
   public static PutHandlaggningRequest getLastPutHandlaggning(String handlaggningId)
         throws JsonProcessingException
   {
      LoggedRequest request = getLastHandlaggningRequest(handlaggningId, RequestMethod.PUT);
      return mapper.readValue(request.getBodyAsString(), PutHandlaggningRequest.class);
   }

   /**
   * Retrieves the Uppgift from the last PUT handläggning request
   *
   * @param handlaggningId handläggning identifier
   * @return uppgift from latest Put handläggning
   */
   public static Uppgift getUppgiftFromLastPutHandlaggning(String handlaggningId)
         throws JsonProcessingException
   {
      PutHandlaggningRequest dto = getLastPutHandlaggning(handlaggningId);
      return dto.getHandlaggning().getUppgift();
   }

   /**
   * Counts requests of a specific HTTP method.
   *
   * @param requests request collection
   * @param method HTTP method to count
   * @return number of matching requests
   */
   public static long countRequests(List<LoggedRequest> requests, RequestMethod method)
   {
      return requests.stream().filter(r -> r.getMethod().equals(method)).count();
   }
}
