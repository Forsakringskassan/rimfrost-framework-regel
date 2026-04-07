package se.fk.rimfrost.framework.regel.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.inject.Inject;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeAll;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SuppressWarnings("unused")
public abstract class AbstractRegelTest
{

   protected static final String regelRequestsChannel = "regel-requests";
   protected static final String regelResponsesChannel = "regel-responses";
   protected static final String handlaggningEndpoint = "/handlaggning/";
   protected static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
         .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

   protected static WireMockServer wiremockServer;

   @Inject
   @Connector("smallrye-in-memory")
   protected InMemoryConnector inMemoryConnector;

   @BeforeAll
   static void setup()
   {
      loadTestProperties();
      wiremockServer = AbstractWireMockTestResource.getWireMockServer();
   }

   protected static void loadTestProperties()
   {
      Properties props = new Properties();
      try (InputStream in = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("test.properties"))
      {
         if (in == null)
         {
            throw new RuntimeException("Could not find /test.properties in classpath");
         }
         props.load(in);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Failed to load test.properties", e);
      }
   }

   protected List<LoggedRequest> waitForWireMockRequest(
         WireMockServer server,
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
            throw new RuntimeException("Interrupted while waiting for WireMock request", e);
         }
      }
      return requests;
   }

   protected List<? extends Message<?>> waitForMessages(String channel)
   {
      await()
            .atMost(5, TimeUnit.SECONDS)
            .until(() -> !inMemoryConnector.sink(channel).received().isEmpty());

      return inMemoryConnector.sink(channel).received();
   }

   protected void clearChannel(String channel)
   {
      inMemoryConnector.sink(channel).clear();
   }

   protected long countRequests(List<LoggedRequest> requests, RequestMethod method)
   {
      return requests.stream()
            .filter(r -> r.getMethod().equals(method))
            .count();
   }
}
