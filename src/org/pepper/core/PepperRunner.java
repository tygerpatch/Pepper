package org.pepper.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.pepper.core.annotations.Given;
import org.pepper.core.annotations.Pending;
import org.pepper.core.annotations.Then;
import org.pepper.core.annotations.When;

public class PepperRunner extends BlockJUnit4ClassRunner {

  public PepperRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  private StepDefinition stepDef;
  private String path;

  private void newStepDefinition() {
    try {
      stepDef = (StepDefinition) getTestClass().getOnlyConstructor().newInstance();

      // This is where you can pass configuration information from the StepDefinition to the PepperRunner.
      if (path == null) {
        path = new File(stepDef.getFeaturesFolder() + "/" + stepDef.getFeatureName() + stepDef.getFeatureExtension()).getAbsolutePath();
      }
    }
    catch (InvocationTargetException invocationTarget) {
      invocationTarget.printStackTrace();
    }
    catch (IllegalAccessException illegalAccess) {
      illegalAccess.printStackTrace();
    }
    catch (InstantiationException instantiation) {
      instantiation.printStackTrace();
    }
  }

  // Given a line in the feature file, it returns the proper Step method in the StepDefinitions file to call
  public FrameworkMethod extractMethod(String line) {
    // handle Given-When-Then step
    FrameworkMethod method = map.get(line);

    if (method != null) {
      return method;
    }

    StringTokenizer stepTokenizer, keyTokenizer;
    String keyToken, stepToken;

    // *** check if step is parameterized
    // Note: Users should expect parameterized steps to take a little longer to process.

    // for each step method in the StepDefinition subclass
    for (String key : map.keySet()) {
      params.clear();

      keyTokenizer = new StringTokenizer(key);
      stepTokenizer = new StringTokenizer(line);

      // iterate over each word in the step method and in the line of the feature file
      while (keyTokenizer.hasMoreTokens() && stepTokenizer.hasMoreTokens()) {
        keyToken = keyTokenizer.nextToken();
        stepToken = stepTokenizer.nextToken();

        // if the word in the given line of the feature file doesn't match up with the word in the step
        if(!keyToken.equals(stepToken)) {
          // if word in step method begins with $ then it must be variable placeholder
          if (keyToken.startsWith("$")) {
            try {
              params.add(Integer.parseInt(stepToken));
            }
            catch (NumberFormatException numberFormat) {
              // numberFormat.printStackTrace(); // TODO: should exception be logged
              params.add(Boolean.valueOf(stepToken));
            }
          }
          break; // from while-loop
        }
      }

      // if there are no more words in either the line or the step method
      if (!keyTokenizer.hasMoreTokens() && !stepTokenizer.hasMoreTokens()) {
        // then invoke the corresponding to this key
        return map.get(key);
      }
    }

    return null;
  }

  public void generateStub(String line) {
    System.out.println("@Pending");
    if (line.startsWith("Given")) {
      System.out.print("@Given(\"");
      System.out.print(line.substring(6));
      System.out.println("\")");
    }
    else if (line.startsWith("When")) {
      System.out.print("@When(\"");
      System.out.print(line.substring(5));
      System.out.println("\")");
    }
    else if (line.startsWith("Then")) {
      System.out.print("@Then(\"");
      System.out.print(line.substring(5));
      System.out.println("\")");
    }

    System.out.print("public void ");
    char ch;
    boolean isNewWord = false;
    for(int index = 0; index < line.length(); index++) {
      ch = line.charAt(index);

      if(isNewWord) {
        ch = Character.toUpperCase(ch);
        System.out.print(ch);
        isNewWord = false;
      }
      else if((ch == ' ') || (ch == '\t')) {
        isNewWord = true;
      }
      else {
        ch = Character.toLowerCase(ch);
        System.out.print(ch);
      }
    }
    System.out.println("() {");
    System.out.println("}");
    System.out.println();
  }

  // Invokes Step methods in StepDefinition
  @Override
  protected Statement childrenInvoker(final RunNotifier notifier) {

    newStepDefinition();
    notifier.addListener(new PepperRunnerListener());

    return new Statement() {
      @Override
      public void evaluate() {
        try {
          File file = new File(path);
          Scanner scanner = new Scanner(file); // <- FileNotFoundException
          String line;
          FrameworkMethod method;

          while (scanner.hasNextLine()) {
            line = scanner.nextLine().trim();

            if (line.startsWith("Scenario:")) {
              newStepDefinition();
              System.out.println(line);
            }
            else if (line.startsWith("Given") || line.startsWith("When") || line.startsWith("Then")) {
              params.clear();
              method = extractMethod(line);

              if (method == null) {
                generateStub(line);
              }
              else {
                System.out.print(line);
                PepperRunner.this.runChild(method, notifier);
              }
            }
            else {
              System.out.println(line);
            }
          }
          scanner.close();
        }
        catch (FileNotFoundException fileNotFound) {
          fileNotFound.printStackTrace();
        }
      }
    };
  }

  @Deprecated
  @Override
  protected void validateInstanceMethods(List<Throwable> errors) {
    // This method is called by collectInitializationErrors(List<Throwable> errors),
    // which is called by validate() method,
    // which is called inside ParentRunner's ParentRunner(Class<?> testClass) constructor.

    // For some strange reason the given-when-then maps aren't initialized when this method is called.
    // The JUnit API says this method will go away in the future. So I think it's safe to comment out.
  }

  private Map<String, FrameworkMethod> map = new HashMap<String, FrameworkMethod>();

  // This method returns a list of all the Given-When-Then step methods in the StepDefinition.
  @Override
  protected List<FrameworkMethod> computeTestMethods() {
    List<FrameworkMethod> list = new ArrayList<FrameworkMethod>();
    TestClass testClass = this.getTestClass();

    for (FrameworkMethod method : testClass.getAnnotatedMethods(Given.class)) {
      list.add(method);

      Given given = method.getAnnotation(Given.class);
      map.put("Given " + given.value(), method);
    }

    for (FrameworkMethod method : testClass.getAnnotatedMethods(When.class)) {
      list.add(method);

      When when = method.getAnnotation(When.class);
      map.put("When " + when.value(), method);
    }

    for (FrameworkMethod method : testClass.getAnnotatedMethods(Then.class)) {
      list.add(method);

      Then then = method.getAnnotation(Then.class);
      map.put("Then " + then.value(), method);
    }

    return list;
  }

  List<Object> params = new ArrayList<Object>();

  // Invokes a Step method
  @Override
  protected Statement methodBlock(final FrameworkMethod method) {

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        // FrameworkMethod - Object invokeExplosively(Object target, Object... params)
        method.invokeExplosively(stepDef, params.toArray());
      }
    };
  }

  @Override
  protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
    Description description = describeChild(method);

    // if (method.getAnnotation(Ignore.class) != null) {   // <- BlockJUnit4ClassRunner's version
    if (method.getAnnotation(Pending.class) != null) {
      notifier.fireTestIgnored(description);
    }
    else {
      runLeaf(methodBlock(method), description, notifier);
    }
  }
}
