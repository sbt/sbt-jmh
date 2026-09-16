// A build carrying the pre-#16 override, which used to select the directory scanned for
// @Benchmark-annotated classes. It must fail with migration instructions rather than
// silently scan the wrong directory.
enablePlugins(JmhPlugin)

Jmh / classDirectory := (Test / classDirectory).value
