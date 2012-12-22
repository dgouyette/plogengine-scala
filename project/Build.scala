import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "plogengine-scala"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    anorm,
    jdbc,
    "rome" % "rome" % "1.0",
    "org.clapper" % "markwrap_2.10" % "0.5.5",
    "postgresql" % "postgresql" % "9.0-801.jdbc4",
    "commons-io" % "commons-io" % "2.0.1",
    "org.mindrot" % "jbcrypt" % "0.3m"
  )





  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "nexus CPD" at "http://nexus.cestpasdur.com/nexus/content/groups/everything/"

  )

}
