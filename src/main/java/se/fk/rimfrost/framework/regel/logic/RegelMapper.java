package se.fk.rimfrost.framework.regel.logic;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.regel.integration.kafka.dto.ImmutableRegelResponse;
import se.fk.rimfrost.framework.regel.integration.kafka.dto.RegelResponse;
import se.fk.rimfrost.framework.regel.logic.config.RegelConfig;
import se.fk.rimfrost.framework.regel.logic.entity.CloudEventData;
import se.fk.rimfrost.framework.regel.logic.entity.ErsattningData;
import se.fk.rimfrost.framework.regel.logic.entity.RegelData;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.*;
import se.fk.rimfrost.framework.regel.Utfall;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.UUID;

@SuppressWarnings("unused")
@ApplicationScoped
public class RegelMapper
{

   public RegelResponse toRegelResponse(UUID kundbehovsflodeId, CloudEventData cloudevent, Utfall utfall)
   {
      return ImmutableRegelResponse.builder()
            .id(cloudevent.id())
            .kundbehovsflodeId(kundbehovsflodeId)
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

   public UpdateKundbehovsflodeRequest toUpdateKundbehovsflodeRequest(RegelData regelData,
         RegelConfig regelConfig)
   {
      var lagrum = ImmutableUpdateKundbehovsflodeLagrum.builder()
            .id(regelConfig.getLagrum().getId())
            .version(regelConfig.getLagrum().getVersion())
            .forfattning(regelConfig.getLagrum().getForfattning())
            .giltigFrom(regelConfig.getLagrum().getGiltigFom().toInstant().atOffset(ZoneOffset.UTC))
            .kapitel(regelConfig.getLagrum().getKapitel())
            .paragraf(regelConfig.getLagrum().getParagraf())
            .stycke(regelConfig.getLagrum().getStycke())
            .punkt(regelConfig.getLagrum().getPunkt())
            .build();

      var regel = ImmutableUpdateKundbehovsflodeRegel.builder()
            .id(regelConfig.getRegel().getId())
            .beskrivning(regelConfig.getRegel().getBeskrivning())
            .namn(regelConfig.getRegel().getNamn())
            .version(regelConfig.getRegel().getVersion())
            .lagrum(lagrum)
            .build();

      var specifikation = ImmutableUpdateKundbehovsflodeSpecifikation.builder()
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

      var uppgift = ImmutableUpdateKundbehovsflodeUppgift.builder()
            .id(regelData.uppgiftId())
            .version(regelConfig.getUppgift().getVersion())
            .skapadTs(regelData.skapadTs())
            .utfordTs(regelData.utfordTs())
            .planeradTs(regelData.planeradTs())
            .utforarId(regelData.utforarId())
            .uppgiftStatus(mapUppgiftStatus(regelData.uppgiftStatus()))
            .aktivitet(regelConfig.getUppgift().getAktivitet())
            .fsSAinformation(mapFssaInformation(regelData.fssaInformation()))
            .specifikation(specifikation)
            .build();

      var requestBuilder = ImmutableUpdateKundbehovsflodeRequest.builder()
            .kundbehovsflodeId(regelData.kundbehovsflodeId())
            .uppgift(uppgift)
            .underlag(new ArrayList<>());

      for (ErsattningData rtfErsattning : regelData.ersattningar())
      {
         var ersattning = ImmutableUpdateKundbehovsflodeErsattning.builder()
               .beslutsutfall(mapBeslutsutfall(rtfErsattning.beslutsutfall()))
               .id(rtfErsattning.id())
               .avslagsanledning(rtfErsattning.avslagsanledning())
               .build();
         requestBuilder.addErsattningar(ersattning);
      }

      for (var rtfUnderlag : regelData.underlag())
      {
         var underlag = ImmutableUpdateKundbehovsflodeUnderlag.builder()
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

   private FSSAinformation mapFssaInformation(se.fk.rimfrost.framework.regel.logic.dto.FSSAinformation fssaInformation) {
            return switch (fssaInformation) {
            case HANDLAGGNING_PAGAR -> FSSAinformation.HANDLAGGNING_PAGAR;
            case VANTAR_PA_INFO_FRAN_ANNAN_PART -> FSSAinformation.VANTAR_PA_INFO_FRAN_ANNAN_PART;
            case VANTAR_PA_INFO_FRAN_KUND -> FSSAinformation.VANTAR_PA_INFO_FRAN_KUND;
            default -> throw new InternalError("Could not map fssaInformation: " + fssaInformation);
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
}
