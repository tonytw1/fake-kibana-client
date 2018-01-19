name := "kibana-client"

version := "1.0"

lazy val `kibana-client` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(ws)
libraryDependencies += "com.netaporter" %% "scala-uri" % "0.4.12"
libraryDependencies += specs2 % Test

