organization := "com.nthportal"
name := "extra-predef"
description := "An extra Predef for Scala."

val rawVersion = "0.2.0"
isSnapshot := false
version := rawVersion + {if (isSnapshot.value) "-SNAPSHOT" else ""}

scalaVersion := "2.12.1"

crossScalaVersions := Seq(
  "2.12.0",
  "2.12.1"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % Test
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true
licenses := Seq("The Apache License, Version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage := Some(url("https://github.com/NthPortal/extra-predef"))

pomExtra :=
  <scm>
    <url>https://github.com/NthPortal/extra-predef</url>
    <connection>scm:git:git@github.com:NthPortal/extra-predef.git</connection>
    <developerConnection>scm:git:git@github.com:NthPortal/extra-predef.git</developerConnection>
  </scm>
    <developers>
      <developer>
        <id>NthPortal</id>
        <name>NthPortal</name>
        <url>https://github.com/NthPortal</url>
      </developer>
    </developers>

    