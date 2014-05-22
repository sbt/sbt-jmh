import JmhKeys._

jmhSettings

outputTarget in Jmh := target.value / s"scala-${scalaBinaryVersion.value}" / "classes"
