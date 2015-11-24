package controllers.auth

import java.io.Closeable

import controllers.data.ProtoConversions.toProto
import im.tox.core.network.Port
import im.tox.tox4j.core.data.{ToxPublicKey, ToxSecretKey}
import im.tox.tox4j.core.options.{SaveDataOptions, ToxOptions}
import im.tox.tox4j.core.{ToxCore, ToxCoreConstants}
import im.tox.tox4j.impl.jni.ToxCoreImpl
import models.proto.TimedEvent.E.EStartedToxInstance
import models.proto.{StartedToxInstance, TimedEvent, TimedEventList}

final class WebToxInstance(secretKey: ToxSecretKey) extends Closeable {

  val tox: ToxCore[WebToxCoreState] = {
    val tox = new ToxCoreImpl[WebToxCoreState](ToxOptions(saveData = SaveDataOptions.SecretKey(secretKey)))
    tox.bootstrap(
      "144.76.60.215",
      Port.fromInt(ToxCoreConstants.DefaultStartPort).get,
      ToxPublicKey.fromHexString("04119E835DF3E78BACF0F84235B300546AF8B936F035185E2A8E9E0A67C8924F").getOrElse {
        throw new RuntimeException("Key parsing failed")
      }
    )

    val eventListener = new WebToxEventListener
    tox.callback(eventListener)
    tox
  }

  private var state = WebToxCoreState().addEvent(EStartedToxInstance(StartedToxInstance(
    tox.getAddress.readable,
    tox.getName.toString(),
    tox.getStatusMessage.toString(),
    toProto(tox.getStatus),
    tox.getNospam
  )))

  override def close(): Unit = tox.close()

  def iterate(): Unit = {
    state = tox.iterate(state).performActions(tox)
  }

  def updateSubscription(subscription: String, enabled: Boolean): Unit = {
    state = state.updateSubscription(subscription, enabled)
  }

  def fireEvent(event: TimedEvent.E): Unit = {
    state = state.addEvent(event)
  }

  def isEmpty: Boolean = {
    state.sessions.isEmpty
  }

  def latestEvents(timestamp: Long): TimedEventList = {
    state.latestEvents(timestamp)
  }

}
