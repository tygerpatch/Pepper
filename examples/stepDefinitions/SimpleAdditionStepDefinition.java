package stepDefinitions;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.pepper.core.StepDefinition;
import org.pepper.core.annotations.Given;
import org.pepper.core.annotations.Then;
import org.pepper.core.annotations.When;

// This class demonstrates how to write a step definition for a simple feature file.
// Note: I added more step methods than what is necessary to show that Pepper calls the right methods. 
public class SimpleAdditionStepDefinition extends StepDefinition {

  private int x;

  @Given("I have 3")
  public void given3() {
    x = 3;
  }

  @Given("I have -3")
  public void givenNegative3() {
    x = -3;
  }

  @When("I add 2")
  public void whenAdd2() {
    x = x + 2;
  }

  @When("I multiply by 2")
  public void whenMultiply2() {
    x = x * 2;
  }

  @Then("I should have 5")
  public void thenIShouldHave5() {
    assertThat(x, equalTo(5));
  }

  @Then("I should have -6")
  public void thenIShouldHaveNegative6() {
    assertThat(x, equalTo(-1));
  }

  @Test
  public void test() {
    fail("called test() method");
  }

  @Given("Hello World")
  public void given() {
    fail("called given() method");
  }
}
