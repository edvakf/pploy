name := """pploy"""

version := "0.4.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)

libraryDependencies += filters

resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "[3.4,)"

// less plugin only compiles main.less by default
// https://github.com/sbt/sbt-less/blob/5429c24ea3589ad232a20704a3c5472997cdfb74/src/main/scala/com/typesafe/sbt/less/SbtLess.scala#L52
includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"
