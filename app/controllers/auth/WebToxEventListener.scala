package controllers.auth

import com.google.protobuf.ByteString
import controllers.data.ProtoConversions.toProto
import im.tox.tox4j.core.callbacks.ToxEventAdapter
import im.tox.tox4j.core.data._
import im.tox.tox4j.core.enums.{ToxConnection, ToxFileControl, ToxMessageType, ToxUserStatus}
import models.proto.TimedEvent.E._
import models.proto._

import scala.language.postfixOps

final class WebToxEventListener extends ToxEventAdapter[WebToxCoreState] {

  override def friendConnectionStatus(friendNumber: Int, connectionStatus: ToxConnection)(state: WebToxCoreState): WebToxCoreState = {
    state.addEvent(EFriendConnectionStatus(FriendConnectionStatus(friendNumber, toProto(connectionStatus))))
  }

  override def friendStatusMessage(friendNumber: Int, message: ToxStatusMessage)(state: WebToxCoreState): WebToxCoreState = {
    state.addEvent(EFriendStatusMessage(FriendStatusMessage(friendNumber, message.toString)))
  }

  override def fileRecvChunk(friendNumber: Int, fileNumber: Int, position: Long, data: Array[Byte])(state: WebToxCoreState): WebToxCoreState = {
    state.addEvent(EFileRecvChunk(FileRecvChunk(friendNumber, fileNumber, position, ByteString.copyFrom(data))))
  }

  override def friendTyping(friendNumber: Int, isTyping: Boolean)(state: WebToxCoreState): WebToxCoreState = {
    state.addEvent(EFriendTyping(FriendTyping(friendNumber, isTyping)))
  }

  override def friendName(friendNumber: Int, name: ToxNickname)(state: WebToxCoreState): WebToxCoreState = {
    state.addEvent(EFriendName(FriendName(friendNumber, name.toString)))
  }

  override def friendReadReceipt(friendNumber: Int, messageId: Int)(state: WebToxCoreState): WebToxCoreState = {
    state.addEvent(EFriendReadReceipt(FriendReadReceipt(friendNumber, messageId)))
  }

  override def friendLossyPacket(friendNumber: Int, data: ToxLossyPacket)(state: WebToxCoreState): WebToxCoreState = {
    state.addEvent(EFriendLossyPacket(FriendLossyPacket(friendNumber, ByteString.copyFrom(data.value))))
  }

  override def selfConnectionStatus(connectionStatus: ToxConnection)(state: WebToxCoreState): WebToxCoreState = {
    state.addEvent(ESelfConnectionStatus(SelfConnectionStatus(toProto(connectionStatus))))
  }

  override def friendStatus(friendNumber: Int, status: ToxUserStatus)(state: WebToxCoreState): WebToxCoreState = {
    state.addEvent(EFriendStatus(FriendStatus(friendNumber, toProto(status))))
  }

  override def friendMessage(friendNumber: Int, messageType: ToxMessageType, timeDelta: Int, message: ToxFriendMessage)(state: WebToxCoreState): WebToxCoreState = {
    state.addEvent(EFriendMessage(FriendMessage(friendNumber, toProto(messageType), timeDelta, message.toString)))
  }

  override def fileChunkRequest(friendNumber: Int, fileNumber: Int, position: Long, length: Int)(state: WebToxCoreState): WebToxCoreState = {
    state.addEvent(EFileChunkRequest(FileChunkRequest(friendNumber, fileNumber, position, length)))
  }

  override def friendRequest(publicKey: ToxPublicKey, timeDelta: Int, message: ToxFriendRequestMessage)(state: WebToxCoreState): WebToxCoreState = {
    state.addEvent(EFriendRequest(FriendRequest(publicKey.readable, timeDelta, message.toString)))
      .addAction { tox =>
        tox.addFriendNorequest(publicKey)
      }
  }

  override def fileRecv(friendNumber: Int, fileNumber: Int, kind: Int, fileSize: Long, filename: ToxFilename)(state: WebToxCoreState): WebToxCoreState = {
    state.addEvent(EFileRecv(FileRecv(friendNumber, fileNumber, kind, fileSize, filename.toString)))
  }

  override def fileRecvControl(friendNumber: Int, fileNumber: Int, control: ToxFileControl)(state: WebToxCoreState): WebToxCoreState = {
    state.addEvent(EFileRecvControl(FileRecvControl(friendNumber, fileNumber, toProto(control))))
  }

  override def friendLosslessPacket(friendNumber: Int, data: ToxLosslessPacket)(state: WebToxCoreState): WebToxCoreState = {
    state.addEvent(EFriendLosslessPacket(FriendLosslessPacket(friendNumber, ByteString.copyFrom(data.value))))
  }

}
