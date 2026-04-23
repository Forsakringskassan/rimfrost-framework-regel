package se.fk.rimfrost.framework.regel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

@SuppressWarnings("unused")

/**
 * Base class for implementing tests within the Regel framework.
 *
 * <p>This class provides common test setup utilities, including:</p>
 *
 * <ul>
 *   <li>A preconfigured {@link ObjectMapper}
 *   <li>An injected {@link InMemoryConnector} for testing kafka in-memory
 *   <li>Lifecycle management and reset of {@link RegelKafkaConnector}
 *   <li>Loading of test configuration from {@code test.properties}
 * </ul>
 *
 * <p><b>In-memory connector behavior:</b> The {@link InMemoryConnector} may retain state across
 * tests even when newly injected. Therefore, {@link RegelKafkaConnector#clear()} is always invoked
 * before each test to ensure isolation.</p>
 *
 * <p><b>Usage:</b></p>
 *
 * <pre>
 * class MyRegelTest extends AbstractRegelTest {
 *     // write tests using mapper and regelKafkaConnector
 * }
 * </pre>
 *
 * <p><b>Configuration:</b> Test configuration is loaded from {@code test.properties} located on the
 * classpath.</p>
 *
 * @see RegelKafkaConnector
 * @see InMemoryConnector
 */
@SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "Injected by Quarkus CDI, used at runtime")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class RegelTestBase
{

   protected static final ObjectMapper mapper = new ObjectMapper()
         .registerModule(new JavaTimeModule())
         .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

   @Inject
   @Connector("smallrye-in-memory")
   protected InMemoryConnector inMemoryConnector;

   protected RegelKafkaConnector regelKafkaConnector;

   /**
    * Resets messaging state.
    * Intended to be called in a BeforeEach-annotated method in subclasses.
    *
    * <p>Ensures that the {@link RegelKafkaConnector} is initialized and clears any previously
    * retained messages from the underlying {@link InMemoryConnector}.</p>
    *
    * <p><b>Note:</b> The {@link InMemoryConnector} may retain state across tests, even when newly
    * injected, so clearing is always required to guarantee test isolation.</p>
    */
   protected void regelResetState()
   {
      if (inMemoryConnector == null)
      {
         throw new IllegalStateException("inMemoryConnector not injected - missing @QuarkusTest or test context");
      }
      if (regelKafkaConnector == null)
      {
         regelKafkaConnector = new RegelKafkaConnector(inMemoryConnector);
      }

      //
      // Have to clear even when connectors are new, since the inMemoryCpnnector is not necessarily
      // empty
      //
      regelKafkaConnector.clear();
   }

   /**
    * Initializes shared test configuration before any tests are executed.
    *
    * <p>This method loads properties from {@code test.properties} on the classpath. It is executed
    * once per test class due to {@link TestInstance.Lifecycle#PER_CLASS}.</p>
    */
   @BeforeAll
   void setup()
   {
      loadTestProperties();
   }

   /**
    * Loads test configuration properties from {@code test.properties}.
    *
    * <p>This method is intended to be invoked once during test initialization.</p>
    *
    * @throws RuntimeException if {@code test.properties} is missing or cannot be read
    */
   protected static void loadTestProperties()
   {
      Properties props = new Properties();
      try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.properties"))
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
}
