package org.jenkinsci.plugins.redpen;

import hudson.Extension;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.redpen.ghpr.GithubPrHelper;
import org.jenkinsci.plugins.redpen.models.Constants;
import org.jenkinsci.plugins.redpen.secrets.SecretRetriever;
import org.redpen.RedpenJenkinsCore;
import org.redpen.model.ParameterModel;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

@Extension
public class RedpenJenkinsListener extends RunListener<Run> {

    public RedpenJenkinsListener() {
        super(Run.class);
    }

    @Override
    public void onFinalized(Run build) {

        Optional<Object> redpenPluginJobPropertiesOptional = build.getParent().getAllProperties().stream()
                .filter(RedpenJobProperty.class::isInstance)
                .findFirst();

        if (redpenPluginJobPropertiesOptional.isPresent()) {
            RedpenJobProperty redpenPluginJobProperties = (RedpenJobProperty) redpenPluginJobPropertiesOptional.get();

            Result result = build.getResult();
            // If the build status is not SUCCESS then
            // Add comment with log file as an attachment in the issue.
            if (result != null && result.isWorseThan(Result.SUCCESS)) {
                try {
                    GithubPrHelper githubPrHelper = new GithubPrHelper();
                    String issueKey = githubPrHelper
                            .getIssueKeyFromPR(build.getEnvironment().get(Constants.GIT_BRANCH, Constants.GIT_BRANCH_MAIN));
                    if (StringUtils.isBlank(issueKey)) {
                        issueKey = githubPrHelper
                                .getIssueKeyFromPR(build);
                    }
                    if (!StringUtils.isBlank(issueKey)) {
                        SecretRetriever secretRetriever = new SecretRetriever();
                        Optional<String> secret = secretRetriever.getSecretFor(redpenPluginJobProperties.getCredentialId());
                        if (secret.isPresent()) {
                            ParameterModel param = getParameterModel(secret.get(), issueKey, build,
                                    redpenPluginJobProperties);

                            RedpenJenkinsCore redpenJenkinsCore = new RedpenJenkinsCore();
                            redpenJenkinsCore.doPerform(param);

                        }
                    }
                } catch (IOException e) {
                    LOGGER.warning(e.getMessage());
                } catch (InterruptedException e) {
                    LOGGER.warning(e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private ParameterModel getParameterModel(String secret, String issueKey, Run build, RedpenJobProperty redpenPluginJobProperties) {
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

    private static final Logger LOGGER = Logger.getLogger(RedpenJenkinsListener.class.getName());
}
