package stepDefinitions;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.pepper.core.StepDefinition;
import org.pepper.core.annotations.Given;
import org.pepper.core.annotations.Then;
import org.pepper.core.annotations.When;

// Demonstrates Pepper can handle And steps
public class CalculatorStepDefinition extends StepDefinition {

  @Given("I want to do Addition")
  public void givenIWantToDoAddition() {
  }

  private int firstOperand, secondOperand, result;

  @Given("my first operand is 6")
  public void givenMyFirstOperandIs6() {
    firstOperand = 6;
  }

  @Given("my second operand is 2")
  public void givenMySecondOperandIs2() {
    secondOperand = 2;
  }

  @When("I execute the Operation")
  public void whenIExecuteTheOperation() {
    result = firstOperand + secondOperand;
  }

  @Then("I should get 8 as my result")
  public void thenIShouldGet8AsMyResult() {
    assertThat(result, equalTo(8));
  }
}
