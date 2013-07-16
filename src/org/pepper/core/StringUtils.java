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

  // ex. "Given a variable x with value 3" -> givenAVariableXWithValue3
  public static String camelCase(String str) {
    StringBuilder strBuilder = new StringBuilder();
    boolean isNewWord = false;
    // Notice that the first letter is always lower cased

    for(char ch : str.toCharArray()) {
      if(isNewWord) {
        strBuilder.append(Character.toUpperCase(ch));
        isNewWord = false;
      }
      else if((ch == ' ') || (ch == '\t')) {
        isNewWord = true;
      }
      else {
        strBuilder.append(Character.toLowerCase(ch));
      }
    }

    return strBuilder.toString();
  }
}
