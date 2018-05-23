package org.openjdk.jmh.samples

import java.security.{Policy, URIParameter}
import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown

object JMHSample_33_SecurityManager {

  /*
   * Some targeted tests may care about SecurityManager being installed.
   * Since JMH itself needs to do privileged actions, it is not enough
   * to blindly install the SecurityManager, as JMH infrastructure will fail.
   */

  /*
   * In this example, we want to measure the performance of System.getProperty
   * with SecurityManager installed or not. To do this, we have two state classes
   * with helper methods. One that reads the default JMH security policy (we ship one
   * with JMH), and installs the security manager; another one that makes sure
   * the SecurityManager is not installed.
   *
   * If you need a restricted security policy for the tests, you are advised to
   * get /jmh-security-minimal.policy, that contains the minimal permissions
   * required for JMH benchmark to run, merge the new permissions there, produce new
   * policy file in a temporary location, and load that policy file instead.
   * There is also /jmh-security-minimal-runner.policy, that contains the minimal
   * permissions for the JMH harness to run, if you want to use JVM args to arm
   * the SecurityManager.
   */


  @State(Scope.Benchmark)
  class SecurityManagerInstalled {

    @Setup
    def setup(): Unit = {
      val policyFile = classOf[JMHSample_33_SecurityManager].getResource("/jmh-security.policy").toURI()
      Policy.setPolicy(Policy.getInstance("JavaPolicy", new URIParameter(policyFile)))
      System.setSecurityManager(new SecurityManager())
    }

    @TearDown
    def tearDown(): Unit = {
      System.setSecurityManager(null)
    }
  }

  @State(Scope.Benchmark)
  class SecurityManagerEmpty {
    @Setup
    def setup(): Unit = {
      System.setSecurityManager(null)
    }
  }

}

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class JMHSample_33_SecurityManager {

  import JMHSample_33_SecurityManager._

  @Benchmark
  def testWithSM(s: SecurityManagerInstalled): String = System.getProperty("java.home")

  @Benchmark
  def testWithoutSM(s: SecurityManagerEmpty): String = System.getProperty("java.home")
}
