package draco

import org.scalatest.{BeforeAndAfterAll, Suite}
import org.slf4j.{Logger, LoggerFactory, MDC}

/** Mix into a ScalaTest suite to send its bulky/diagnostic output to a per-suite
  * file instead of flooding the console. Use `log` (the `test.output` logger) in
  * place of `println`; logback routes it to `target/test-output/<suite>.log`
  * (truncated each run) via the "suite" MDC key, and keeps it off the console.
  *
  * The console then carries only ScalaTest's per-test PASS/FAIL lines (which arrive
  * via sbt's logger, not stdout) plus one pointer line per suite saying where the
  * data went. See `src/test/resources/logback-test.xml`. */
trait PersistentTestLog extends BeforeAndAfterAll { this: Suite =>

  /** File-only logger for demonstration/diagnostic output (replaces `println`). */
  protected val log: Logger = LoggerFactory.getLogger("test.output")

  /** Console-only, plain-format logger for the occasional headline a suite wants
    * visible at a glance (e.g. a one-line summary). Bulk output still goes to `log`. */
  protected val console: Logger = LoggerFactory.getLogger("test.report")

  override protected def beforeAll(): Unit = {
    MDC.put("suite", suiteName)
    super.beforeAll()
  }

  override protected def afterAll(): Unit = {
    try super.afterAll()
    finally {
      console.info(s"  $suiteName output -> target/test-output/$suiteName.log")
      MDC.remove("suite")
    }
  }
}
