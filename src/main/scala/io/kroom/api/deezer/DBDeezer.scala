package io.kroom.api.deezer

import io.circe.generic.auto._
import io.circe.parser
import io.circe.syntax._
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Try}

class DBDeezer(private val db: H2Profile.backend.Database) {

  import DBDeezer._

  def getDeezerGenre(id: Int): Try[DataDeezerGenre] = {
    val query = tabDeezerGenre.filter(_.id === id).result.head

    Await.ready(db.run(query), Duration.Inf).value
      .map(_.flatMap(tabToObjDeezerGenre))
      .getOrElse(Failure(new IllegalStateException("DBDeezer.getDeezerGenre genre.id not found")))
  }

  def addDeezerGenre(dg: DataDeezerGenre): Try[Unit] = {
    val query = tabDeezerGenre += ((dg.id, dg.asJson.toString()))

    Await.ready(db.run(query), Duration.Inf).value
      .getOrElse(Failure(new IllegalStateException("DBDeezer.addDeezerGenre failed")))
      .map(_ => Unit)
  }

  def getDeezerAlbum(id: Int): Try[DataDeezerAlbum] = {
    val query = tabDeezerAlbum.filter(_.id === id).result.head

    Await.ready(db.run(query), Duration.Inf).value
      .map(_.flatMap(tabToObjDeezerAlbum))
      .getOrElse(Failure(new IllegalStateException("DBDeezer.getDeezerAlbum album.id not found")))
  }

  def addDeezerAlbum(dg: DataDeezerAlbum): Try[Unit] = {
    val query = tabDeezerAlbum += ((dg.id, dg.asJson.toString()))

    Await.ready(db.run(query), Duration.Inf).value
      .getOrElse(Failure(new IllegalStateException("DBDeezer.addDeezerAlbum failed")))
      .map(_ => Unit)
  }

  def getDeezerArtist(id: Int): Try[DataDeezerArtist] = {
    val query = tabDeezerArtist.filter(_.id === id).result.head

    Await.ready(db.run(query), Duration.Inf).value
      .map(_.flatMap(tabToObjDeezerArtist))
      .getOrElse(Failure(new IllegalStateException("DBDeezer.getDeezerArtist artist.id not found")))
  }

  def addDeezerArtist(dg: DataDeezerArtist): Try[Unit] = {
    val query = tabDeezerArtist += ((dg.id, dg.asJson.toString()))

    Await.ready(db.run(query), Duration.Inf).value
      .getOrElse(Failure(new IllegalStateException("DBDeezer.addDeezerArtist failed")))
      .map(_ => Unit)
  }

  def getDeezerTrack(id: Int): Try[DataDeezerTrack] = {
    val query = tabDeezerTrack.filter(_.id === id).result.head

    Await.ready(db.run(query), Duration.Inf).value
      .map(_.flatMap(tabToObjDeezerTrack))
      .getOrElse(Failure(new IllegalStateException("DBDeezer.getDeezerTrack track.id not found")))
  }

  def addDeezerTrack(dg: DataDeezerTrack): Try[Unit] = {
    val query = tabDeezerTrack += ((dg.id, dg.asJson.toString()))

    Await.ready(db.run(query), Duration.Inf).value
      .getOrElse(Failure(new IllegalStateException("DBDeezer.addDeezerTrack failed")))
      .map(_ => Unit)
  }

}

object DBDeezer {

  class TabDeezerGenre(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_GENRE") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerGenre = TableQuery[TabDeezerGenre]

  def tabToObjDeezerGenre(t: (Int, String)): Try[DataDeezerGenre] = {
    parser.decode[DataDeezerGenre](t._2).toTry
  }

  class TabDeezerAlbum(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_ALBUM") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerAlbum = TableQuery[TabDeezerAlbum]

  def tabToObjDeezerAlbum(t: (Int, String)): Try[DataDeezerAlbum] = {
    parser.decode[DataDeezerAlbum](t._2).toTry
  }

  class TabDeezerArtist(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_ARTIST") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerArtist = TableQuery[TabDeezerArtist]

  def tabToObjDeezerArtist(t: (Int, String)): Try[DataDeezerArtist] = {
    parser.decode[DataDeezerArtist](t._2).toTry
  }

  class TabDeezerTrack(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_TRACK") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerTrack = TableQuery[TabDeezerTrack]

  def tabToObjDeezerTrack(t: (Int, String)): Try[DataDeezerTrack] = {
    parser.decode[DataDeezerTrack](t._2).toTry
  }

}
