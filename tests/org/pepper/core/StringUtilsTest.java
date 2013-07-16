package org.pepper.core;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class StringUtilsTest {

  @Test
  public void testSnakeCase() {
    assertThat(StringUtils.snakecase("MyStepDefinition"), equalTo("my_step_definition"));
  }

  @Test
  public void testCamelCase() {
    // ex. "Given a variable x with value 3" -> givenAVariableXWithValue3
    String result = StringUtils.camelCase("Given a variable x with value 3");
    assertThat(result, equalTo("givenAVariableXWithValue3"));

    // ex. "wHeN tHe VaLuE 7 iS aDdEd" -> "whenTheValue7IsAdded"
    result = StringUtils.camelCase("wHeN tHe VaLuE 7 iS aDdEd");
    assertThat(result, equalTo("whenTheValue7IsAdded"));
  }
}
