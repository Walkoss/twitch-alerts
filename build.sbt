import play.sbt.PlaySettings
import sbt.Keys._


scalaVersion in ThisBuild := "2.12.8"

libraryDependencies += guice
libraryDependencies += "org.joda" % "joda-convert" % "2.1.2"
libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "5.2"

libraryDependencies += "com.netaporter" %% "scala-uri" % "0.4.16"
libraryDependencies += "net.codingwell" %% "scala-guice" % "4.2.1"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.2" % Test

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "4.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.0"
)
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.27.2.1"

// The Play project itself
lazy val root = (project in file("."))
  .enablePlugins(Common, PlayService, PlayLayoutPlugin)
  .settings(
    name := """twitch-alerts"""
  )

javaOptions in Test += "-Dconfig.resource=application.test.conf"
