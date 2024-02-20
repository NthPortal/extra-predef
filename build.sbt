ThisBuild / scalaVersion    := "2.13.6"
ThisBuild / autoAPIMappings := true
ThisBuild / crossScalaVersions := Seq(
  "2.12.13",
  "2.13.6"
)

// publishing info
inThisBuild(
  Seq(
    organization  := "com.nthportal",
    versionScheme := Some("early-semver"),
    homepage      := Some(url("https://github.com/NthPortal/extra-predef")),
    licenses      := Seq("The Apache License, Version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")),
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

// CI config
val PrimaryJava = "adopt@1.8"

inThisBuild(
  Seq(
    githubWorkflowTargetTags ++= Seq("v*"),
    githubWorkflowPublishTargetBranches ++= Seq(
      RefPredicate.StartsWith(Ref.Tag("v"))
    ),
    githubWorkflowJavaVersions := Seq(
      "adopt@1.8",
      "adopt@1.11",
      "adopt@1.15",
      "graalvm-ce-java8@20.2.0"
    ),
    githubWorkflowBuild := Seq(
      WorkflowStep.Sbt(List("compile", "test:compile"), name = Some("Compile"))
    ),
    githubWorkflowAddedJobs ++= Seq(
      WorkflowJob(
        "test",
        "Test",
        githubWorkflowJobSetup.value.toList ::: List(
          WorkflowStep.Sbt(List("test"), name = Some("Test"))
        ),
        javas = githubWorkflowJavaVersions.value.toList,
        scalas = crossScalaVersions.value.toList,
        needs = List("build"),
        matrixFailFast = Some(false)
      ),
      WorkflowJob(
        "bincompat",
        "Binary Compatibility",
        githubWorkflowJobSetup.value.toList ::: List(
          WorkflowStep.Sbt(List("clean", "mimaReportBinaryIssues"), name = Some("MiMa"))
        ),
        javas = List(PrimaryJava),
        scalas = crossScalaVersions.value.toList,
        needs = List("build"),
        matrixFailFast = Some(false)
      ),
      WorkflowJob(
        "coverage",
        "Coverage",
        githubWorkflowJobSetup.value.toList ::: List(
          WorkflowStep.Sbt(
            List("coverage", "test", "coverageAggregate", "coveralls"),
            name = Some("Coveralls"),
            cond = Some("env.COVERALLS_REPO_TOKEN != ''")
          )
        ),
        env = Map("COVERALLS_REPO_TOKEN" -> "${{ secrets.COVERALLS_REPO_TOKEN }}"),
        javas = List(PrimaryJava),
        scalas = crossScalaVersions.value.toList,
        needs = List("test"),
        matrixFailFast = Some(false)
      ),
      WorkflowJob(
        "formatting",
        "Formatting",
        githubWorkflowJobSetup.value.toList ::: List(
          WorkflowStep.Sbt(List("scalafmtCheckAll", "scalafmtSbtCheck"), name = Some("Formatting"))
        ),
        javas = List(PrimaryJava),
        scalas = List(scalaVersion.value),
        needs = List("build"),
        matrixFailFast = Some(false)
      ),
      WorkflowJob(
        "check-docs",
        "Check Docs",
        githubWorkflowJobSetup.value.toList ::: List(
          WorkflowStep.Sbt(List("doc"), name = Some("Check Scaladocs"))
        ),
        javas = List(PrimaryJava),
        scalas = List(scalaVersion.value),
        needs = List("build"),
        matrixFailFast = Some(false)
      )
    ),
    githubWorkflowPublishPreamble += WorkflowStep.Use(UseRef.Public("olafurpg", "setup-gpg", "v3")),
    githubWorkflowPublish := Seq(
      WorkflowStep.Sbt(
        List("ci-release"),
        env = Map(
          "PGP_PASSPHRASE"    -> "${{ secrets.PGP_PASSPHRASE }}",
          "PGP_SECRET"        -> "${{ secrets.PGP_SECRET }}",
          "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
          "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
        )
      )
    )
  )
)

lazy val extraPredef = project
  .in(file("."))
  .settings(
    name                  := "extra-predef",
    description           := "An extra Predef for Scala.",
    mimaPreviousArtifacts := Set("2.0.0").map(organization.value %% name.value % _),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.18" % Test
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
