import scalaj.http.{Http, HttpRequest, HttpResponse}
import io.circe.generic.auto._
import io.circe.parser
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Id(id: Int)

case class DataListId(data: List[Id])

case class DeezerGenre(
                        id: Int,
                        name: String,
                        // url,
                        picture: String,
                        // url,
                        picture_small: String,
                        // url,
                        picture_medium: String,
                        // url,
                        picture_big: String,
                        // url,
                        picture_xl: String,
                      )

case class DeezerAlbum(
                        id: Int,
                        title: String,
                        upc: String,
                        // url,
                        link: String,
                        // url,
                        share: String,
                        // url,
                        cover: String,
                        // url,
                        cover_small: String,
                        // url,
                        cover_medium: String,
                        // url,
                        cover_big: String,
                        // url,
                        cover_xl: String,
                        // -1 not found
                        genre_id: Int,
                        // genres: DataListId,
                        label: String,
                        nb_tracks: Int,
                        duration: Int,
                        fans: Int,
                        rating: Int,
                        // date
                        release_date: String,
                        record_type: String,
                        available: Boolean,
                        // alternative: DeezerAlbum,
                        // url
                        tracklist: String,
                        explicit_lyrics: Boolean,
                        //0:Not Explicit; 1:Explicit; 2:Unknown; 3:Edited; 4:Partially Explicit (Album "lyrics" only); 5:Partially Unknown (Album "lyrics" only); 6:No Advice Available; 7:Partially No Advice Available (Album "lyrics" only)
                        explicit_content_lyrics: Int,
                        //0:Not Explicit; 1:Explicit; 2:Unknown; 3:Edited; 4:Partially Explicit (Album "lyrics" only); 5:Partially Unknown (Album "lyrics" only); 6:No Advice Available; 7:Partially No Advice Available (Album "lyrics" only)
                        explicit_content_cover: Int,
                        contributors: List[Id],
                        artist: Id,
                        // tracks: DataListId,
                      )

case class DeezerArtist(
                         id: Int,
                         name: String,
                         // url,
                         link: String,
                         // url,
                         share: String,
                         // url,
                         picture: String,
                         // url,
                         picture_small: String,
                         // url,
                         picture_medium: String,
                         // url,
                         picture_big: String,
                         // url,
                         picture_xl: String,
                         nb_album: Int,
                         nb_fan: Int,
                         // url
                         tracklist: String
                       )

case class DeezerTrack(
                        id: Int,
                        readable: Boolean,
                        title: String,
                        title_short: String,
                        title_version: String,
                        isrc: String,
                        // url,
                        link: String,
                        // url,
                        share: String,
                        duration: Int,
                        track_position: Int,
                        disk_number: Int,
                        rank: Int,
                        // Date,
                        release_date: String,
                        explicit_lyrics: Boolean,
                        explicit_content_lyrics: Int,
                        explicit_content_cover: Int,
                        // url,
                        preview: String,
                        bpm: Double,
                        gain: Double,
                        available_countries: List[String],
                        contributors: List[Id],
                        artist: Id,
                        album: Id,
                      )

// --

class DeezerRepo {

  // --

  // Definition of the SUPPLIERS table
  class Suppliers(tag: Tag) extends Table[(Int, String, String, String, String, String)](tag, "SUPPLIERS") {
    def id = column[Int]("SUP_ID", O.PrimaryKey) // This is the primary key column
    def name = column[String]("SUP_NAME")

    def street = column[String]("STREET")

    def city = column[String]("CITY")

    def state = column[String]("STATE")

    def zip = column[String]("ZIP")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, name, street, city, state, zip)
  }

  val suppliers = TableQuery[Suppliers]

  // Definition of the COFFEES table
  class Coffees(tag: Tag) extends Table[(String, Int, Double, Int, Int)](tag, "COFFEES") {
    def name = column[String]("COF_NAME", O.PrimaryKey)

    def supID = column[Int]("SUP_ID")

    def price = column[Double]("PRICE")

    def sales = column[Int]("SALES")

    def total = column[Int]("TOTAL")

    def * = (name, supID, price, sales, total)

    // A reified foreign key relation that can be navigated to create a join
    def supplier = foreignKey("SUP_FK", supID, suppliers)(_.id)
  }

  val coffees = TableQuery[Coffees]

  val setup = DBIO.seq(
    // Create the tables, including primary and foreign keys
    (suppliers.schema ++ coffees.schema).create,

    // Insert some suppliers
    suppliers += (101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199"),
    suppliers += (49, "Superior Coffee", "1 Party Place", "Mendocino", "CA", "95460"),
    suppliers += (150, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966"),
    // Equivalent SQL code:
    // insert into SUPPLIERS(SUP_ID, SUP_NAME, STREET, CITY, STATE, ZIP) values (?,?,?,?,?,?)

    // Insert some coffees (using JDBC's batch insert feature, if supported by the DB)
    coffees ++= Seq(
      ("Colombian", 101, 7.99, 0, 0),
      ("French_Roast", 49, 8.99, 0, 0),
      ("Espresso", 150, 9.99, 0, 0),
      ("Colombian_Decaf", 101, 8.99, 0, 0),
      ("French_Roast_Decaf", 49, 9.99, 0, 0)
    )
    // Equivalent SQL code:
    // insert into COFFEES(COF_NAME, SUP_ID, PRICE, SALES, TOTAL) values (?,?,?,?,?)
  )

  val db = Database.forConfig("h2mem1")
  val setupFuture: Future[Unit] = db.run(setup)


  // Insert some coffees (using JDBC's batch insert feature)
  val insertAction: DBIO[Option[Int]] = coffees ++= Seq(
    ("Colombian", 101, 7.99, 0, 0),
    ("French_Roast", 49, 8.99, 0, 0),
    ("Espresso", 150, 9.99, 0, 0),
    ("Colombian_Decaf", 101, 8.99, 0, 0),
    ("French_Roast_Decaf", 49, 9.99, 0, 0)
  )

  val insertAndPrintAction: DBIO[Unit] = insertAction.map { coffeesInsertResult =>
    // Print the number of rows inserted
    coffeesInsertResult foreach { numRows =>
      println(s"Inserted $numRows rows into the Coffees table")
    }
  }


  println("Coffees:")
  db.run(coffees.result).map(_.foreach {
    case (name, supID, price, sales, total) =>
      println("  " + name + "\t" + supID + "\t" + price + "\t" + sales + "\t" + total)
  })

  // --


  def getTrackById(id: Int): Option[DeezerTrack] = {
    val request: HttpRequest = Http(s"https://api.deezer.com/track/$id")
    val res: HttpResponse[String] = request.asString

    val decodingResult = parser.decode[DeezerTrack](res.body)
    decodingResult match {
      case Right(track) => Some(track)
      case Left(error) => {
        print("getTrackById: ")
        println(error)
        None
      }
    }
  }

  def getArtistById(id: Int): Option[DeezerArtist] = {
    val request: HttpRequest = Http(s"https://api.deezer.com/artist/$id")
    val res: HttpResponse[String] = request.asString

    val decodingResult = parser.decode[DeezerArtist](res.body)
    decodingResult match {
      case Right(artist) => Some(artist)
      case Left(error) => {
        print("getArtistById: ")
        println(error)
        None
      }
    }
  }

  def getAlbumById(id: Int): Option[DeezerAlbum] = {
    val request: HttpRequest = Http(s"https://api.deezer.com/album/$id")
    val res: HttpResponse[String] = request.asString

    val decodingResult = parser.decode[DeezerAlbum](res.body)
    decodingResult match {
      case Right(album) => Some(album)
      case Left(error) => {
        print("getAlbumById: ")
        println(error)
        None
      }
    }
  }

  def getGenreById(id: Int): Option[DeezerGenre] = {
    val request: HttpRequest = Http(s"https://api.deezer.com/genre/$id")
    val res: HttpResponse[String] = request.asString

    val decodingResult = parser.decode[DeezerGenre](res.body)
    decodingResult match {
      case Right(genre) => Some(genre)
      case Left(error) => {
        print("getGenreById: ")
        println(error)
        None
      }
    }
  }

}
