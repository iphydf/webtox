package controllers.auth

import codes.reactive.scalatime._
import controllers.PushMessaging
import im.tox.tox4j.core.ToxCore
import models.proto.{TimedEvent, TimedEventList}

import scala.language.postfixOps

/**
 * The Tox state for a single user.
 */
final case class WebToxCoreState(
    actions: List[ToxCore[WebToxCoreState] => Unit] = Nil,
    events: TimedEventList = TimedEventList.defaultInstance,
    sessions: Map[String, Instant] = Map.empty
) {

  def addEvent(event: TimedEvent.E): WebToxCoreState = {
    PushMessaging.pushNotification(sessions.keys, "WebTox Event", "WebTox received a message")
    copy(events = events.addEvents(TimedEvent(System.currentTimeMillis(), event)))
  }

  /**
   * Filter out events older than the passed time stamp.
   */
  def latestEvents(timestamp: Long): TimedEventList = {
    events.withEvents(events.events.filter(_.timestamp > timestamp))
  }

  def addAction(action: ToxCore[WebToxCoreState] => Unit): WebToxCoreState = {
    copy(actions = action :: actions)
  }

  def performActions(tox: ToxCore[WebToxCoreState]): WebToxCoreState = {
    actions.reverse.foreach(_(tox))
    copy(actions = Nil)
  }

  def updateSubscription(subscriptionId: String, enabled: Boolean): WebToxCoreState = {
    copy(sessions =
      if (enabled) {
        sessions + (subscriptionId -> Instant())
      } else {
        sessions - subscriptionId
      })
  }

}
