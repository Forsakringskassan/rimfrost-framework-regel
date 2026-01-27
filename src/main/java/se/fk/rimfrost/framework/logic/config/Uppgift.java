package se.fk.rimfrost.framework.logic.config;

@SuppressWarnings("unused")
public class Uppgift
{
   private String version;
   private String path;
   private String aktivitet;

   public Uppgift()
   {
      // required by SnakeYAML
   }

   public String getVersion()
   {
      return version;
   }

   public void setVersion(String version)
   {
      this.version = version;
   }

   public String getAktivitet()
   {
      return aktivitet;
   }

   public void setAktivitet(String aktivitet)
   {
      this.aktivitet = aktivitet;
   }

   public String getPath()
   {
      return path;
   }

   public void setPath(String path)
   {
      this.path = path;
   }

}
