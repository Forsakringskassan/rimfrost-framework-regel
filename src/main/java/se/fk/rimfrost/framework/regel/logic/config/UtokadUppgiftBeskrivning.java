package se.fk.rimfrost.framework.regel.logic.config;

@SuppressWarnings("unused")
public class UtokadUppgiftBeskrivning
{
   private String beskrivning;

   public UtokadUppgiftBeskrivning()
   {
      // required by SnakeYAML
   }

   public String getBeskrivning()
   {
      return beskrivning;
   }

   public void setBeskrivning(String beskrivning)
   {
      this.beskrivning = beskrivning;
   }
}
