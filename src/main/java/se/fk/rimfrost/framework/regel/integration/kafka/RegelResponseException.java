package se.fk.rimfrost.framework.regel.integration.kafka;

public class RegelResponseException extends RuntimeException
{
   public RegelResponseException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
