
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._

/**
  * Performance test for the Source entity.
  */
class SourceGatlingTest extends ManagementPortalSimulation {
    override val scn: ScenarioBuilder = scenario("Test the Source entity")
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
      .feed(randomString)
      .exec(http("Create new sourceType")
        .post("/api/source-types")
        .headers(headers_http_authenticated)
        .body(StringBody("""{"id":null, "producer":"GATLING", "model":"MODEL-${randstring}", "catalogVersion":"v1", "sourceTypeScope": "ACTIVE"}""")).asJson
        .check(status.is(201))
        .check(headerRegex("Location", "(.*)").saveAs("new_sourceType_url"))
        .check(jsonPath("$.id").saveAs("sourceTypeId"))).exitHereIfFailed
      .pause(5)
      .repeat(2) {
          exec(http("Get all sources")
            .get("/api/sources")
            .headers(headers_http_authenticated)
            .check(status.is(200)))
            .pause(5.seconds, 10.seconds)
            .feed(randomString)
            .exec(http("Create new source")
              .post("/api/sources")
              .headers(headers_http_authenticated)
              .body(StringBody("""{"id":null, "sourceName":"SOURCE-${randstring}", "assigned":false, "sourceType":{"id":"${sourceTypeId}"}}""")).asJson
              .check(status.is(201))
              .check(headerRegex("Location", "(.*)").saveAs("new_source_url"))).exitHereIfFailed
            .pause(5)
            .repeat(5) {
                exec(http("Get created source")
                  .get("${new_source_url}")
                  .headers(headers_http_authenticated)
                  .check(status.is(200)))
                  .pause(5)
            }
            .exec(http("Delete created source")
              .delete("${new_source_url}")
              .headers(headers_http_authenticated)
              .check(status.is(200)))
            .pause(5)
      }
      .exec(http("Delete created source type")
        .delete("${new_sourceType_url}")
        .headers(headers_http_authenticated)
        .check(status.is(200)))

    run()
}
