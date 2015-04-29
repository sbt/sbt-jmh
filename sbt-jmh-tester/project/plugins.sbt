lazy val plugins = project
  .in(file("."))
  .dependsOn(file("../").getCanonicalFile.toURI)
