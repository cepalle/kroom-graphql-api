import io.circe.parser
import io.circe.generic.auto._
import io.circe.syntax._
import slick.jdbc.H2Profile.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

class DBHandler {

  import DBHandler._

  def getDeezerGenre(id: Int): Option[DeezerGenre] = {
    val query = tabDeezerGenre.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value.flatMap(_.toOption).flatMap(e => {
      val (id, json) = e
      parser.decode[DeezerGenre](json).toOption
    })
  }

  def addDeezerGenre(dg: DeezerGenre): Boolean = {
    val f = db.run(tabDeezerGenre += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    false
  }

  def getDeezerAlbum(id: Int): Option[DeezerAlbum] = {
    val query = tabDeezerAlbum.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value.flatMap(_.toOption).flatMap(e => {
      val (id, json) = e
      parser.decode[DeezerAlbum](json).toOption
    })
  }

  def addDeezerAlbum(dg: DeezerAlbum): Boolean = {
    val f = db.run(tabDeezerAlbum += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    false
  }

  def getDeezerArtist(id: Int): Option[DeezerArtist] = {
    val query = tabDeezerArtist.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value.flatMap(_.toOption).flatMap(e => {
      val (id, json) = e
      parser.decode[DeezerArtist](json).toOption
    })
  }

  def addDeezerArtist(dg: DeezerArtist): Boolean = {
    val f = db.run(tabDeezerArtist += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    false
  }

  def getDeezerTrack(id: Int): Option[DeezerTrack] = {
    val query = tabDeezerTrack.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value.flatMap(_.toOption).flatMap(e => {
      val (id, json) = e
      parser.decode[DeezerTrack](json).toOption
    })
  }

  def addDeezerTrack(dg: DeezerTrack): Boolean = {
    val f = db.run(tabDeezerTrack += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    false
  }

}

object DBHandler {

  class TabDeezerGenre(tag: Tag)
    extends Table[(Int, String)](tag, "DEEZER_GENRE") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerGenre = TableQuery[TabDeezerGenre]

  class TabDeezerAlbum(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_ALBUM") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerAlbum = TableQuery[TabDeezerAlbum]

  class TabDeezerArtist(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_ARTIST") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerArtist = TableQuery[TabDeezerArtist]

  class TabDeezerTrack(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_TRACK") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerTrack = TableQuery[TabDeezerTrack]

  private val db = Database.forConfig("h2mem1")

  def init(): Unit = {
    val setup = DBIO.seq(
      (tabDeezerGenre.schema ++ tabDeezerAlbum.schema ++ tabDeezerArtist.schema ++ tabDeezerTrack.schema).create,
    )

    val f = db.run(setup)

    val result = Await.ready(f, Duration.Inf).value.get

    println(result)
  }

}
