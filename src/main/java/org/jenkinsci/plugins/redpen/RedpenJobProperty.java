package org.jenkinsci.plugins.redpen;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.ListBoxModel;
import jenkins.model.ParameterizedJobMixIn;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.redpen.models.Constants;
import org.jenkinsci.plugins.redpen.secrets.SecretRetriever;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@EqualsAndHashCode(callSuper = true)
@Data
public class RedpenJobProperty extends JobProperty<Job<?, ?>> {
    private String credentialId;
    private String logFileLocation;
    private String unitTestFrameWork;
    private String e2eTestFrameWork;

    @DataBoundConstructor
    public RedpenJobProperty(String credentialId, String logFileLocation, String unitTestFrameWork, String e2eTestFrameWork) {
        this.logFileLocation = logFileLocation;
        this.unitTestFrameWork = unitTestFrameWork;
        this.e2eTestFrameWork = e2eTestFrameWork;
        this.credentialId = credentialId;
    }

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return ParameterizedJobMixIn.ParameterizedJob.class.isAssignableFrom(jobType);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Redpen General";
        }

        @Override
        public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws Descriptor.FormException {

            if (req != null)  {
                RedpenJobProperty redpenPluginInstance = req.bindJSON(
                        RedpenJobProperty.class,
                        net.sf.json.JSONObject.fromObject(formData.getJSONObject(Constants.REDPEN_PLUGIN))
                );
                if (redpenPluginInstance == null) {
                    LOGGER.fine("Couldn't bind JSON");
                    return null;
                }
                if (redpenPluginInstance.credentialId == null) {
                    LOGGER.fine("Credential not found, nullifying Redpen Plugin");
                    return null;
                }
                return redpenPluginInstance;
            }
            return null;
        }

        public ListBoxModel doFillUnitTestFrameWorkItems() {

            ListBoxModel list = new ListBoxModel();
            list.add(Constants.NONE_DISPLAY_NAME, null);
            list.add(Constants.JUNIT_DISPLAY_NAME, Constants.JUNIT);
            list.add(Constants.NUNIT_DISPLAY_NAME, Constants.NUNIT);
            list.add(Constants.JACOCO_DISPLAY_NAME, Constants.JACOCO);
            list.add(Constants.JEST_DISPLAY_NAME, Constants.JEST);

            return list;
        }

        public ListBoxModel doFillE2eTestFrameWorkItems() {

            ListBoxModel list = new ListBoxModel();
            list.add(Constants.NONE_DISPLAY_NAME, null);
            list.add(Constants.WEB_DRIVER_IO_DISPLAY_NAME, Constants.WEB_DRIVER_IO);

            return list;
        }

        public ListBoxModel doFillCredentialIdItems(
                @QueryParameter String credentialsId
        ) {
            List<CredentialsMatcher> matchers = new ArrayList<>();
            if (!StringUtils.isEmpty(credentialsId)) {
                matchers.add(0, CredentialsMatchers.withId(credentialsId));
            }

            matchers.add(CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class));
            matchers.add(CredentialsMatchers.instanceOf(StringCredentials.class));

            SecretRetriever secretRetriever = new SecretRetriever();

            List<StringCredentials> credentials = secretRetriever.getCredential();

            return new StandardListBoxModel()
                    .withMatching(
                            CredentialsMatchers.anyOf(
                                    matchers.toArray(new CredentialsMatcher[0])),
                            credentials
                    );
        }


    }

    private static final Logger LOGGER = Logger.getLogger(RedpenJobProperty.class.getName());
}
