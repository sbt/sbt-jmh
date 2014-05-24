import JmhKeys._

jmhSettings

version in Jmh := "0.7.1"

outputTarget in Jmh := target.value / s"scala-${scalaBinaryVersion.value}"
