package se.fk.rimfrost.framework.regel;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.is;

/**
 * Verifies that the framework health endpoint reports the service as operational.
 */
@QuarkusTest
public class RegelHealthTest
{

   @Test
   @DisplayName("FRALL-NFR-01.1: Health endpoint returnerar UP")
   void health_endpoint_should_return_up()
   {
      when()
            .get("/q/health/live")
            .then()
            .statusCode(200)
            .body("status", is("UP"));
   }
}
