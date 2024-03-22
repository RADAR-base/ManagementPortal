
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.language.postfixOps
/**
  * Performance test for the Project entity.
  */
class ProjectGatlingTest extends ManagementPortalSimulation {

    override val scn: ScenarioBuilder = scenario("Test the Project entity")
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
      exec(http("Get all projects")
        .get("/api/projects")
        .headers(headers_http_authenticated)
        .check(status.is(200)))
        .pause(5 seconds, 10 seconds)
        .feed(randomString)
        .exec(http("Create new project")
          .post("/api/projects")
          .headers(headers_http_authenticated)
          .body(StringBody("""{"id":null, "projectName":"PROJECT-${randstring}", "description":"SAMPLE_TEXT", "organization":"SAMPLE_TEXT", "location":"SAMPLE_TEXT", "startDate":"2020-01-01T00:00:00.000Z", "projectStatus":null, "endDate":"2020-01-01T00:00:00.000Z", "projectAdmin":null}""")).asJson
          .check(status.is(201))
          .check(headerRegex("Location", "(.*)").saveAs("new_project_url"))).exitHereIfFailed
        .pause(5)
        .repeat(5) {
            exec(http("Get created project")
              .get("${new_project_url}")
              .headers(headers_http_authenticated)
              .check(status.is(200)))
              .pause(5)
        }
        .exec(http("Delete created project")
          .delete("${new_project_url}")
          .headers(headers_http_authenticated)
          .check(status.is(200)))
        .pause(5)
    }

    run()
}
