package stepDefinitions;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.pepper.core.StepDefinition;
import org.pepper.core.annotations.Given;
import org.pepper.core.annotations.Then;
import org.pepper.core.annotations.When;

// This class demonstrates how to parameterize steps.
public class ParameterizedStepDefinition extends StepDefinition {

  private int x;
  private boolean flag;
  private double y;

  // *** GIVENS
  @Given("I have $value")
  public void given(int value) {
    x = value;
  }

  @Given("I have $value")
  public void givenDbl(double value) {
    y = value;
  }
  // Notice that although the above two step methods have the same text,
  // Pepper calls the correct one based on the given argument types.

  @Given("the flag is $value")
  public void givenTheFlagIsFalse(boolean value) {
    flag = value;
  }

  // *** WHENS
  @When("I add $value")
  public void when(int value) {
    x = x + value;
  }

  @When("I add $dbl")
  public void whenDbl(double value) {
    y = y + value;
  }
  // Again, notice that Pepper calls the correct step method based on the given argument types.

  @When("I check the flag")
  public void whenICheckTheFlag() {
  }

  // *** THENS
  @Then("I should have $value")
  public void then(int value) {
    assertThat(x, equalTo(value));
  }

  @Then("I should have $value")
  public void thenDbl(double value) {
    assertThat(y, equalTo(value));
  }

  @Then("it should be $value")
  public void thenItShouldBeFalse(boolean value) {
    assertThat(flag, equalTo(value));
  }
}
