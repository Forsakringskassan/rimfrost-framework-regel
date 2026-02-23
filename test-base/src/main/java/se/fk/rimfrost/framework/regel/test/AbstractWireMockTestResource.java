package se.fk.rimfrost.framework.regel.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public abstract class AbstractWireMockTestResource implements QuarkusTestResourceLifecycleManager
{

   private WireMockServer wireMockServer;

   @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Test infrastructure exposes WireMockServer intentionally")
   public WireMockServer getWireMockServer()
   {
      return wireMockServer;
   }

   protected abstract Map<String, String> getProperties();

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
