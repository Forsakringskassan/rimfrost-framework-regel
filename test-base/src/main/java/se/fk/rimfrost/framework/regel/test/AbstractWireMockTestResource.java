package se.fk.rimfrost.framework.regel.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public abstract class AbstractWireMockTestResource implements QuarkusTestResourceLifecycleManager
{

   private WireMockServer wireMockServer;

   @SuppressWarnings("EI_EXPOSE_REP")
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
