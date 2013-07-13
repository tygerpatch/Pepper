package org.pepper.core;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import stepDefinitions.SimpleAdditionStepDefinition;

public class StepDefinitionTest {

  private StepDefinition stepDef;

  @Before
  public void setUp() {
    stepDef = new SimpleAdditionStepDefinition();
  }

  // By default, the name of the feature file should be the same as the
  // StepDefinition subclass, only snake cased.
  @Test
  public void testGetFeatureName() {
    assertThat(stepDef.getFeatureName(), equalTo("simple_addition"));
  }

  // Should allow the User to customize the name of the feature file.
  @Test
  public void testSetFeatureName() {
    stepDef.setFeatureName("Addition");
    // Note: Addition is just a random feature name here.
    assertThat(stepDef.getFeatureName(), equalTo("Addition"));
  }

  // By default, feature files will have an extension of ".feature"
  @Test
  public void testGetFeatureExtension() {
    assertThat(stepDef.getFeatureExtension(), equalTo(".feature"));
  }

  // Should allow the User to customize the feature file's extension name.
  @Test
  public void testSetFeatureExtension() {
    stepDef.setFeatureExtension(".story");
    // Note: ".story" is just a random feature extension here.
    assertThat(stepDef.getFeatureExtension(), equalTo(".story"));
  }

  // By default will look for feature files in the first folder it finds named "feature".
  @Test
  public void testGetFeatureFolder() {
    String featuresFolder = stepDef.getFeaturesFolder();
    assertTrue(featuresFolder.endsWith("features"));

    File file = new File(featuresFolder + "/" + stepDef.getFeatureName() + stepDef.getFeatureExtension());
    assertTrue(file.exists());
  }
}
