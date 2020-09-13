# extra-predef

[![Build Status](https://img.shields.io/travis/NthPortal/extra-predef/master?logo=travis&style=for-the-badge)](https://travis-ci.org/NthPortal/extra-predef)
[![Coverage Status](https://img.shields.io/coveralls/github/NthPortal/extra-predef/master?logo=coveralls&style=for-the-badge)](https://coveralls.io/github/NthPortal/extra-predef?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/com.nthportal/extra-predef_2.13?logo=apache-maven&style=for-the-badge)](https://mvnrepository.com/artifact/com.nthportal/extra-predef_2.13)
[![Versioning](https://img.shields.io/badge/versioning-semver%202.0.0-blue.svg?style=for-the-badge)](http://semver.org/spec/v2.0.0.html)
[![Docs](https://www.javadoc.io/badge2/com.nthportal/extra-predef_2.13/docs.svg?color=blue&style=for-the-badge)](https://www.javadoc.io/doc/com.nthportal/extra-predef_2.13)

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
