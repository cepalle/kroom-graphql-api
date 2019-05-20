package io.kroom.api.deezer

import io.circe.generic.auto._
import io.circe.parser
import io.circe.syntax._
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DBDeezer(private val db: H2Profile.backend.Database) {

  import DBDeezer._

  def getDeezerGenre(id: Int): Option[DataDeezerGenre] = {
    val query = tabDeezerGenre.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjDeezerGenre)
  }

  def addDeezerGenre(dg: DataDeezerGenre): Boolean = {
    val f = db.run(tabDeezerGenre += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    true
  }

  def getDeezerAlbum(id: Int): Option[DataDeezerAlbum] = {
    val query = tabDeezerAlbum.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjDeezerAlbum)
  }

  def addDeezerAlbum(dg: DataDeezerAlbum): Boolean = {
    val f = db.run(tabDeezerAlbum += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    true
  }

  def getDeezerArtist(id: Int): Option[DataDeezerArtist] = {
    val query = tabDeezerArtist.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjDeezerArtist)
  }

  def addDeezerArtist(dg: DataDeezerArtist): Boolean = {
    val f = db.run(tabDeezerArtist += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    true
  }

  def getDeezerTrack(id: Int): Option[DataDeezerTrack] = {
    val query = tabDeezerTrack.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjDeezerTrack)
  }

  def addDeezerTrack(dg: DataDeezerTrack): Boolean = {
    val f = db.run(tabDeezerTrack += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    true
  }

}

object DBDeezer {

  class TabDeezerGenre(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_GENRE") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerGenre = TableQuery[TabDeezerGenre]

  def tabToObjDeezerGenre(t: (Int, String)): DataDeezerGenre = {
    parser.decode[DataDeezerGenre](t._2).toOption match {
      case Some(res) => res
      case _ => throw new IllegalArgumentException("TabDeezerGenre: json in db is invalid")
    }
  }

  class TabDeezerAlbum(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_ALBUM") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerAlbum = TableQuery[TabDeezerAlbum]

  def tabToObjDeezerAlbum(t: (Int, String)): DataDeezerAlbum = {
    parser.decode[DataDeezerAlbum](t._2).toOption match {
      case Some(res) => res
      case _ => throw new IllegalArgumentException("TabDeezerAlbum: json in db is invalid")
    }
  }

  class TabDeezerArtist(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_ARTIST") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerArtist = TableQuery[TabDeezerArtist]

  def tabToObjDeezerArtist(t: (Int, String)): DataDeezerArtist = {
    parser.decode[DataDeezerArtist](t._2).toOption match {
      case Some(res) => res
      case _ => throw new IllegalArgumentException("TabDeezerArtist: json in db is invalid")
    }
  }

  class TabDeezerTrack(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_TRACK") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerTrack = TableQuery[TabDeezerTrack]

  def tabToObjDeezerTrack(t: (Int, String)): DataDeezerTrack = {
    parser.decode[DataDeezerTrack](t._2).toOption match {
      case Some(res) => res
      case _ => throw new IllegalArgumentException("TabDeezerTrack: json in db is invalid")
    }
  }

}
