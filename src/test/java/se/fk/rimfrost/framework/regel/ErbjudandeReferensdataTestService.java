package se.fk.rimfrost.framework.regel;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.referensdata.ErbjudandeReferensdataInterface;

/**
 * Shared CDI test fixture implementing {@link ErbjudandeReferensdataInterface}. Consumers should
 * assert against {@link #DEFAULT_ERBJUDANDE_NAMN} rather than the literal string so the return
 * value can be changed in one place.
 *
 * <p>Registered as {@link DefaultBean} so any regel implementation that provides its own
 * production-scope bean can override it.</p>
 */
@ApplicationScoped
@DefaultBean
public class ErbjudandeReferensdataTestService implements ErbjudandeReferensdataInterface
{
   /**
    * Constant erbjudande name returned by this test fixture. Tests that assert on the returned
    * value should reference this constant.
    */
   public static final String DEFAULT_ERBJUDANDE_NAMN = "Test erbjudande";

   /**
    * Returns a fixed erbjudande name regardless of the supplied id.
    *
    * @param id ignored
    * @return {@link #DEFAULT_ERBJUDANDE_NAMN}
    */
   @Override
   public String getErbjudandeNamn(String id)
   {
      return DEFAULT_ERBJUDANDE_NAMN;
   }
}
