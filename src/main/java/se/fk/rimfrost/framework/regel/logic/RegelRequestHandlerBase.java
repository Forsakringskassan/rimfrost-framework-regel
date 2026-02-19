package se.fk.rimfrost.framework.regel.logic;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;

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
import se.fk.rimfrost.framework.regel.logic.dto.RegelResultRequest;
import se.fk.rimfrost.framework.regel.logic.dto.UppgiftStatus;
import se.fk.rimfrost.framework.regel.logic.entity.*;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;

@SuppressWarnings("unused")
public abstract class RegelRequestHandlerBase implements RegelRequestHandlerInterface
{
   private static final Logger LOGGER = LoggerFactory.getLogger(RegelRequestHandlerBase.class);

   @ConfigProperty(name = "kafka.source")
   private String kafkaSource;

   @Inject
   private RegelMapper regelMapper;

   @Inject
   protected KundbehovsflodeAdapter kundbehovsflodeAdapter;

   @Inject
   private RegelConfigProviderYaml regelConfigProvider;

   @Inject
   private RegelKafkaProducer regelKafkaProducer;

   @Inject
   private RegelServiceInterface regelService;

   private RegelConfig regelConfig;

   @PostConstruct
   void init()
   {
      this.regelConfig = regelConfigProvider.getConfig();
   }

   @Override
   public void handleRegelRequest(RegelResultRequest request)
   {
      var cloudevent = createCloudEvent(request);

      var kundbehovsResponse = kundbehovsflodeAdapter.getKundbehovsflodeInfo(
            ImmutableKundbehovsflodeRequest.builder().kundbehovsflodeId(request.kundbehovsflodeId()).build());

      var regelResult = regelService.processRegel(regelMapper.toProcessRegelRequest(kundbehovsResponse));

      updateKundbehovsFlode(request.kundbehovsflodeId(), regelResult);
      sendResponse(request.kundbehovsflodeId(), cloudevent, regelResult.utfall());
   }

   private CloudEventData createCloudEvent(RegelResultRequest request)
   {
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

   private void sendResponse(UUID kundbehovsflodeId, CloudEventData cloudEventData, Utfall utfall)
   {
      var regelResponse = regelMapper.toRegelResponse(kundbehovsflodeId, cloudEventData, utfall);
      regelKafkaProducer.sendRegelResponse(regelResponse);
   }

   private void updateKundbehovsFlode(UUID kundbehovsflodeId, RegelResult regelResult)
   {
      var patchRequest = regelMapper.toPatchKundbehovsflodeRequest(kundbehovsflodeId, regelResult);
      var putRequest = regelMapper.toPutKundbehovsflodeRequest(kundbehovsflodeId, regelResult, regelConfig);
      kundbehovsflodeAdapter.patchKundbehovsflode(patchRequest);
      kundbehovsflodeAdapter.putKundbehovsflode(putRequest);
   }

}
