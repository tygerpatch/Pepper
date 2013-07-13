package org.pepper.core;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class StringUtilsTest {

  @Test
  public void testSnakeCase() {
    assertThat(StringUtils.snakecase("MyStepDefinition"), equalTo("my_step_definition"));
  }
}
