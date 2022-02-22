package org.jenkinsci.plugins.redpen.models;

import com.google.common.collect.ImmutableList;

public class Constants {
    private Constants() {
    }

    public static final String WEB_DRIVER_IO = "WEB_DRIVER_IO";
    public static final String JUNIT = "JUNIT";
    public static final String JACOCO = "JACOCO";
    public static final String NUNIT = "NUNIT";
    public static final String JEST = "JEST";
    public static final String SELENIUM = "SELENIUM";
    public static final String GIT_BRANCH = "GIT_BRANCH";
    public static final String GIT_BRANCH_MAIN = "main";

    public static final String NONE_DISPLAY_NAME = "None";
    public static final String JUNIT_DISPLAY_NAME = "JUnit";
    public static final String NUNIT_DISPLAY_NAME = "NUnit";
    public static final String SELENIUM_DISPLAY_NAME = "Selenium";
    public static final String JACOCO_DISPLAY_NAME = "Jacoco";
    public static final String JEST_DISPLAY_NAME = "Jest";
    public static final String WEB_DRIVER_IO_DISPLAY_NAME = "WebDriverIO";

    public static final String JENKINS_HOME = "JENKINS_HOME";
    public static final String REDPEN_PLUGIN = "RedpenPlugin";
    public static final String BASE_PATH = "https://api.qa.redpen.work";
    public static final String CLIENT_ID = "3b0127d0-0607-4d6d-ae92-a96cd3b6c063";

    public static final String JUNIT_PATH = "/logs";
    public static final String NUNIT_PATH = "/logs";
    public static final String SELENIUM_PATH = "/chromedriver.log";
    public static final String JACOCO_PATH = "/target/jacoco.exec";
    public static final String JEST_PATH = "/jest-stare/jest-results.json";
    public static final String WEB_DRIVER_IO_PATH = "/reports/E2E-report.pdf";

    public static final ImmutableList<TestFrameWork> TEST_FRAME_WORKS = ImmutableList.of(
            TestFrameWork.builder().displayName(WEB_DRIVER_IO_DISPLAY_NAME).path(WEB_DRIVER_IO_PATH)
                    .value(WEB_DRIVER_IO).build(),
            TestFrameWork.builder().displayName(JUNIT_DISPLAY_NAME).path(JUNIT_PATH).value(JUNIT).build(),
            TestFrameWork.builder().displayName(JACOCO_DISPLAY_NAME).path(JACOCO_PATH).value(JACOCO)
                    .build(),
            TestFrameWork.builder().displayName(NUNIT_DISPLAY_NAME).path(NUNIT_PATH).value(NUNIT).build(),
            TestFrameWork.builder().displayName(SELENIUM_DISPLAY_NAME).path(SELENIUM_PATH).value(SELENIUM)
                    .build(),
            TestFrameWork.builder().displayName(JEST_DISPLAY_NAME).path(JEST_PATH).value(JEST)
                    .build());

}
