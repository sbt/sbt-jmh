sbtPlugin := true

//organization := "com.typesafe.sbt" someday perhaps

organization := "pl.project13.scala"

name := "sbt-jmh"

version := "0.1.0"

val jmhVersion = "0.7.1"

libraryDependencies += "org.openjdk.jmh" % "jmh-core"                 % jmhVersion    // GPLv2

libraryDependencies += "org.openjdk.jmh" % "jmh-generator-bytecode"   % jmhVersion    // GPLv2

libraryDependencies += "org.openjdk.jmh" % "jmh-generator-reflection" % jmhVersion    // GPLv2

publishTo <<= isSnapshot { snapshot =>
  if (snapshot) Some(Classpaths.sbtPluginSnapshots) else Some(Classpaths.sbtPluginReleases)
}


// publishing settings

publishMavenStyle := true

publishArtifact in Test := false

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(Path.userHome / ".sbt" / "sonatype.properties")

pomExtra :=
  <url>https://github.com/ktoso/sbt-jmh</url>
  <licenses>
    <license>
      <name>Apache2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:ktoso/sbt-jmh.git</url>
    <connection>scm:git:git@github.com:ktoso/sbt-jmh.git</connection>
  </scm>
  <developers>
    <developer>
      <id>ktoso</id>
      <name>Konrad 'ktoso' Malawski</name>
      <url>http://blog.project13.pl</url>
    </developer>
  </developers>
  <parent>
    <groupId>org.sonatype.oss</groupId>
    <artifactId>oss-parent</artifactId>
    <version>7</version>
  </parent>
