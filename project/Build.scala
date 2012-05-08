import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "plogengine-scala"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    //"mysql" % "mysql-connector-java" % "5.1.17",
    "rome" % "rome" % "1.0",
    "org.clapper" % "markwrap_2.9.1" % "0.5.4",
    "postgresql" % "postgresql" % "9.0-801.jdbc4",
    "commons-io" % "commons-io" % "2.0.1"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    resolvers += "CPD Repo" at "http://nexus.cestpasdur.com/nexus/content/groups/everything/"
  )

}
