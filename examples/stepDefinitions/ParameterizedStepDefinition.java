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

  @Given("I have $value")
  public void given(int value) {
    x = value;
  }

  @When("I add $value")
  public void when(int value) {
    x = x + value;
  }

  @Then("I should have $value")
  public void then(int value) {
    assertThat(x, equalTo(value));
  }
}
