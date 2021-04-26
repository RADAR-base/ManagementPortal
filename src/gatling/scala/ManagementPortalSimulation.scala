import java.nio.charset.StandardCharsets
import java.util.Base64

import ch.qos.logback.classic.LoggerContext
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef.http
import io.gatling.http.protocol.HttpProtocolBuilder
import org.slf4j.LoggerFactory
import scala.concurrent.duration._

import scala.util.Random

abstract class ManagementPortalSimulation extends Simulation {
    val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    // Log all HTTP requests
    //context.getLogger("io.gatling.http").setLevel(Level.valueOf("TRACE"))
    // Log failed HTTP requests
    //context.getLogger("io.gatling.http").setLevel(Level.valueOf("DEBUG"))

    val baseUrl: String = Option(System.getProperty("baseUrl")) getOrElse """http://127.0.0.1:8080"""

    val httpConf: HttpProtocolBuilder = http.baseUrl(baseUrl)
            .inferHtmlResources()
            .acceptHeader("*/*")
            .acceptEncodingHeader("gzip, deflate")
            .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
            .connectionHeader("keep-alive")
            .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:33.0) Gecko/20100101 Firefox/33.0")

    val headers_http = Map(
        "Accept" -> """application/json"""
    )

    val authorization_header: String = "Basic " + Base64.getEncoder.encodeToString("ManagementPortalapp:my-secret-token-to-change-in-production".getBytes(StandardCharsets.UTF_8))

    val headers_http_authentication = Map(
        "Content-Type" -> """application/x-www-form-urlencoded""",
        "Accept" -> """application/json""",
        "Authorization"-> authorization_header
    )

    val headers_http_authenticated = Map(
        "Accept" -> """application/json""",
        "Authorization" -> "Bearer ${access_token}"
    )

    val randomString: Iterator[Map[String, String]] = Iterator.continually(Map("randstring" -> Random.alphanumeric.take(12).mkString))// length of the random string is 12 chars here

    def scn : ScenarioBuilder

    def run(): Unit = {
        val users = scenario("Users").exec(scn)
        setUp(
            users.inject(rampUsers(100) during (1 minutes))
        ).protocols(httpConf)
    }
}
