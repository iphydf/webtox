package controllers.data

import java.sql.Connection

import anorm._
import im.tox.tox4j.core.data.ToxSecretKey

/**
 * Database access object.
 */
case object WebToxDB {

  def initialise(implicit dbc: Connection): Unit = {
    SQL"""
      CREATE TABLE IF NOT EXISTS Users (
        username TEXT NOT NULL,
        password TEXT NOT NULL,
        secretKey TEXT NOT NULL
      );
      CREATE TABLE IF NOT EXISTS Sessions (
        secretKey TEXT NOT NULL,
        session INT NOT NULL
      );
    """
      .execute()
  }

  def addUser(username: String, password: String, secretKey: ToxSecretKey)(implicit dbc: Connection): Unit = {
    // Insert user info into DB.
    SQL"""
      INSERT INTO Users
      VALUES($username, $password, ${secretKey.readable})
     """
      .execute()
  }

  def authenticate(username: String, password: String)(implicit dbc: Connection): Option[ToxSecretKey] = {
    SQL"""
      SELECT secretKey
      FROM Users
      WHERE username = $username
      AND password = $password
    """
      .as(SqlParser.str("secretKey").singleOpt)
      .map(ToxSecretKey.fromHexString).flatMap(_.toOption)
  }

}
