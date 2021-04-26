
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._

/**
  * Performance test for the SourceData entity.
  */
class SensorDataGatlingTest extends ManagementPortalSimulation {

    override val scn: ScenarioBuilder = scenario("Test the SourceData entity")
      .exec(http("First unauthenticated request")
        .get("/api/account")
        .headers(headers_http)
        .check(status.is(401))).exitHereIfFailed
      .pause(5)
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
      .pause(5)
      .repeat(2) {
          exec(http("Get all sourceData")
            .get("/api/source-data")
            .headers(headers_http_authenticated)
            .check(status.is(200)))
            .pause(5 seconds, 10 seconds)
            .feed(randomString)
            .exec(http("Create new sourceData")
              .post("/api/source-data")
              .headers(headers_http_authenticated)
              .body(StringBody("""{"id":null, "sourceDataType":"SAMPLE_TEXT", "sourceDataName":"SOURCEDATA-${randstring}", "processingState":null, "keySchema":"SAMPLE_TEXT", "frequency":"SAMPLE_TEXT"}""")).asJson
              .check(status.is(201))
              .check(headerRegex("Location", "(.*)").saveAs("new_sourceData_url"))).exitHereIfFailed
            .pause(5)
            .repeat(5) {
                exec(http("Get created sourceData")
                  .get("${new_sourceData_url}")
                  .headers(headers_http_authenticated)
                  .check(status.is(200)))
                  .pause(5)
            }
            .exec(http("Delete created sourceData")
              .delete("${new_sourceData_url}")
              .headers(headers_http_authenticated)
              .check(status.is(200)))
            .pause(5)
      }

    run()
}
