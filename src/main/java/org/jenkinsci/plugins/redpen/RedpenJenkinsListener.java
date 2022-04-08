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
import java.util.ArrayList;
import java.util.List;
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

        RedpenPluginConfig redpenPluginConfig = RedpenPluginConfig.all().get(RedpenPluginConfig.class);
        List<RedpenGlobalConfig> list = new ArrayList<>();
        if (redpenPluginConfig != null) {
            list = redpenPluginConfig.getConfigs();
        }

        Optional<Object> redpenPluginJobPropertiesOptional = build.getParent().getAllProperties().stream()
                .filter(RedpenJobProperty.class::isInstance)
                .findFirst();

        if (redpenPluginJobPropertiesOptional.isPresent()) {
            RedpenJobProperty redpenPluginJobProperties = (RedpenJobProperty) redpenPluginJobPropertiesOptional.get();
            Optional<RedpenGlobalConfig> config = list.stream().filter(redpenGlobalConfig -> redpenPluginJobProperties.getRedpenConfig().equals(redpenGlobalConfig.getName())).findFirst();

            Result result = build.getResult();
            // If the build status is not SUCCESS then
            // Add comment with log file as an attachment in the issue.
            if (result != null && result.isWorseThan(Result.SUCCESS) && config.isPresent()) {
                try {
//                    String issueKey = getIssueKey(build, redpenPluginJobProperties.getGhToken().getPlainText());
                    String issueKey = getIssueKey(build, "");

                    SecretRetriever secretRetriever = new SecretRetriever();
                    Optional<String> secret = secretRetriever.getSecretFor(config.get().getToken().getPlainText());
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

        String ghBranchName = build.getEnvironment().get(Constants.GIT_BRANCH, Constants.GIT_BRANCH_MAIN);
        String ghLink = build.getEnvironment().get(Constants.GIT_URL, "");

        String issueKey = githubPrHelper.getIssueKeyFromPR(ghBranchName);

        if (!StringUtils.isBlank(ghBranchName) && StringUtils.isBlank(issueKey) && !StringUtils.isBlank(ghLink)) {
            String[] ghRepo = ghLink.split("https://github.com/");
            issueKey = service.getIssueKeyFromPR(ghRepo[1], ghBranchName, ghToken);
        }

        return issueKey;
    }
}
