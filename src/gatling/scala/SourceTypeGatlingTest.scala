
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import scala.language.postfixOps

import scala.concurrent.duration._

/**
  * Performance test for the SourceType entity.
  */
class SourceTypeGatlingTest extends ManagementPortalSimulation {
    override val scn: ScenarioBuilder = scenario("Test the SourceType entity")
      .exec(http("First unauthenticated request")
        .get("/api/account")
        .headers(headers_http)
        .check(status.is(401))).exitHereIfFailed
        .tryMax(5) {
          pause(5)
          .exec(http("Authentication")
            .post("/oauth/token")
            .headers(headers_http_authentication)
            .formParam("username", "admin")
            .formParam("password", "admin")
            .formParam("grant_type", "password")
            .formParam("client_secret", "my-secret-token-to-change-in-production")
            .formParam("client_id", "ManagementPortalapp")
            .formParam("submit", "Login")
            .check(jsonPath("$.access_token").saveAs("access_token"))).exitHereIfFailed
          .pause(1)
          .exec(http("Authenticated request")
            .get("/api/account")
            .headers(headers_http_authenticated)
            .check(status.is(200)))
        }
      .pause(5)
      .repeat(2) {
          exec(http("Get all sourceTypes")
            .get("/api/source-types")
            .headers(headers_http_authenticated)
            .check(status.is(200)))
            .pause(5 seconds, 10 seconds)
            .feed(randomString)
            .exec(http("Create new sourceType")
              .post("/api/source-types")
              .headers(headers_http_authenticated)
              .body(StringBody("""{"id":null, "producer":"GATLING", "model":"MODEL-${randstring}", "catalogVersion":"v1", "sourceTypeScope": "ACTIVE"}""")).asJson
              .check(status.is(201))
              .check(headerRegex("Location", "(.*)").saveAs("new_sourceType_url"))).exitHereIfFailed
            .pause(5)
            .repeat(5) {
                exec(http("Get created sourceType")
                  .get("${new_sourceType_url}")
                  .headers(headers_http_authenticated)
                  .check(status.is(200)))
                  .pause(5)
            }
            .exec(http("Delete created sourceType")
              .delete("${new_sourceType_url}")
              .headers(headers_http_authenticated)
              .check(status.is(200)))
            .pause(5)
      }

    run()
}
