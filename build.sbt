import java.io.File

name := "Voteban"

version := "1.0"

scalaVersion := "2.13.0"

assemblyJarName in assembly := s"${name.value}-${version.value}.jar"

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.2.0"
libraryDependencies += "org.json4s" %% "json4s-native" % "3.6.6"

// JDA
resolvers += "jcenter-bintray" at "http://jcenter.bintray.com"
libraryDependencies += "net.dv8tion" % "JDA" % "3.8.3_463"

// log4j Logger
libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.22"

//Custom tasks
lazy val RESOURCES_FILE = new File("src/main/resources")
lazy val ADDITIONAL_RESOURCES: Seq[File] = Seq(
  new File("README.md"),
  new File("LICENSE")
)
lazy val include = TaskKey[Unit]("include", "Copies the resources that should be packed with the jar to the resources directory")
include := SBTUtillity.includeResources(streams.value.log, RESOURCES_FILE, ADDITIONAL_RESOURCES)