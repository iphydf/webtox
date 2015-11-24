package controllers

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.libs.ws.WS

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

case object PushMessaging {

  private val logger = Logger(LoggerFactory.getLogger(getClass))

  private val serverKey = sys.env("GCM_SERVER_KEY")

  def pushNotification(registrationIds: Iterable[String], title: String, body: String): Unit = {
    if (registrationIds.nonEmpty) {
      logger.debug(s"Sending notification to ${registrationIds.size} subscribers")
      assert(registrationIds.forall(!_.contains('/')))
      val future = WS.url("http://android.googleapis.com/gcm/send")
        .withHeaders("Authorization" -> s"key=$serverKey")
        .post(
          Json.obj(
            "delay_while_idle" -> true,
            "time_to_live" -> 30,
            "notification" -> Json.obj(
              "title" -> title,
              "body" -> body
            ),
            "data" -> Json.obj(
              "login_count" -> registrationIds.size
            ),
            "collapse_key" -> "webtox-push-notification",
            "registration_ids" -> registrationIds
          )
        )
        .map { response =>
          logger.debug(s"Received response from GCM server: $response")
        }

      Await.result(future, 1 minute)
    }
  }

}
