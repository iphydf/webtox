package controllers

import java.util.Base64

import controllers.auth.AuthenticatedAction
import controllers.data.WebToxDB
import im.tox.tox4j.core.options.ToxOptions
import im.tox.tox4j.impl.jni.ToxCoreImpl
import models.proto.Savedata
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.db.DB
import play.api.libs.json.Json
import play.api.mvc._

case object Authentication extends Controller {

  final case class LoginFormData(
    username: String,
    password: String,
    endpoint: String
  )

  val loginForm = Form(
    mapping(
      "username" -> text,
      "password" -> text,
      "endpoint" -> text
    )(LoginFormData.apply)(LoginFormData.unapply)
  )

  def authenticate: Action[LoginFormData] = Action(parse.form(loginForm)) { request =>
    val subscriptionId = request.body.endpoint.drop(request.body.endpoint.lastIndexOf('/') + 1)

    val secretKey = DB.withConnection { implicit dbc =>
      WebToxDB.initialise

      val credentials = request.body
      WebToxDB.authenticate(credentials.username, credentials.password).getOrElse {
        // Create the user.
        val secretKey = {
          val tox = new ToxCoreImpl[Unit](ToxOptions())
          val secretKey = tox.getSecretKey
          tox.close()
          secretKey
        }
        WebToxDB.addUser(credentials.username, credentials.password, secretKey)

        secretKey
      }.readable
    }

    Ok(Json.obj(
      "success" -> true
    )).withSession(
      AuthenticatedAction.setCookie(Savedata(secretKey, subscriptionId))
    )
  }

  def logout: Action[AnyContent] = AuthenticatedAction { request =>
    AuthenticatedAction.logout(request.session)

    Ok(Json.obj(
      "success" -> true
    )).withNewSession
  }

  def loginStatus: Action[AnyContent] = Action { request =>
    val loggedIn = AuthenticatedAction.isLoggedIn(request.session)

    Ok(Json.obj(
      "success" -> loggedIn
    ))
  }

}
