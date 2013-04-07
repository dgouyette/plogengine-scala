import sbt._
import Keys._
import sbt._
import play.Project._



object ApplicationBuild extends Build {

  val appName = "plogengine-scala"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    jdbc,
    "rome" % "rome" % "1.0",
    "org.clapper" % "markwrap_2.10" % "0.5.5",
    "mysql" % "mysql-connector-java" % "5.1.21",
    "postgresql" % "postgresql" % "9.0-801.jdbc4",
    "commons-io" % "commons-io" % "2.0.1",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "com.typesafe" %% "slick" % "1.0.0-RC1",
    "com.google.guava" % "guava" % "13.0.1",
    "org.elasticsearch" % "elasticsearch" % "0.19.0"
  )





  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "nexus CPD" at "http://nexus.cestpasdur.com/nexus/content/groups/everything/"

  )

}
