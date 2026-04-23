package se.fk.rimfrost.framework.regel.logic.config;

@SuppressWarnings("unused")
public class UtokadUppgiftsbeskrivning
{
   private String beskrivning;

   public UtokadUppgiftsbeskrivning()
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
