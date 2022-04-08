package org.jenkinsci.plugins.redpen.models;

import hudson.model.Run;
import jenkins.model.Jenkins;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.redpen.RedpenJobProperty;
import org.jenkinsci.plugins.redpen.RedpenTestFrameworkConfig;
import org.jenkinsci.plugins.redpen.service.RedpenJenkinsCore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParameterModel {

    private String rootURL;
    private String secret;
    private String issueKey;
    private String logAbsolutePath;
    private String displayName;
    private String result;
    private String projectName;
    private Instant buildTriggerTime;
    private String buildNumber;
    private List<String> paths;
    private String logFileLocation;

    public static ParameterModel getParameterModel(String secret, String issueKey, Run build, RedpenJobProperty redpenPluginJobProperties) {
        ParameterModel parameterModel = new ParameterModel();

        parameterModel.setSecret(secret);
        parameterModel.setIssueKey(issueKey);
        parameterModel.setLogAbsolutePath(build.getLogFile().getAbsolutePath());
        parameterModel.setLogFileLocation(redpenPluginJobProperties.getLogFileLocation());
        parameterModel.setDisplayName(build.getDisplayName());
        parameterModel.setResult(String.valueOf(build.getResult()));
        parameterModel.setProjectName(build.getParent().getName());
        parameterModel.setBuildNumber(build.getSearchUrl());
        parameterModel.setBuildTriggerTime(build.getTime().toInstant());
        List<String> pathList = new ArrayList<>();
        for (RedpenTestFrameworkConfig redpenTestFrameworkConfig :
                redpenPluginJobProperties.getTestFramework()) {
            Optional<TestFrameWork> availableInList = RedpenJenkinsCore.isAvailableInList(redpenTestFrameworkConfig.getTestFrameWork());
            if (availableInList.isPresent() && StringUtils.isEmpty(redpenTestFrameworkConfig.getTestFrameWorkPath())) {
                pathList.add(availableInList.get().getPath());
            } else {
                pathList.add(redpenTestFrameworkConfig.getTestFrameWorkPath());
            }
        }
        parameterModel.setPaths(pathList);
        parameterModel.setRootURL(Jenkins.get().getRootUrl());

        return parameterModel;
    }
}
