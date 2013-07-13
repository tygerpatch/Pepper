package org.pepper.core;

// Class to store useful methods that work on String objects
public class StringUtils {

  // ex. MyStepDefinition -> my_step_definition
  public static String snakecase(String str) {
    StringBuilder strBuilder = new StringBuilder();

    // make first letter lowercase
    char ch = Character.toLowerCase(str.charAt(0));
    strBuilder.append(ch);

    // for each character in the string
    for (int index = 1; index < str.length(); index++) {
      ch = str.charAt(index);

      // if character is upper case
      if (Character.isUpperCase(ch)) {
        // create a "space" and then lower case the character
        strBuilder.append('_');
        ch = Character.toLowerCase(ch);
      }

      // add character to result
      strBuilder.append(ch);
    }

    return strBuilder.toString();
  }
}
