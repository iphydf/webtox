package controllers

import play.api.mvc._

import scala.language.postfixOps

case object Homepage extends Controller {

  def index: Action[AnyContent] = Action {
    val x = org.webjars.RequireJS.getSetupJavaScript(routes.WebJarAssets.at("").url)
    Ok(views.html.index())
  }

}
