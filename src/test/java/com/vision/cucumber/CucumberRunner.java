package com.vision.cucumber;

import io.cucumber.junit.platform.engine.Cucumber;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
public class CucumberRunner {
    static {
        org.bytedeco.javacpp.Loader.load(org.bytedeco.opencv.opencv_java.class);
    }
}
