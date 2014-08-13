import sbt._
import Keys._
import sbt._
import play.Project._


object ApplicationBuild extends Build {

  val appName = "plogengine-scala"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    jdbc,
    cache,
    "rome" % "rome" % "1.0",
    "com.typesafe.slick" % "slick_2.10" % "1.0.0",
    "org.clapper" % "markwrap_2.10" % "0.5.5",
    "mysql" % "mysql-connector-java" % "5.1.21",
    "commons-io" % "commons-io" % "2.0.1",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "com.google.guava" % "guava" % "13.0.1",
    "org.jongo" % "jongo" % "0.4",

    "org.elasticsearch" % "elasticsearch" % "1.3.1"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
   // resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"


  )

}
