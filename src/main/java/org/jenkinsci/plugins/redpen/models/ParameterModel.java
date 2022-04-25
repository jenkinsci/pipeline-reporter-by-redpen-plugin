package org.jenkinsci.plugins.redpen.models;

import hudson.model.Run;
import jenkins.model.Jenkins;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jenkinsci.plugins.redpen.RedpenJobProperty;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParameterModel {

    private String rootURL;
    private String secret;
    private String issueKey;
    private String logAbsolutePath;
    private String result;
    private String projectName;
    private Instant buildTriggerTime;
    private String buildNumber;
    private String e2eTestFrameWork;
    private String unitTestFrameWork;
    private String logFileLocation;
    private String coverageFrameWork;
    private String unitTestFrameWorkPath;
    private String e2eTestFrameWorkPath;
    private String coverageFrameWorkPath;
    private String userEmail;
    private String userPassword;


    public static ParameterModel getParameterModel(String secret, String issueKey, Run<?, ?> build, RedpenJobProperty redpenPluginJobProperties) {
        ParameterModel parameterModel = new ParameterModel();

        parameterModel.setSecret(secret);
        parameterModel.setIssueKey(issueKey);
        parameterModel.setLogAbsolutePath(build.getLogFile().getAbsolutePath());
        parameterModel.setLogFileLocation(redpenPluginJobProperties.getLogFileLocation());
        parameterModel.setResult(String.valueOf(build.getResult()));
        parameterModel.setProjectName(build.getParent().getName());
        parameterModel.setBuildNumber(String.valueOf(build.getNumber()));
        parameterModel.setBuildTriggerTime(build.getTime().toInstant());
        parameterModel.setE2eTestFrameWork(redpenPluginJobProperties.getE2eTestFrameWork());
        parameterModel.setE2eTestFrameWorkPath(redpenPluginJobProperties.getE2eTestFrameWorkPath());
        parameterModel.setUnitTestFrameWork(redpenPluginJobProperties.getUnitTestFrameWork());
        parameterModel.setUnitTestFrameWorkPath(redpenPluginJobProperties.getUnitTestFrameWorkPath());
        parameterModel.setCoverageFrameWork(redpenPluginJobProperties.getCoverageFrameWork());
        parameterModel.setCoverageFrameWorkPath(redpenPluginJobProperties.getCoverageFrameWorkPath());
        parameterModel.setUserEmail(redpenPluginJobProperties.getUserEmail());
        parameterModel.setUserPassword(redpenPluginJobProperties.getUserPassword().getPlainText());
        parameterModel.setRootURL(Jenkins.get().getRootUrl());

        return parameterModel;
    }
}
