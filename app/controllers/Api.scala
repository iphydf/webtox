package controllers

import com.google.protobuf.Message
import com.googlecode.protobuf.format.JsonJacksonFormat
import com.trueaccord.scalapb.JavaProtoSupport
import controllers.auth.AuthenticatedAction
import controllers.data.ProtoConversions.toProto
import controllers.data.ToxFormats._
import im.tox.tox4j.core.data.{ToxNickname, ToxStatusMessage}
import im.tox.tox4j.core.enums.ToxUserStatus
import models.proto.SetInfoSuccess
import models.proto.TimedEvent.E.ESetInfoSuccess
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats._
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.language.postfixOps

//noinspection MutatorLikeMethodIsParameterless
case object Api extends Controller {

  private def toJson[ScalaPB, JavaPB <: Message](
    companion: JavaProtoSupport[ScalaPB, JavaPB],
    message: ScalaPB
  ): JsValue = {
    val jsonFormat = new JsonJacksonFormat
    Json.parse(jsonFormat.printToString(companion.toJavaProto(message)))
  }

  def getEvents: Action[Long] = AuthenticatedAction(parse.form(Form("timestamp" -> of[Long]))) { request =>
    val events = request.latestEvents(request.body)

    Ok(Json.obj(
      "success" -> toJson(events.companion, events)
    ))
  }

  final case class SetInfoFormData(
    name: ToxNickname,
    statusMessage: ToxStatusMessage,
    status: ToxUserStatus,
    nospam: Int
  )

  def setInfoForm: Form[SetInfoFormData] = Form(
    mapping(
      "name" -> of[ToxNickname],
      "statusMessage" -> of[ToxStatusMessage],
      "status" -> of[ToxUserStatus],
      "nospam" -> of[Int]
    )(SetInfoFormData.apply)(SetInfoFormData.unapply)
  )

  def setInfo: Action[SetInfoFormData] = {
    AuthenticatedAction(parse.form[SetInfoFormData](
      setInfoForm,
      onErrors = { form => Ok(Json.obj("failure" -> form.errorsAsJson)) }
    )) { request =>
      request.tox.setName(request.body.name)
      request.tox.setStatusMessage(request.body.statusMessage)
      request.tox.setStatus(request.body.status)
      request.tox.setNospam(request.body.nospam)

      request.fireEvent(ESetInfoSuccess(SetInfoSuccess(
        request.body.name.toString,
        request.body.statusMessage.toString,
        toProto(request.body.status),
        request.body.nospam
      )))

      Ok(Json.obj(
        "success" -> true
      ))
    }
  }

}
