package se.fk.rimfrost.framework.regel.logic;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.regel.integration.kafka.dto.ImmutableRegelResponse;
import se.fk.rimfrost.framework.regel.integration.kafka.dto.RegelResponse;
import se.fk.rimfrost.framework.regel.logic.config.RegelConfig;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableErsattning;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableProcessRegelRequest;
import se.fk.rimfrost.framework.regel.logic.dto.ProcessRegelRequest;
import se.fk.rimfrost.framework.regel.logic.entity.CloudEventData;
import se.fk.rimfrost.framework.regel.logic.entity.ErsattningData;
import se.fk.rimfrost.framework.regel.logic.entity.ImmutableRegelResult;
import se.fk.rimfrost.framework.regel.logic.entity.RegelResult;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.Beslutsutfall;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.FSSAinformation;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.ImmutablePatchErsattningRequest;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.ImmutablePutKundbehovsflodeUppgiftRequest;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.ImmutableUpdateKundbehovsflodeErsattning;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.ImmutableUpdateKundbehovsflodeLagrum;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.ImmutableUpdateKundbehovsflodeRegel;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.ImmutableUpdateKundbehovsflodeSpecifikation;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.ImmutableUpdateKundbehovsflodeUnderlag;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.ImmutableUpdateKundbehovsflodeUppgift;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.KundbehovsflodeResponse;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.PatchErsattningRequest;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.PutKundbehovsflodeUppgiftRequest;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.Roll;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.UppgiftStatus;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.Verksamhetslogik;
import se.fk.rimfrost.framework.regel.Utfall;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

   public ProcessRegelRequest toProcessRegelRequest(KundbehovsflodeResponse kundbehovsflodeResponse)
   {
      return ImmutableProcessRegelRequest.builder()
            .kundbehovsflodeId(kundbehovsflodeResponse.kundbehovsflodeId())
            .personnummer(kundbehovsflodeResponse.personnummer())
            .formanstyp(kundbehovsflodeResponse.formanstyp())
            .ersattning(
                  kundbehovsflodeResponse.ersattning()
                        .stream()
                        .map(e -> ImmutableErsattning.builder()
                              .ersattningsId(e.ersattningsId())
                              .ersattningsTyp(e.ersattningsTyp())
                              .omfattningsProcent(e.omfattningsProcent())
                              .belopp(e.belopp())
                              .berakningsgrund(e.berakningsgrund())
                              .beslutsutfall(e.beslutsutfall())
                              .franOchMed(e.franOchMed())
                              .tillOchMed(e.tillOchMed())
                              .build())
                        .collect(Collectors.toList()))
            .build();
   }

   public PutKundbehovsflodeUppgiftRequest toPutKundbehovsflodeRequest(UUID kundbehovsflodeId, RegelResult regelResult,
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
            .id(regelResult.uppgiftId())
            .version(regelConfig.getUppgift().getVersion())
            .skapadTs(regelResult.skapadTs())
            .utfordTs(regelResult.utfordTs())
            .planeradTs(regelResult.planeradTs())
            .uppgiftStatus(mapUppgiftStatus(regelResult.uppgiftStatus()))
            .aktivitet(regelConfig.getUppgift().getAktivitet())
            .fsSAinformation(mapFssaInformation(regelResult.fssaInformation()))
            .specifikation(specifikation)
            .build();

      var requestBuilder = ImmutablePutKundbehovsflodeUppgiftRequest.builder()
            .kundbehovsflodeId(kundbehovsflodeId)
            .uppgift(uppgift);

      for (var rtfUnderlag : regelResult.underlag())
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

   public PatchErsattningRequest toPatchKundbehovsflodeRequest(UUID kundbehovsflodeId, List<ErsattningData> ersattningar)
   {
      var requestBuilder = ImmutablePatchErsattningRequest.builder()
            .kundbehovsflodeId(kundbehovsflodeId);

      for (ErsattningData ersattning : ersattningar)
      {
         var updateErsattning = ImmutableUpdateKundbehovsflodeErsattning.builder()
               .beslutsutfall(mapBeslutsutfall(ersattning.beslutsutfall()))
               .ersattningId(ersattning.id())
               .avslagsanledning(ersattning.avslagsanledning())
               .build();
         requestBuilder.addErsattningar(updateErsattning);
      }

      return requestBuilder.build();
   }
}
