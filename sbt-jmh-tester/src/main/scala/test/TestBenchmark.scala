package test

import org.openjdk.jmh.annotations.Benchmark

import scala.io.Source

class TestBenchmark {
  import TestBenchmark._

  @Benchmark
  def range(): Int =
    1.to(100000)
      .filter(_ % 2 == 0)
      .count(_.toString.length == 4)

  @Benchmark
  def iterator(): Int =
    Iterator.from(1)
      .takeWhile(_ < 100000)
      .filter(_ % 2 == 0)
      .count(_.toString.length == 4)

  @Benchmark
  def readFromFile(): Int = {
    val actual = helloFile.getLines()
      .map(_.length)
      .sum
    assert(actual > 0)
    actual
  }

object TestBenchmark {
  val helloFile = Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("hello.txt"))
}
