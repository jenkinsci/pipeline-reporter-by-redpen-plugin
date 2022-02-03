package org.jenkinsci.plugins.redpen.ghpr;

import hudson.model.AbstractBuild;
import hudson.model.Job;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.redpen.util.IssueKeyExtractor;
import org.jenkinsci.plugins.ghprb.Ghprb;
import org.jenkinsci.plugins.ghprb.GhprbCause;
import org.jenkinsci.plugins.ghprb.GhprbTrigger;
import org.kohsuke.github.GHPullRequest;

import java.io.IOException;

public class GithubPrHelper {

    public String getIssueKeyFromPR(AbstractBuild<?, ?> build) throws IOException {
        GHPullRequest pullRequest = this.getGithubPR(build);

        String issueKeyFromBranch = this.getIssueKeyFromDesc(pullRequest.getHead().getRef());

        if(!StringUtils.isBlank(issueKeyFromBranch)) {
            return issueKeyFromBranch;
        }

        String issueKeyFromBranchName = this.getIssueKeyFromDesc(pullRequest.getTitle());

        if(!StringUtils.isBlank(issueKeyFromBranchName)) {
            return issueKeyFromBranchName;
        }

        return this.getIssueKeyFromDesc(pullRequest.getBody());
    }

    private GHPullRequest getGithubPR(AbstractBuild<?, ?> build) throws IOException {
        Job<?, ?> project = build.getParent();
        GhprbTrigger trigger = Ghprb.extractTrigger(project);
        if (trigger == null) {
            throw new RuntimeException("Trigger is not defined");
        }

        // If build is started manually then cause will be null.
        GhprbCause cause = Ghprb.getCause(build);
        if (cause == null) {
            throw new RuntimeException("Cause in not defined");
        }

        return trigger.getRepository()
                .getActualPullRequest(cause.getPullID());
    }

    private String getIssueKeyFromDesc(String description) {
        return IssueKeyExtractor.extractIssueKey(description);
    }
}
