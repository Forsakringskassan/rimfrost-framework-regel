package se.fk.rimfrost.framework.regel.integration.kundbehovsflode.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;

@Value.Immutable
public interface UpdateKundbehovsflodeLagrum
{

   UUID id();

   String version();

   OffsetDateTime giltigFrom();

   @Nullable
   OffsetDateTime giltigTom();

   String forfattning();

   String kapitel();

   String paragraf();

   String stycke();

   String punkt();
}
