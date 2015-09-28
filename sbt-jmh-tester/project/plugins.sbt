lazy val plugins = project
  .in(file("."))
  .dependsOn(file("../").getCanonicalFile.toURI)
  .dependsOn(file("../sbt-jmh-plugin").getCanonicalFile.toURI)
  .dependsOn(file("../sbt-jmh-flamegraph").getCanonicalFile.toURI)
