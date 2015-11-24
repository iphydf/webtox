package controllers.auth

import java.nio.charset.Charset
import java.util.Base64

import com.typesafe.scalalogging.Logger
import im.tox.tox4j.core.data.ToxSecretKey
import im.tox.tox4j.crypto.ToxCryptoConstants
import im.tox.tox4j.impl.jni.ToxCryptoImpl
import models.proto.Savedata
import org.slf4j.LoggerFactory
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

case object AuthenticatedAction extends ActionBuilder[WebToxInstanceRequest] with ActionRefiner[Request, WebToxInstanceRequest] with Results {

  private val CookieKey = "secretKey"
  private val passKey = ToxCryptoImpl.deriveKeyWithSalt(
    "change-me".getBytes(Charset.forName("UTF-8")),
    Array.ofDim(ToxCryptoConstants.SaltLength)
  )

  def setCookie(savedata: Savedata): Session = {
    Session(Map(
      AuthenticatedAction.CookieKey -> Base64.getEncoder.encodeToString(
        ToxCryptoImpl.encrypt(savedata.toByteArray, passKey)
      )
    ))
  }

  def getCookie(session: Session): Option[Savedata] = {
    for {
      cookie <- session.get(CookieKey)
    } yield {
      Savedata.parseFrom(ToxCryptoImpl.decrypt(Base64.getDecoder.decode(cookie), passKey))
    }
  }

  private val logger = Logger(LoggerFactory.getLogger(getClass))

  private var instances = Map.empty[String, WebToxInstance]

  Akka.system.scheduler.schedule(1 second, 500 milliseconds) {
    synchronized {
      instances.values.foreach(_.iterate())
    }
  }

  private def currentInstance(session: Session): Option[(Savedata, WebToxInstance)] = {
    for {
      savedata <- getCookie(session)
    } yield {
      val secretKeyHex = savedata.secretKey
      logger.debug(s"Looking up user session for $secretKeyHex")

      val instance = synchronized {
        instances.get(savedata.secretKey) match {
          case None =>
            // (Re-)create session.
            logger.debug(s"Creating new user session for $secretKeyHex")
            val instance = new WebToxInstance(
              ToxSecretKey.fromHexString(secretKeyHex).getOrElse {
                throw new RuntimeException("Key parsing failed. Database corruption likely.")
              }
            )
            instances += savedata.secretKey -> instance
            instance

          case Some(userSession) =>
            logger.debug("Found existing user session")
            userSession
        }
      }

      // Enable GCM push notifications and/or update the last active time.
      instance.updateSubscription(savedata.subscriptionId, enabled = true)

      (savedata, instance)
    }
  }

  def logout(session: Session): Unit = {
    currentInstance(session).foreach {
      case (savedata, instance) =>
        // Unsubscribe user from GCM notifications.
        instance.updateSubscription(savedata.subscriptionId, enabled = false)
        if (instance.isEmpty) {
          logger.info("No more sessions => user has logged out everywhere. Shutting down Tox instance.")
          synchronized {
            instance.close()
            instances -= savedata.secretKey
          }
        }
    }
  }

  def isLoggedIn(session: Session): Boolean = {
    currentInstance(session).isDefined
  }

  override protected def refine[A](request: Request[A]): Future[Either[Result, WebToxInstanceRequest[A]]] = {
    Future.successful {
      currentInstance(request.session) match {
        case None                       => Left(Unauthorized(Json.obj("failure" -> "Not logged in")))
        case Some((savedata, instance)) => Right(new WebToxInstanceRequest(request, savedata, instance))
      }
    }
  }

}
