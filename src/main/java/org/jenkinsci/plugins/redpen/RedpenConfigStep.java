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
import okhttp3.Response;
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
    private String userServiceConnectionId;

    @DataBoundConstructor
    public RedpenConfigStep(String userServiceConnectionId) {
        this.userServiceConnectionId = userServiceConnectionId;
    }

    public String getUserServiceConnectionId() {
        return userServiceConnectionId;
    }

    public void setUserServiceConnectionId(String userServiceConnectionId) {
        this.userServiceConnectionId = userServiceConnectionId;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                           BuildListener listener) throws InterruptedException, IOException {
        Result result = build.getResult();

        // If the build status is not SUCCESS then
        // call the redpen and add comment with log file as an attachment in the issue.
//        if (result != null && result.isWorseThan(Result.SUCCESS)) {
            GithubPrHelper githubPrHelper = new GithubPrHelper();
            String issueKey = githubPrHelper.getIssueKeyFromPR(build);


//            SecretRetriever secretRetriever = new SecretRetriever();
            RedpenService redpenService = RedpenService.getRedpenInstance();
        Response jwt = redpenService.getJWT("29dce7bc-da7a-429d-b1e4-8e8cf69acac7");
//            Optional<String> mayBeKey = secretRetriever.getSecretFor("PRIVATE_KEY_CONTENT");

                String token = null;
                try {
                    token = jwt.message();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                redpenService.addAttachment(build, issueKey, token);
                redpenService.addComment(build, issueKey, token);
                return true;
//            }

//        }
//        return true;
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
            return "Redpen user service connection id";
        }
    }
}

