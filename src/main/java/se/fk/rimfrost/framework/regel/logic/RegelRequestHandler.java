package se.fk.rimfrost.framework.regel.logic;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.KundbehovsflodeAdapter;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.ImmutableKundbehovsflodeRequest;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.integration.config.RegelConfigProviderYaml;
import se.fk.rimfrost.framework.regel.integration.kafka.RegelKafkaProducer;
import se.fk.rimfrost.framework.regel.logic.config.RegelConfig;
import se.fk.rimfrost.framework.regel.logic.dto.Beslutsutfall;
import se.fk.rimfrost.framework.regel.logic.dto.FSSAinformation;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.dto.UppgiftStatus;
import se.fk.rimfrost.framework.regel.logic.entity.*;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;

@SuppressWarnings("unused")
@ApplicationScoped
public class RegelRequestHandler implements RegelRequestHandlerInterface
{
   private static final Logger LOGGER = LoggerFactory.getLogger(RegelRequestHandler.class);

   @ConfigProperty(name = "kafka.source")
   private String kafkaSource;

   @Inject
   protected RegelMapper regelMapper;

   @Inject
   protected KundbehovsflodeAdapter kundbehovsflodeAdapter;

   @Inject
   protected RegelConfigProviderYaml regelConfigProvider;

   @Inject
   protected RegelKafkaProducer regelKafkaProducer;

   @Inject
   private RegelServiceInterface regelService;

   protected RegelConfig regelConfig;

   @PostConstruct
   void init()
   {
      this.regelConfig = regelConfigProvider.getConfig();
   }

   @Override
   public void handleRegelRequest(RegelDataRequest request) {
      var kundbehovsResponse = kundbehovsflodeAdapter.getKundbehovsflodeInfo(ImmutableKundbehovsflodeRequest.builder().kundbehovsflodeId(request.kundbehovsflodeId()).build());
      
      var processRegelResponse = regelService.processRegel(kundbehovsResponse);

      var cloudevent = createCloudEvent(request);

      var regelData = ImmutableRegelData.builder()
                  .kundbehovsflodeId(request.kundbehovsflodeId())
                  .skapadTs(OffsetDateTime.now())
                  .planeradTs(OffsetDateTime.now())
                  .uppgiftStatus(UppgiftStatus.AVSLUTAD)
                  .fssaInformation(FSSAinformation.HANDLAGGNING_PAGAR)
                  .ersattningar(processRegelResponse.ersattningar())
                  .underlag(processRegelResponse.underlag())
                  .build();

      updateKundbehovsFlode(regelData);
      sendResponse(regelData, cloudevent, decideUtfall(regelData));
   }
  
   private CloudEventData createCloudEvent(RegelDataRequest request) {
          return ImmutableCloudEventData.builder()
            .id(request.id())
            .kogitoparentprociid(request.kogitoparentprociid())
            .kogitoprocid(request.kogitoprocid())
            .kogitoprocinstanceid(request.kogitoprocinstanceid())
            .kogitoprocist(request.kogitoprocist())
            .kogitoprocversion(request.kogitoprocversion())
            .kogitorootprocid(request.kogitorootprocid())
            .kogitorootprociid(request.kogitorootprociid())
            .type(request.type())
            .source(kafkaSource)
            .build();
   }

   private void sendResponse(RegelData regelData, CloudEventData cloudEventData, Utfall utfall)
   {
      var regelResponse = regelMapper.toRegelResponse(regelData.kundbehovsflodeId(), cloudEventData, utfall);
      regelKafkaProducer.sendRegelResponse(regelResponse);
   }

   private void updateKundbehovsFlode(RegelData regelData) {
      kundbehovsflodeAdapter.updateKundbehovsflodeInfo(regelMapper.toUpdateKundbehovsflodeRequest(regelData, regelConfig));
   }

   private Utfall decideUtfall(RegelData regelData)
   {
      return regelData.ersattningar().stream().allMatch(e -> e.beslutsutfall() == Beslutsutfall.JA) ? Utfall.JA : Utfall.NEJ;
   }

}
