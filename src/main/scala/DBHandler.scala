import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

class DBHandler {

  import DBHandler._

  def getDeezerGenre(id: Int): Option[DeezerGenre] = {
    None
  }

  def addDeezerGenre(dg: DeezerGenre): Boolean = {
    true
  }

  def getDeezerAlbum(id: Int): Option[DeezerAlbum] = {
    None
  }

  def addDeezerAlbum(dg: DeezerAlbum): Boolean = {
    true
  }

  def getDeezerArtist(id: Int): Option[DeezerArtist] = {
    None
  }

  def addDeezerArtist(dg: DeezerArtist): Boolean = {
    true
  }

  def getDeezerTrack(id: Int): Option[DeezerTrack] = {
    None
  }

  def addDeezerTrack(dg: DeezerTrack): Boolean = {
    true
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
