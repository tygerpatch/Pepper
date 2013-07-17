package org.pepper.core;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import static org.junit.matchers.JUnitMatchers.hasItems;

public class PepperRunnerTest {

  @Test
  public void testGenerateStub() {
    // TODO: write test to ensure someone doesn't do something like "giventhat x is 3"
    StringBuilder strBuilder = new StringBuilder();
    String result = PepperRunner.generateStub("Given a variable x with value 3");

    strBuilder.append("@Pending\n");
    strBuilder.append("@Given(\"a variable x with value 3\")\n");
    strBuilder.append("public void givenAVariableXWithValue3() {\n");
    strBuilder.append("}\n");

    assertThat(result, equalTo(strBuilder.toString()));
    // ---
    strBuilder = new StringBuilder();
    result = PepperRunner.generateStub("When the value 7 is added");

    strBuilder.append("@Pending\n");
    strBuilder.append("@When(\"the value 7 is added\")\n");
    strBuilder.append("public void whenTheValue7IsAdded() {\n");
    strBuilder.append("}\n");

    assertThat(result, equalTo(strBuilder.toString()));
    // ---
    strBuilder = new StringBuilder();
    result = PepperRunner.generateStub("Then the variable x should have the value 10");

    strBuilder.append("@Pending\n");
    strBuilder.append("@Then(\"the variable x should have the value 10\")\n");
    strBuilder.append("public void thenTheVariableXShouldHaveTheValue10() {\n");
    strBuilder.append("}\n");

    assertThat(result, equalTo(strBuilder.toString()));
  }

  @Test
  public void testParseArgument() {
    Object obj;

    // ensure integer arguments get parsed as an integer
    obj = PepperRunner.parseArgument("123");
    assertTrue("java.lang.Integer".equals(obj.getClass().getName()));

    // ensure that numbers with a decimal get parsed as a double
    obj = PepperRunner.parseArgument("1.23");
    assertTrue("java.lang.Double".equals(obj.getClass().getName()));

    // ensure that "true" get parsed as a boolean
    obj = PepperRunner.parseArgument("true");
    assertTrue("java.lang.Boolean".equals(obj.getClass().getName()));

    // ensure that "false" get parsed as a boolean
    obj = PepperRunner.parseArgument("false");
    assertTrue("java.lang.Boolean".equals(obj.getClass().getName()));

    // ensure that parsing a boolean is case insensitive
    obj = PepperRunner.parseArgument("tRuE");
    assertTrue("java.lang.Boolean".equals(obj.getClass().getName()));

    // ensure that parsing a boolean is case insensitive
    obj = PepperRunner.parseArgument("fAlSe");
    assertTrue("java.lang.Boolean".equals(obj.getClass().getName()));
  }

  @Test
  public void testParseRow() {
    List<String> list = PepperRunner.parseRow("|-1|2|3|");
    assertThat(list.size(), equalTo(3));
    assertThat(list, hasItems("-1", "2", "3"));
  }
}
