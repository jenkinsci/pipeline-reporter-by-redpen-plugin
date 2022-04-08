package org.jenkinsci.plugins.redpen;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.AbstractIdCredentialsListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.redpen.constant.Constants;
import org.jenkinsci.plugins.redpen.secrets.SecretRetriever;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Extension
@Data
public class RedpenGlobalConfig extends AbstractDescribableImpl<RedpenGlobalConfig> {
    private String name;
    private Secret token;

    public static final String REDPEN_TITLE = "Configure Redpen Token";

    public RedpenGlobalConfig() {
    }

    @DataBoundConstructor
    public RedpenGlobalConfig(String name, Secret token) {
        this.name = name;
        this.token = token;
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = name;
    }

    public Secret getToken() {
        return token;
    }

    @DataBoundSetter
    public void setToken(Secret token) {
        this.token = token;
    }

    public String getDisplayName() {
        return getName();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<RedpenGlobalConfig> {

        @NonNull
        @Override
        public String getDisplayName() {
            return REDPEN_TITLE;
        }

        public ListBoxModel doFillTokenItems(@QueryParameter String credentialsId) {
            List<CredentialsMatcher> matchers = new ArrayList<>();
            if (!StringUtils.isEmpty(credentialsId)) {
                matchers.add(0, CredentialsMatchers.withId(credentialsId));
            }

            matchers.add(CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class));
            matchers.add(CredentialsMatchers.instanceOf(StringCredentials.class));

            SecretRetriever secretRetriever = new SecretRetriever();

            List<StringCredentials> credentials = secretRetriever.getCredential();

            AbstractIdCredentialsListBoxModel<StandardListBoxModel, StandardCredentials> options = new StandardListBoxModel()
                    .withMatching(
                            CredentialsMatchers.anyOf(
                                    matchers.toArray(new CredentialsMatcher[0])),
                            credentials);

            ListBoxModel listBoxModel = new ListBoxModel();
            listBoxModel.add(Constants.NONE_DISPLAY_NAME, "");
            listBoxModel.addAll(options);

            return listBoxModel;
        }
    }
}
