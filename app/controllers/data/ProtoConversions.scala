package controllers.data

import im.tox.tox4j.core.enums.{ToxConnection, ToxFileControl, ToxMessageType, ToxUserStatus}
import models.proto.{Connection, FileControl, MessageType, UserStatus}

object ProtoConversions {

  def toProto(connectionStatus: ToxConnection): Connection.Type = {
    connectionStatus match {
      case ToxConnection.NONE => Connection.Type.NONE
      case ToxConnection.TCP  => Connection.Type.TCP
      case ToxConnection.UDP  => Connection.Type.UDP
    }
  }

  def toProto(status: ToxUserStatus): UserStatus.Type = {
    status match {
      case ToxUserStatus.NONE => UserStatus.Type.NONE
      case ToxUserStatus.AWAY => UserStatus.Type.AWAY
      case ToxUserStatus.BUSY => UserStatus.Type.BUSY
    }
  }

  def toProto(status: ToxMessageType): MessageType.Type = {
    status match {
      case ToxMessageType.ACTION => MessageType.Type.ACTION
      case ToxMessageType.NORMAL => MessageType.Type.NORMAL
    }
  }

  def toProto(control: ToxFileControl): FileControl.Type = {
    control match {
      case ToxFileControl.CANCEL => FileControl.Type.CANCEL
      case ToxFileControl.PAUSE  => FileControl.Type.PAUSE
      case ToxFileControl.RESUME => FileControl.Type.RESUME
    }
  }

}
