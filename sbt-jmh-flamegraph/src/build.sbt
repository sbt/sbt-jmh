// adding the tools.jar to the unmanaged-jars seq
unmanagedJars in Compile ~= {uj =>
  Seq(Attributed.blank(file(System.getProperty("java.home").dropRight(3)+"lib/tools.jar"))) ++ uj
}

libraryDependencies += ("com.sun" % "tools" % "1.8" % "provided")
  .from("file://" + System.getProperty("java.home").dropRight(3)+"lib/tools.jar")

//// exluding the tools.jar file from the build
//excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
//    cp filter {_.data.getName == "tools.jar"}
//}