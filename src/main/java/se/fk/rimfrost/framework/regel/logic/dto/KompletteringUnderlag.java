package se.fk.rimfrost.framework.regel.logic.dto;

import org.immutables.value.Value;

/**
 * Describes one attribute in the yrkande that is missing or incomplete.
 *
 * <p>Constructed inside {@code checkKomplettering()} — one instance per missing attribute.
 * The list is persisted in {@code KompletteringStorage} and served via
 * {@code GET /{handlaggningId}/komplettering} so the handläggare client can fetch structured
 * information about what is missing. It is not embedded in the OUL task description.
 */
@Value.Immutable
public interface KompletteringUnderlag
{
   /**
    * Machine-readable type identifier matching the corresponding {@code Underlag.typ()} value.
    *
    * <p>Define this string as a local constant in the regel repo — never in this framework.
    */
   String underlagTyp();

   /** Human-readable description shown to the handläggare. */
   String beskrivning();
}
