scalariformSettings

name := """pploy"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws
)

libraryDependencies += filters

resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "[3.4,)"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies += "org.scalatestplus" %% "play" % "1.4.0-M3" % "test"

// less plugin only compiles main.less by default
// https://github.com/sbt/sbt-less/blob/5429c24ea3589ad232a20704a3c5472997cdfb74/src/main/scala/com/typesafe/sbt/less/SbtLess.scala#L52
includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"

// http://grokbase.com/t/gg/play-framework/14bhbem1vw/2-2-change-folder-name-in-distribution-zip
name in Universal := moduleName.value
