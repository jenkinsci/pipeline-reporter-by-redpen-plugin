package org.jenkinsci.plugins.redpen;

import hudson.Extension;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.redpen.ghpr.GithubPrHelper;
import org.jenkinsci.plugins.redpen.models.Constants;
import org.jenkinsci.plugins.redpen.models.ParameterModel;
import org.jenkinsci.plugins.redpen.secrets.SecretRetriever;

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
                        RedpenJenkinsLogic redpenJenkinsLogic = new RedpenJenkinsLogic();
                        if (secret.isPresent()) {
                            ParameterModel param = redpenJenkinsLogic.getParameterModel(secret.get(), issueKey, build,
                                    redpenPluginJobProperties);
                            redpenJenkinsLogic.doPerform(param);
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

    private static final Logger LOGGER = Logger.getLogger(RedpenJenkinsListener.class.getName());
}
