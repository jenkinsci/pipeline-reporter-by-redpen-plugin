package org.jenkinsci.plugins.redpen;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.jenkinsci.plugins.redpen.ghpr.GithubPrHelper;
import org.jenkinsci.plugins.redpen.jwt.JWTUtility;
import org.jenkinsci.plugins.redpen.redpenservices.RedpenService;
import org.jenkinsci.plugins.redpen.secrets.SecretRetriever;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

public class RedpenConfigStep extends Recorder {
    private String serviceConnectionId;

    @DataBoundConstructor
    public RedpenConfigStep(String serviceConnectionId) {
        this.serviceConnectionId = serviceConnectionId;
    }

    public String getServiceConnectionId() {
        return serviceConnectionId;
    }

    public void setServiceConnectionId(String serviceConnectionId) {
        this.serviceConnectionId = serviceConnectionId;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        Result result = build.getResult();

        // If the build status is not SUCCESS then
        // call the redpen and add comment with log file as an attachment in the issue.
        if (result != null && result.isWorseThan(Result.SUCCESS)) {
            GithubPrHelper githubPrHelper = new GithubPrHelper();
            String issueKey = githubPrHelper.getIssueKeyFromPR(build);
            SecretRetriever secretRetriever = new SecretRetriever();
            Optional<String> mayBeKey = secretRetriever.getSecretFor("PRIVATE_KEY_CONTENT");

            RedpenService redpenService = RedpenService.getRedpenInstance();
            if (mayBeKey.isPresent()) {
                try {
                    String jwtToken = JWTUtility.getJWTToken(mayBeKey.get(), this.serviceConnectionId);
                    redpenService.addAttachment(build, issueKey, jwtToken, build.getLogFile().getAbsolutePath());
                    redpenService.addAttachment(build, issueKey, jwtToken,
                            "work/workspace/Jenkins Test Project React/logs/tmpDir/React App - Home Screen/should have data of customer/screenshot_1.png");
                    redpenService.addAttachment(build, issueKey, jwtToken,
                            "work/workspace/Jenkins Test Project React/logs/tmpDir/React App - Home Screen/should have data of customer/screenshot_1.png");
                    redpenService.addComment(build, issueKey, jwtToken);
                    redpenService.addComment(build, issueKey, jwtToken);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class aClass) {
            return true;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Redpen ServiceConnectionId";
        }
    }
}
