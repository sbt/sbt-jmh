// Mirrors the multimodule recipe in the README: benchmarks live under src/test, so the
// generator has to scan (Test / classDirectory) instead of the default (Compile / classDirectory).
enablePlugins(JmhPlugin)

Jmh / sourceDirectory := (Test / sourceDirectory).value
Jmh / jmhBytecodeDirectory := (Test / classDirectory).value
Jmh / dependencyClasspath := (Test / dependencyClasspath).value
// The scanned directory is (Test / classDirectory), so the benchmarks have to be compiled
// before the generator scans for them.
Jmh / run := (Jmh / run).dependsOn(Test / compile).evaluated
