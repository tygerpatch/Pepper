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

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.pepper.core.annotations.Given;
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

  // Invokes Step methods in StepDefinition
  protected Statement childrenInvoker(final RunNotifier notifier) {

    newStepDefinition();

    return new Statement() {
      @Override
      public void evaluate() {
        try {
          File file = new File(path);
          Scanner scanner = new Scanner(file); // <- FileNotFoundException
          String step;
          FrameworkMethod method;
          StringTokenizer stepTokenizer, keyTokenizer;
          String keyToken, stepToken;

          while (scanner.hasNextLine()) {
            step = scanner.nextLine().trim();

            // skip empty lines
            if(step.isEmpty()) {
              continue; // with reading the feature file
            }

            if (step.startsWith("Scenario:")) {
              // need to create a new instance of StepDefinition so variables are in initial state
              newStepDefinition();
            }
            else if (step.startsWith("Given") || step.startsWith("When") || step.startsWith("Then")) {
              // handle Given-When-Then step
              if ((method = map.get(step)) != null) {
                PepperRunner.this.runChild(method, notifier);
              }
              else {
                // check if step is parameterized
                // Note: Users should expect parameterized steps to take a little longer to process.
                boolean found = false;

                // for each step method in the StepDefinition subclass
                for (String key : map.keySet()) {
                  params.clear();
                  keyTokenizer = new StringTokenizer(key);
                  stepTokenizer = new StringTokenizer(step);

                  // iterate over each word in the step method and in the line of the feature file
                  while (keyTokenizer.hasMoreTokens() && stepTokenizer.hasMoreTokens()) {
                    keyToken = keyTokenizer.nextToken();
                    stepToken = stepTokenizer.nextToken();

                    // if word in step method begins with $ then it must be variable placeholder
                    if (keyToken.startsWith("$")) {
                      try {
                        params.add(Integer.parseInt(stepToken));
                      }
                      catch (NumberFormatException numberFormat) {
                        numberFormat.printStackTrace();
                      }
                    }
                    // else if it's not a variable placeholder
                    // and it doesn't match with word in the given line of the feature file,
                    // then don't invoke the method corresponding to this key
                    if (keyToken.equals(stepToken)) {
                      break; // from while-loop
                    }
                  }

                  // if there are no more words in either the line or the step method
                  if (!keyTokenizer.hasMoreTokens() && !stepTokenizer.hasMoreTokens()) {
                    // then invoke the corresponding to this key
                    method = map.get(key);

                    PepperRunner.this.runChild(method, notifier);
                    found = true;
                    break; // from for-each loop
                  }
                }

                // if step method was not found, then generate a method stub for it
                if (!found) {
                  System.out.println("@Pending");
                  if (step.startsWith("Given")) {
                    System.out.print("@Given(");
                  }
                  else if (step.startsWith("When")) {
                    System.out.print("@When(");
                  }
                  else if (step.startsWith("Then")) {
                    System.out.print("@Then(");
                  }
                  System.out.println(step + ")");
                  System.out.println();
                }
              }
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
  protected void validateInstanceMethods(List<Throwable> errors) {
    // This method is called by collectInitializationErrors(List<Throwable> errors),
    // which is called by validate() method,
    // which is called inside ParentRunner's ParentRunner(Class<?> testClass) constructor.

    // For some strange reason the given-when-then maps aren't initialized when this method is called.
    // The JUnit API says this method will go away in the future. So I think it's safe to comment out.
  }

  private Map<String, FrameworkMethod> map = new HashMap<String, FrameworkMethod>();

  // This method returns a list of all the Given-When-Then step methods in the StepDefinition.
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
  protected Statement methodBlock(final FrameworkMethod method) {

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        // FrameworkMethod - Object invokeExplosively(Object target, Object... params)
        method.invokeExplosively(stepDef, params.toArray());
      }
    };
  }
}
