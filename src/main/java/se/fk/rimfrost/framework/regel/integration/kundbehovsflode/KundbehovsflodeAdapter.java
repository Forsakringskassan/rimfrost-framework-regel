package se.fk.rimfrost.framework.regel.integration.kundbehovsflode;

import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.jaxrsclientfactory.JaxrsClientFactory;
import se.fk.github.jaxrsclientfactory.JaxrsClientOptionsBuilders;
import se.fk.rimfrost.framework.regel.integration.kundbehovsflode.dto.KundbehovsflodeRequest;
import se.fk.rimfrost.framework.regel.integration.kundbehovsflode.dto.KundbehovsflodeResponse;
import se.fk.rimfrost.framework.regel.integration.kundbehovsflode.dto.UpdateKundbehovsflodeRequest;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.KundbehovsflodeControllerApi;
import static io.quarkus.arc.impl.UncaughtExceptions.LOGGER;

@SuppressWarnings("unused")
@ApplicationScoped
public class KundbehovsflodeAdapter
{

   @ConfigProperty(name = "kundbehovsflode.api.base-url")
   String kundbehovsflodeBaseUrl;

   private KundbehovsflodeControllerApi kundbehovsClient;

   @Inject
   KundbehovsflodeMapper kundbehovsflodemapper;

   @PostConstruct
   void init()
   {
      this.kundbehovsClient = new JaxrsClientFactory()
            .create(JaxrsClientOptionsBuilders.createClient(kundbehovsflodeBaseUrl, KundbehovsflodeControllerApi.class)
                  .build());
   }

   public KundbehovsflodeResponse getKundbehovsflodeInfo(KundbehovsflodeRequest kundbehovsflodeRequest)
   {
      var apiResponse = kundbehovsClient.getKundbehovsflode(kundbehovsflodeRequest.kundbehovsflodeId());
      return kundbehovsflodemapper.toKundbehovsflodeResponse(apiResponse);
   }

   public void updateKundbehovsflodeInfo(UpdateKundbehovsflodeRequest request)
   {
      var apiResponse = kundbehovsClient.getKundbehovsflode(request.kundbehovsflodeId());

      var apiRequest = kundbehovsflodemapper.toKundbehovsflodeRequest(request, apiResponse);
      LOGGER.info("updateKundbehovsflodeInfo " + request);
      try
      {
         kundbehovsClient.putKundbehovsflode(request.kundbehovsflodeId(), apiRequest);
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         throw t;
      }
      LOGGER.info("putKundbehovsflode executed");
   }
}
