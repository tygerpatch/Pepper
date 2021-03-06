package org.pepper.core;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class PepperRunnerListener extends RunListener {

  protected String line;
  protected boolean isRunFinished;

  public void setLine(String line) {
    this.line = line;
  }

  @Override
  public void testRunStarted(Description description) throws Exception {
    super.testRunStarted(description);
    isRunFinished = false;
  }

  @Override
  public void testFailure(Failure failure) throws Exception {
    System.out.println(line + " (FAILED)");
    isRunFinished = true;
    super.testFailure(failure);
  }

  @Override
  public void testFinished(Description description) throws Exception {
    // Note: Without this condition expression, Pepper will display " (PASSED)" at the end of the Scenario.
    if(!isRunFinished) {
      System.out.println(line + " (PASSED)");
      super.testFinished(description);
    }
  }

  @Override
  public void testIgnored(Description description) throws Exception {
    System.out.println(line + " (PENDING)");
    super.testIgnored(description);
  }
}
