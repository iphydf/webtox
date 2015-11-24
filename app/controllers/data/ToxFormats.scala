package controllers.data

import com.typesafe.scalalogging.Logger
import im.tox.core.error.CoreError
import im.tox.core.typesafe.ByteArrayCompanion
import im.tox.tox4j.core.data._
import im.tox.tox4j.core.enums.ToxUserStatus
import org.slf4j.LoggerFactory
import play.api.data.FormError
import play.api.data.format.Formats._
import play.api.data.format.Formatter

import scalaz.\/

case object ToxFormats {

  private val logger = Logger(LoggerFactory.getLogger(getClass))

  private def formatError(key: String, args: String*) = {
    Seq(FormError(key, "error.format", args))
  }

  private def byteArrayFormat[A <: AnyVal](fromString: String => CoreError \/ A): Formatter[A] = new Formatter[A] {

    override def unbind(key: String, value: A): Map[String, String] = {
      stringFormat.unbind(key, value.toString)
    }

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], A] = {
      stringFormat.bind(key, data).right.flatMap { name =>
        fromString(name).leftMap { error =>
          formatError(key, error.toString)
        }.toEither
      }
    }

  }

  private def enumFormat[E <: Enum[E]](valueOf: String => E, values: Array[E]): Formatter[E] = new Formatter[E] {

    override def unbind(key: String, value: E): Map[String, String] = {
      stringFormat.unbind(key, value.toString)
    }

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], E] = {
      stringFormat.bind(key, data).right.flatMap { status =>
        try {
          Right(valueOf(status))
        } catch {
          case exn: IllegalArgumentException =>
            Left(Seq(FormError(key, "error.enum_format", values.mkString(", "))))
        }
      }
    }

  }

  // Text formats.
  implicit def toxNicknameFormat: Formatter[ToxNickname] = byteArrayFormat(ToxNickname.fromString)
  implicit def toxFriendMessageFormat: Formatter[ToxFriendMessage] = byteArrayFormat(ToxFriendMessage.fromString)
  implicit def toxFriendRequestMessageFormat: Formatter[ToxFriendRequestMessage] = byteArrayFormat(ToxFriendRequestMessage.fromString)
  implicit def toxStatusMessageFormat: Formatter[ToxStatusMessage] = byteArrayFormat(ToxStatusMessage.fromString)

  // Byte array formats.
  implicit def toxPublicKeyFormat: Formatter[ToxPublicKey] = byteArrayFormat(ToxPublicKey.fromHexString)
  implicit def toxFriendAddressFormat: Formatter[ToxFriendAddress] = byteArrayFormat(ToxFriendAddress.fromHexString)

  // Enum formats.
  implicit def toxUserStatusFormat: Formatter[ToxUserStatus] = enumFormat(ToxUserStatus.valueOf, ToxUserStatus.values)

}
