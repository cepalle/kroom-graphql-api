import slick.dbio.DBIO
import slick.jdbc.H2Profile

import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DBHandler {

  class Suppliers(tag: Tag) extends Table[(Int, String, String, String, String, String)](tag, "SUPPLIERS") {
    def id = column[Int]("SUP_ID", O.PrimaryKey)

    def name = column[String]("SUP_NAME")

    def street = column[String]("STREET")

    def city = column[String]("CITY")

    def state = column[String]("STATE")

    def zip = column[String]("ZIP")

    def * = (id, name, street, city, state, zip)
  }

  val suppliers = TableQuery[Suppliers]

  class Coffees(tag: Tag) extends Table[(String, Int, Double, Int, Int)](tag, "COFFEES") {
    def name = column[String]("COF_NAME", O.PrimaryKey)

    def supID = column[Int]("SUP_ID")

    def price = column[Double]("PRICE")

    def sales = column[Int]("SALES")

    def total = column[Int]("TOTAL")

    def * = (name, supID, price, sales, total)

    def supplier = foreignKey("SUP_FK", supID, suppliers)(_.id)
  }

  val coffees = TableQuery[Coffees]

  private val setup = DBIO.seq(
    // Create the tables, including primary and foreign keys
    (suppliers.schema ++ coffees.schema).create,

  )

  val db: H2Profile.backend.Database = Database.forConfig("h2mem1")
  private val setupFuture: Future[Unit] = db.run(setup)


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

}
