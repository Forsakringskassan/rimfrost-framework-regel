package se.fk.rimfrost.framework.regel.logic;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.handlaggning.adapter.dto.*;
import se.fk.rimfrost.framework.regel.integration.kafka.dto.ImmutableRegelResponse;
import se.fk.rimfrost.framework.regel.integration.kafka.dto.RegelResponse;
import se.fk.rimfrost.framework.regel.logic.config.RegelConfig;
import se.fk.rimfrost.framework.regel.logic.entity.CloudEventData;
import se.fk.rimfrost.framework.regel.logic.entity.ErsattningData;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.logic.entity.Underlag;
import se.fk.rimfrost.framework.regel.logic.entity.UppgiftData;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
@ApplicationScoped
public class RegelMapper
{

   public RegelResponse toRegelResponse(UUID handlaggningId, CloudEventData cloudevent, Utfall utfall)
   {
      return ImmutableRegelResponse.builder()
            .id(cloudevent.id())
            .handlaggningId(handlaggningId)
            .kogitoparentprociid(cloudevent.kogitoparentprociid())
            .kogitorootprociid(cloudevent.kogitorootprociid())
            .kogitoprocid(cloudevent.kogitoprocid())
            .kogitorootprocid(cloudevent.kogitorootprocid())
            .kogitoprocinstanceid(cloudevent.kogitoprocinstanceid())
            .kogitoprocist(cloudevent.kogitoprocist())
            .kogitoprocversion(cloudevent.kogitoprocversion())
            .utfall(utfall)
            .type(cloudevent.type())
            .source(cloudevent.source())
            .build();
   }

   public PutHandlaggningUppgiftRequest toPutHandlaggningRequest(UUID handlaggningId, UppgiftData uppgiftData,
         List<Underlag> uppgiftUnderlag,
         RegelConfig regelConfig)
   {
      var lagrum = ImmutableUpdateHandlaggningLagrum.builder()
            .id(regelConfig.getLagrum().getId())
            .version(regelConfig.getLagrum().getVersion())
            .forfattning(regelConfig.getLagrum().getForfattning())
            .giltigFrom(regelConfig.getLagrum().getGiltigFom().toInstant().atOffset(ZoneOffset.UTC))
            .kapitel(regelConfig.getLagrum().getKapitel())
            .paragraf(regelConfig.getLagrum().getParagraf())
            .stycke(regelConfig.getLagrum().getStycke())
            .punkt(regelConfig.getLagrum().getPunkt())
            .build();

      var regel = ImmutableUpdateHandlaggningRegel.builder()
            .id(regelConfig.getRegel().getId())
            .beskrivning(regelConfig.getRegel().getBeskrivning())
            .namn(regelConfig.getRegel().getNamn())
            .version(regelConfig.getRegel().getVersion())
            .lagrum(lagrum)
            .build();

      var specifikation = ImmutableUpdateHandlaggningSpecifikation.builder()
            .id(regelConfig.getSpecifikation().getId())
            .version(regelConfig.getSpecifikation().getVersion())
            .namn(regelConfig.getSpecifikation().getNamn())
            .uppgiftsbeskrivning(regelConfig.getSpecifikation().getUppgiftbeskrivning())
            .verksamhetslogik(mapVerksamhetslogik(regelConfig.getSpecifikation().getVerksamhetslogik()))
            .roll(mapRoll(regelConfig.getSpecifikation().getRoll()))
            .applikationsId(regelConfig.getSpecifikation().getApplikationsId())
            .applikationsversion(regelConfig.getSpecifikation().getApplikationsversion())
            .url(regelConfig.getUppgift().getPath())
            .regel(regel)
            .build();

      var uppgift = ImmutableUpdateHandlaggningUppgift.builder()
            .id(uppgiftData.uppgiftId())
            .version(regelConfig.getUppgift().getVersion())
            .skapadTs(uppgiftData.skapadTs())
            .utfordTs(uppgiftData.utfordTs())
            .planeradTs(uppgiftData.planeradTs())
            .uppgiftStatus(mapUppgiftStatus(uppgiftData.uppgiftStatus()))
            .aktivitet(regelConfig.getUppgift().getAktivitet())
            .fsSAinformation(mapFssaInformation(uppgiftData.fssaInformation()))
            .specifikation(specifikation)
            .utforarId(uppgiftData.utforarId())
            .build();

      var requestBuilder = ImmutablePutHandlaggningUppgiftRequest.builder()
            .handlaggningId(handlaggningId)
            .uppgift(uppgift);

      for (var rtfUnderlag : uppgiftUnderlag)
      {
         var underlag = ImmutableUpdateHandlaggningUnderlag.builder()
               .typ(rtfUnderlag.typ())
               .version(rtfUnderlag.version())
               .data(rtfUnderlag.data())
               .build();
         requestBuilder.addUnderlag(underlag);
      }

      return requestBuilder.build();
   }

   private Beslutsutfall mapBeslutsutfall(se.fk.rimfrost.framework.regel.logic.dto.Beslutsutfall beslutsutfall) {
      return switch(beslutsutfall) {
            case JA -> Beslutsutfall.JA;
            case NEJ -> Beslutsutfall.NEJ;
            case FU -> Beslutsutfall.FU;
            default -> throw new InternalError("Could not map beslutsutfall: " + beslutsutfall);
      };
}

   private Roll mapRoll(String roll)
   {
      return switch(roll){case"AGARE"->Roll.AGARE;case"ANSVARIG_HANDLAGGARE"->Roll.ANSVARIG_HANDLAGGARE;case"DJUR"->Roll.DJUR;default->throw new InternalError("Could not map roll: "+roll);};
   }

   private Verksamhetslogik mapVerksamhetslogik(String verksamhetslogik)
   {
      return switch(verksamhetslogik){case"A"->Verksamhetslogik.A;case"B"->Verksamhetslogik.B;case"C"->Verksamhetslogik.C;default->throw new InternalError("Could not map verksamhetslogik: "+verksamhetslogik);};
   }

   private UppgiftStatus mapUppgiftStatus(se.fk.rimfrost.framework.regel.logic.dto.UppgiftStatus uppgiftStatus) {
          return switch (uppgiftStatus) {
           case TILLDELAD -> UppgiftStatus.TILLDELAD;
           case AVSLUTAD -> UppgiftStatus.AVSLUTAD;
           case PLANERAD -> UppgiftStatus.PLANERAD;
           default -> throw new InternalError("Could not map UppgiftStatus: " + uppgiftStatus);
       };
      }

   private FSSAinformation mapFssaInformation(se.fk.rimfrost.framework.regel.logic.dto.FSSAinformation fssaInformation) {
            return switch (fssaInformation) {
            case HANDLAGGNING_PAGAR -> FSSAinformation.HANDLAGGNING_PAGAR;
            case VANTAR_PA_INFO_FRAN_ANNAN_PART -> FSSAinformation.VANTAR_PA_INFO_FRAN_ANNAN_PART;
            case VANTAR_PA_INFO_FRAN_KUND -> FSSAinformation.VANTAR_PA_INFO_FRAN_KUND;
            default -> throw new InternalError("Could not map fssaInformation: " + fssaInformation);
        };
}

   public PatchErsattningRequest toPatchHandlaggningRequest(UUID handlaggningId, List<ErsattningData> ersattningar)
   {
      var requestBuilder = ImmutablePatchErsattningRequest.builder()
            .handlaggningId(handlaggningId);

      for (ErsattningData ersattning : ersattningar)
      {
         var updateErsattning = ImmutableUpdateHandlaggningErsattning.builder()
               .beslutsutfall(mapBeslutsutfall(ersattning.beslutsutfall()))
               .ersattningId(ersattning.id())
               .avslagsanledning(ersattning.avslagsanledning())
               .build();
         requestBuilder.addErsattningar(updateErsattning);
      }

      return requestBuilder.build();
   }
}
