package org.jenkinsci.plugins.redpen;

import hudson.Extension;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.redpen.constant.Constants;
import org.jenkinsci.plugins.redpen.ghpr.GithubPrHelper;
import org.jenkinsci.plugins.redpen.models.ParameterModel;
import org.jenkinsci.plugins.redpen.secrets.SecretRetriever;
import org.jenkinsci.plugins.redpen.service.RedpenJenkinsCore;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

@Extension
public class RedpenJenkinsListener extends RunListener<Run> {
    private static final Logger LOGGER = Logger.getLogger(RedpenJenkinsListener.class.getName());

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
                    String issueKey = getIssueKey(build);

                    SecretRetriever secretRetriever = new SecretRetriever();
                    String secret = secretRetriever.getSecretById(redpenPluginJobProperties.getCredentialId(), build);

                    if (!StringUtils.isBlank(issueKey) && secret != null) {
                        ParameterModel param = ParameterModel.getParameterModel(secret, issueKey, build,
                                redpenPluginJobProperties);

                        RedpenJenkinsCore redpenJenkinsCore = new RedpenJenkinsCore();
                        redpenJenkinsCore.doPerform(param);
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

    private String getIssueKey(Run<?, ?> build) throws IOException, InterruptedException {
        GithubPrHelper githubPrHelper = new GithubPrHelper();
        String issueKey = githubPrHelper
                .getIssueKeyFromPR(build.getEnvironment().get(Constants.GIT_BRANCH, Constants.GIT_BRANCH_MAIN));

        if (StringUtils.isBlank(issueKey)) {
            issueKey = githubPrHelper
                    .getIssueKeyFromPR(build);
        }

        return issueKey;
    }
}
