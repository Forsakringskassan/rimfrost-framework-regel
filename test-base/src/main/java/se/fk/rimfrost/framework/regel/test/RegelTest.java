package se.fk.rimfrost.framework.regel.test;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.inject.Inject;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

public abstract class RegelTest
{

   protected static final String regelRequestsChannel = "regel-requests";

   @Inject
   @Connector("smallrye-in-memory")
   protected InMemoryConnector inMemoryConnector;

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
