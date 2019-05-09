name := "sangria-akka-http-example"
version := "0.1.0-SNAPSHOT"

description := "An example GraphQL server written with akka-http, circe and sangria."

scalaVersion := "2.12.8"
scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.4.2",
  "org.sangria-graphql" %% "sangria-slowlog" % "0.1.8",
  "org.sangria-graphql" %% "sangria-circe" % "1.2.1",

  "com.typesafe.akka" %% "akka-http" % "10.1.3",
  "de.heikoseeberger" %% "akka-http-circe" % "1.21.0",

  "io.circe" %%	"circe-core" % "0.10.0",
  "io.circe" %% "circe-parser" % "0.10.0",
  "io.circe" %% "circe-optics" % "0.10.0",

  "org.scalatest" %% "scalatest" % "3.0.5" % Test,

  "org.scalaj" %% "scalaj-http" % "2.4.1"

)

Revolver.settings
enablePlugins(JavaAppPackaging)
