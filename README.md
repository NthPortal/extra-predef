# extra-predef

[![Build Status](https://travis-ci.org/NthPortal/extra-predef.svg?branch=master)](https://travis-ci.org/NthPortal/extra-predef)
[![Coverage Status](https://coveralls.io/repos/github/NthPortal/extra-predef/badge.svg?branch=master)](https://coveralls.io/github/NthPortal/extra-predef?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/com.nthportal/extra-predef_2.13.svg)](https://mvnrepository.com/artifact/com.nthportal/extra-predef_2.13)
[![Versioning](https://img.shields.io/badge/versioning-semver%202.0.0-blue.svg)](http://semver.org/spec/v2.0.0.html)
[![Docs](https://javadoc.io/badge2/com.nthportal/extra-predef_2.13/javadoc.svg?color=blue&label=docs)](https://javadoc.io/doc/com.nthportal/extra-predef_2.13)

An extra Predef for Scala.

## Add as a Dependency

### SBT

```scala
libraryDependencies += "com.nthportal" %% "extra-predef" % "2.1.0"
```

## Usage

You can use the definitions in the extra Predef either by importing
`com.nthportal.extrapredef.ExtraPredef._` wherever needed/desired,
or by using the `scalac` option
`-Yimports:java.lang,scala,scala.Predef,com.nthportal.extrapredef.ExtraPredef`.
