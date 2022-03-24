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
import org.jenkinsci.plugins.redpen.service.RedpenService;

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
                    String issueKey = getIssueKey(build, redpenPluginJobProperties.getGhToken().getPlainText());

                    SecretRetriever secretRetriever = new SecretRetriever();
                    Optional<String> secret = secretRetriever.getSecretFor(redpenPluginJobProperties.getCredentialId());

                    if (!StringUtils.isBlank(issueKey) && secret.isPresent()) {
                        ParameterModel param = ParameterModel.getParameterModel(secret.get(), issueKey, build,
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

    private String getIssueKey(Run build, String ghToken) throws IOException, InterruptedException {
        RedpenService service = RedpenService.getRedpenInstance();
        GithubPrHelper githubPrHelper = new GithubPrHelper();
        String issueKey = githubPrHelper
                .getIssueKeyFromPR(build.getEnvironment().get(Constants.GIT_BRANCH, Constants.GIT_BRANCH_MAIN));

        String prLink = build.getEnvironment().get("GIT_URL" , "");

        if(!StringUtils.isBlank(prLink)) {
            System.out.println("ghToken " + ghToken);
            System.out.println("url" + prLink.split("https://github.com/"));
            service.getPR(prLink, ghToken);
        }


        if (StringUtils.isBlank(issueKey)) {
            issueKey = githubPrHelper
                    .getIssueKeyFromPR(build);
        }

        return issueKey;
    }
}
