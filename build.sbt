ThisBuild / scalaVersion := "2.13.5"
ThisBuild / autoAPIMappings := true
ThisBuild / crossScalaVersions := Seq(
  "2.12.10",
  "2.13.3"
)

// publishing info
inThisBuild(
  Seq(
    organization := "com.nthportal",
    homepage := Some(url("https://github.com/NthPortal/extra-predef")),
    licenses := Seq("The Apache License, Version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    developers := List(
      Developer(
        "NthPortal",
        "April | Princess",
        "dev@princess.lgbt",
        url("https://nthportal.com")
      )
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/NthPortal/extra-predef"),
        "scm:git:git@github.com:NthPortal/extra-predef.git",
        "scm:git:git@github.com:NthPortal/extra-predef.git"
      )
    )
  )
)

lazy val extraPredef = project
  .in(file("."))
  .settings(
    name := "extra-predef",
    description := "An extra Predef for Scala.",
    mimaPreviousArtifacts := Set("2.0.0").map(organization.value %% name.value % _),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.7" % Test
    ),
    scalacOptions ++= {
      if (isSnapshot.value) Nil
      else
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 13)) => Seq("-opt:l:inline", "-opt-inline-from:com.nthportal.extrapredef.**")
          case _             => Nil
        }
    }
  )
