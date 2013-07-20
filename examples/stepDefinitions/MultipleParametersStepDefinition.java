package stepDefinitions;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.pepper.core.StepDefinition;
import org.pepper.core.annotations.Given;
import org.pepper.core.annotations.Then;
import org.pepper.core.annotations.When;

// This class demonstrates how to parameterize steps.
public class MultipleParametersStepDefinition extends StepDefinition {

  private int numQuarters, numDimes, numNickles, numPennies;
  private double change;

  @Given("I have $Q Quarters, $D Dimes, $N Nickles, and $P Pennies")
  public void given(int Q, int D, int N, int P) {
    numQuarters = Q;
    numDimes = D;
    numNickles = N;
    numPennies = P;
  }

  // When I count my change
  @When("I count my change")
  public void when() {
    change = (0.25 * numQuarters) + (0.10 * numDimes) + (0.05 * numNickles) + (0.01 * numPennies);
  }

  // Then I should have 0.99 cents in change
  @Then("I should have $thisMany cents in change")
  public void then(double amount) {
    assertThat(change, equalTo(amount));
  }
}
