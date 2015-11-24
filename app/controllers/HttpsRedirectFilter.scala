package controllers

import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Filter, RequestHeader, Result, Results}

import scala.concurrent.Future

object HttpsRedirectFilter extends Filter {

  override def apply(next: RequestHeader => Future[Result])(request: RequestHeader): Future[Result] = {
    request.headers.get("x-forwarded-proto") match {

      case Some(header) =>
        if (header == "https") {
          next(request).map { result =>
            result.withHeaders(("Strict-Transport-Security", "max-age=31536000"))
          }
        } else {
          Future.successful(Results.Redirect("https://" + request.host + request.uri, 301))
        }

      case None =>
        next(request)

    }
  }

}
