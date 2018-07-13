
import java.nio.charset.StandardCharsets
import java.util.Base64

import ch.qos.logback.classic.LoggerContext
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.util.Random

/**
  * Performance test for the Source entity.
  */
class SourceGatlingTest extends Simulation {

    val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    // Log all HTTP requests
    //context.getLogger("io.gatling.http").setLevel(Level.valueOf("TRACE"))
    // Log failed HTTP requests
    //context.getLogger("io.gatling.http").setLevel(Level.valueOf("DEBUG"))

    val baseURL = Option(System.getProperty("baseURL")) getOrElse """http://127.0.0.1:8080"""

    val httpConf = http
      .baseURL(baseURL)
      .inferHtmlResources()
      .acceptHeader("*/*")
      .acceptEncodingHeader("gzip, deflate")
      .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
      .connectionHeader("keep-alive")
      .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:33.0) Gecko/20100101 Firefox/33.0")

    val headers_http = Map(
        "Accept" -> """application/json"""
    )

    val authorization_header = "Basic " + Base64.getEncoder.encodeToString("ManagementPortalapp:my-secret-token-to-change-in-production".getBytes(StandardCharsets.UTF_8))

    val headers_http_authentication = Map(
        "Content-Type" -> """application/x-www-form-urlencoded""",
        "Accept" -> """application/json""",
        "Authorization"-> authorization_header
    )

    val headers_http_authenticated = Map(
        "Accept" -> """application/json""",
        "Authorization" -> "Bearer ${access_token}"
    )

    val randomString = Iterator.continually(Map("randstring" -> ( Random.alphanumeric.take(12).mkString )))// length of the random string is 12 chars here

    val scn = scenario("Test the Source entity")
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
      .feed(randomString)
      .exec(http("Create new sourceType")
        .post("/api/source-types")
        .headers(headers_http_authenticated)
        .body(StringBody("""{"id":null, "producer":"GATLING", "model":"MODEL-${randstring}", "catalogVersion":"v1", "sourceTypeScope": "ACTIVE"}""")).asJSON
        .check(status.is(201))
        .check(headerRegex("Location", "(.*)").saveAs("new_sourceType_url"))
        .check(jsonPath("$.id").saveAs("sourceTypeId"))).exitHereIfFailed
      .pause(5)
      .repeat(2) {
          exec(http("Get all sources")
            .get("/api/sources")
            .headers(headers_http_authenticated)
            .check(status.is(200)))
            .pause(5 seconds, 10 seconds)
            .feed(randomString)
            .exec(http("Create new source")
              .post("/api/sources")
              .headers(headers_http_authenticated)
              .body(StringBody("""{"id":null, "sourceName":"SOURCE-${randstring}", "assigned":false, "sourceType":{"id":"${sourceTypeId}"}}""")).asJSON
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

    val users = scenario("Users").exec(scn)

    setUp(
        users.inject(rampUsers(100) over (1 minute))
    ).protocols(httpConf)
}
