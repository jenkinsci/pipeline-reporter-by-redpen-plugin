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
import org.jenkinsci.plugins.redpen.redpenservices.RedpenService;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

public class RedpenConfigStep extends Recorder {
    private String redpenWidgetId;

    @DataBoundConstructor
    public RedpenConfigStep(String redpenWidgetId) {
        this.redpenWidgetId = redpenWidgetId;
    }

    public String getRedpenWidgetId() {
        return redpenWidgetId;
    }

    public void setRedpenWidgetId(String redpenWidgetId) {
        this.redpenWidgetId = redpenWidgetId;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                           BuildListener listener) throws InterruptedException, IOException {
        Result result = build.getResult();

        // If the build status is not SUCCESS then
        // call the redpen and add comment with log file as an attachment in the issue.
        if (result != null && result.isWorseThan(Result.SUCCESS)) {
            GithubPrHelper githubPrHelper = new GithubPrHelper();
            String issueKey = githubPrHelper.getIssueKeyFromPR(build);
            String widgetId = this.redpenWidgetId;

            RedpenService redpenService = RedpenService.getRedpenInstance();
        String jwt = redpenService.getJWT(widgetId);

                String token = null;
                try {
                    JSONObject json = new JSONObject(jwt);

                    token = json.getString("jwt");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                redpenService.addAttachment(build,widgetId, issueKey, token);
//                redpenService.addComment(build, issueKey, token);
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
            return "Redpen Widget id";
        }
    }
}

