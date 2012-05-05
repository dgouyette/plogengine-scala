import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "plogengine-scala"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "mysql" % "mysql-connector-java" % "5.1.17",
    "rome" % "rome" % "1.0",
    "org.clapper" %% "markwrap" % "0.5.3"
  //"postgresql" % "postgresql" % "8.4-702.jdbc4"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    resolvers += "CPD Repo" at "http://nexus.cestpasdur.com/nexus/content/groups/everything/"
  )

}
