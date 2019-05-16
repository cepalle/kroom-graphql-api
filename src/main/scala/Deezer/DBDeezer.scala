package Deezer

import io.circe.generic.auto._
import io.circe.parser
import io.circe.syntax._
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object DBDeezer {

  def getDeezerGenre(db: H2Profile.backend.Database, id: Int): Option[DeezerGenre] = {
    val query = tabDeezerGenre.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjDeezerGenre)
  }

  def addDeezerGenre(db: H2Profile.backend.Database, dg: DeezerGenre): Boolean = {
    val f = db.run(tabDeezerGenre += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    true
  }

  def getDeezerAlbum(db: H2Profile.backend.Database, id: Int): Option[DeezerAlbum] = {
    val query = tabDeezerAlbum.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjDeezerAlbum)
  }

  def addDeezerAlbum(db: H2Profile.backend.Database, dg: DeezerAlbum): Boolean = {
    val f = db.run(tabDeezerAlbum += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    true
  }

  def getDeezerArtist(db: H2Profile.backend.Database, id: Int): Option[DeezerArtist] = {
    val query = tabDeezerArtist.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjDeezerArtist)
  }

  def addDeezerArtist(db: H2Profile.backend.Database, dg: DeezerArtist): Boolean = {
    val f = db.run(tabDeezerArtist += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    true
  }

  def getDeezerTrack(db: H2Profile.backend.Database, id: Int): Option[DeezerTrack] = {
    val query = tabDeezerTrack.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjDeezerTrack)
  }

  def addDeezerTrack(db: H2Profile.backend.Database, dg: DeezerTrack): Boolean = {
    val f = db.run(tabDeezerTrack += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    true
  }

  class TabDeezerGenre(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_GENRE") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerGenre = TableQuery[TabDeezerGenre]

  def tabToObjDeezerGenre(t: (Int, String)): DeezerGenre = {
    parser.decode[DeezerGenre](t._2).toOption match {
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

  def tabToObjDeezerAlbum(t: (Int, String)): DeezerAlbum = {
    parser.decode[DeezerAlbum](t._2).toOption match {
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

  def tabToObjDeezerArtist(t: (Int, String)): DeezerArtist = {
    parser.decode[DeezerArtist](t._2).toOption match {
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

  def tabToObjDeezerTrack(t: (Int, String)): DeezerTrack = {
    parser.decode[DeezerTrack](t._2).toOption match {
      case Some(res) => res
      case _ => throw new IllegalArgumentException("TabDeezerTrack: json in db is invalid")
    }
  }

}
