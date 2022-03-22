package org.jenkinsci.plugins.redpen.ghpr;

import hudson.model.Job;
import hudson.model.Run;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.ghprb.Ghprb;
import org.jenkinsci.plugins.ghprb.GhprbCause;
import org.jenkinsci.plugins.ghprb.GhprbTrigger;
import org.jenkinsci.plugins.redpen.util.IssueKeyExtractor;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHPullRequest;

import java.io.IOException;

public class GithubPrHelper {

    public String getIssueKeyFromPR(Run<?, ?> build) throws IOException {
        GHPullRequest pullRequest = this.getGithubPR(build);
        String issueKeyFromBranch = this.getIssueKeyFromString(pullRequest.getHead().getRef());

        if (!StringUtils.isBlank(issueKeyFromBranch)) {
            return issueKeyFromBranch;
        }

        String issueKeyFromBranchName = this.getIssueKeyFromString(pullRequest.getTitle());

        if (!StringUtils.isBlank(issueKeyFromBranchName)) {
            return issueKeyFromBranchName;
        }

        return this.getIssueKeyFromString(pullRequest.getBody());
    }

    private GHPullRequest getGithubPR(Run<?, ?> build) throws IOException {
        Job<?, ?> project = build.getParent();
        GhprbTrigger trigger = Ghprb.extractTrigger(project);

        if (trigger == null) {
            throw new GHFileNotFoundException("Trigger is not defined");
        }

        // If build is started manually then cause will be null.
        GhprbCause cause = Ghprb.getCause(build);
        if (cause == null) {
            throw new GHFileNotFoundException("Cause in not defined");
        }

        return trigger.getRepository().getActualPullRequest(cause.getPullID());
    }

    public String getIssueKeyFromPR(String branchName) {
        return getIssueKeyFromString(branchName);
    }

    private String getIssueKeyFromString(String string) {
        return IssueKeyExtractor.extractIssueKey(string);
    }
}
