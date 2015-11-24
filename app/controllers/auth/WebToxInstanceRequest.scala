package controllers.auth

import im.tox.tox4j.core.ToxCore
import models.proto.{Savedata, TimedEvent, TimedEventList}
import play.api.mvc.{Request, WrappedRequest}

final class WebToxInstanceRequest[A](
    request: Request[A],
    val savedata: Savedata,
    instance: WebToxInstance
) extends WrappedRequest[A](request) {

  def tox: ToxCore[WebToxCoreState] = instance.tox

  def updateSubscription(subscription: String, enabled: Boolean): Unit = {
    instance.updateSubscription(subscription, enabled)
  }

  def fireEvent(event: TimedEvent.E): Unit = {
    instance.fireEvent(event)
  }

  def latestEvents(timestamp: Long): TimedEventList = {
    instance.latestEvents(timestamp)
  }

}
