package com.booking;

import org.junit.platform.suite.api.*;
import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.booking")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value =
    "pretty, " +
    "html:target/cucumber-reports.html, " +
    "json:target/cucumber.json, " +
    "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
public class TestRunner {
}