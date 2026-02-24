package se.fk.rimfrost.framework.regel.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public abstract class AbstractWireMockTestResource implements QuarkusTestResourceLifecycleManager
{

   private static WireMockServer wireMockServer;

   @SuppressFBWarnings(value = "MS_EXPOSE_REP", justification = "Static WireMockServer is intentional test infrastructure")
   public static WireMockServer getWireMockServer()
   {
      return wireMockServer;
   }

   protected abstract Map<String, String> getProperties();

   @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "Static lifecycle managed intentionally by Quarkus test resource")
   @Override
   public Map<String, String> start()
   {
      wireMockServer = new WireMockServer(
            options()
                  .dynamicPort()
                  .usingFilesUnderDirectory("src/test/resources"));
      wireMockServer.start();
      return getProperties();
   }

   @Override
   public void stop()
   {
      if (wireMockServer != null)
      {
         wireMockServer.stop();
      }
   }
}
