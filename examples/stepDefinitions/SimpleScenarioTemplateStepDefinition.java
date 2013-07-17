package stepDefinitions;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.pepper.core.StepDefinition;
import org.pepper.core.annotations.Given;
import org.pepper.core.annotations.Then;
import org.pepper.core.annotations.When;

public class SimpleScenarioTemplateStepDefinition extends StepDefinition {

  private int x;

  @Given("x is $x")
  public void given(int x) {
    this.x = x;
  }

  @When("I add $y")
  public void when(int y) {
    x = x + y;
  }

  @Then("I should have $z")
  public void then(int z) {
    assertThat(x, equalTo(z));
  }
}
