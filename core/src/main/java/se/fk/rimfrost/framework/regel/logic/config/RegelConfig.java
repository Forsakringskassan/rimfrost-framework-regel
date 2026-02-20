package se.fk.rimfrost.framework.regel.logic.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("unused")
public class RegelConfig
{

   private Uppgift uppgift;
   private Specifikation specifikation;
   private Regel regel;
   private Lagrum lagrum;
   private UtokadUppgiftsbeskrivning utokadUppgiftsbeskrivning;

   public RegelConfig()
   {
   }

   @SuppressFBWarnings("EI_EXPOSE_REP2")
   public RegelConfig(Uppgift uppgift, Specifikation specifikation, Regel regel, Lagrum lagrum)
   {
      this.uppgift = uppgift;
      this.specifikation = specifikation;
      this.regel = regel;
      this.lagrum = lagrum;
   }

   @SuppressFBWarnings("EI_EXPOSE_REP")
   public Uppgift getUppgift()
   {
      return uppgift;
   }

   @SuppressFBWarnings("EI_EXPOSE_REP")
   public void setUppgift(Uppgift uppgift)
   {
      this.uppgift = uppgift;
   }

   @SuppressFBWarnings("EI_EXPOSE_REP")
   public Specifikation getSpecifikation()
   {
      return specifikation;
   }

   @SuppressFBWarnings("EI_EXPOSE_REP")
   public void setSpecifikation(Specifikation specifikation)
   {
      this.specifikation = specifikation;
   }

   @SuppressFBWarnings("EI_EXPOSE_REP")
   public Regel getRegel()
   {
      return regel;
   }

   @SuppressFBWarnings("EI_EXPOSE_REP")
   public void setRegel(Regel regel)
   {
      this.regel = regel;
   }

   @SuppressFBWarnings("EI_EXPOSE_REP")
   public Lagrum getLagrum()
   {
      return lagrum;
   }

   @SuppressFBWarnings("EI_EXPOSE_REP")
   public void setLagrum(Lagrum lagrum)
   {
      this.lagrum = lagrum;
   }

   @SuppressFBWarnings("EI_EXPOSE_REP")
   public UtokadUppgiftsbeskrivning getUtokadUppgiftsbeskrivning()
   {
      return utokadUppgiftsbeskrivning;
   }

   @SuppressFBWarnings("EI_EXPOSE_REP")
   public void setUtokadUppgiftsbeskrivning(UtokadUppgiftsbeskrivning utokadUppgiftsbeskrivning)
   {
      this.utokadUppgiftsbeskrivning = utokadUppgiftsbeskrivning;
   }

}
