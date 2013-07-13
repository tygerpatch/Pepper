package stepDefinitions;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.pepper.core.StepDefinition;
import org.pepper.core.annotations.Given;
import org.pepper.core.annotations.Then;
import org.pepper.core.annotations.When;

// Corresponding feature file has a narrative and a comment
public class AdditionStepDefinition extends StepDefinition {

  private int x;

  @Given("a variable x with value 3")
  public void given3() {
    x = 3;
  }

  @When("the value 7 is added")
  public void whenAdd2() {
    x = x + 7;
  }

  @Then("the variable x should have the value 10")
  public void thenIShouldHave5() {
    assertThat(x, equalTo(10));
  }
}
