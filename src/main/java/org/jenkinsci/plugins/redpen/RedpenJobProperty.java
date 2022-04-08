package org.jenkinsci.plugins.redpen;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.ListBoxModel;
import jenkins.model.ParameterizedJobMixIn;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.redpen.constant.Constants;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.List;
import java.util.logging.Logger;

@EqualsAndHashCode(callSuper = true)
@Data
public class RedpenJobProperty extends JobProperty<Job<?, ?>> {
    private static final Logger LOGGER = Logger.getLogger(RedpenJobProperty.class.getName());

    private String logFileLocation;
    private String redpenConfig;
    private List<RedpenTestFrameworkConfig> testFramework;

    @DataBoundConstructor
    public RedpenJobProperty(String logFileLocation, String redpenConfig, List<RedpenTestFrameworkConfig> testFramework) {
        this.logFileLocation = logFileLocation;
        this.redpenConfig = redpenConfig;
        this.testFramework = testFramework;
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
        public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) {

            if (req == null) {
                return null;
            }

            RedpenJobProperty redpenPluginInstance = req.bindJSON(
                    RedpenJobProperty.class,
                    net.sf.json.JSONObject.fromObject(formData.getJSONObject(Constants.REDPEN_PLUGIN)));

            if (redpenPluginInstance == null) {
                LOGGER.fine("Couldn't bind JSON");
                return null;
            }

            if (redpenPluginInstance.redpenConfig == null) {
                LOGGER.fine("Credential not found, nullifying Redpen Plugin");
                return null;
            }

            return redpenPluginInstance;
        }

        public ListBoxModel doFillRedpenConfigItems() {
            ListBoxModel list = new ListBoxModel();
            RedpenPluginConfig redpenPluginConfig = RedpenPluginConfig.all().get(RedpenPluginConfig.class);
            List<RedpenGlobalConfig> configs = redpenPluginConfig.getConfigs();

            for (RedpenGlobalConfig config : configs) {
                list.add(config.getDisplayName(), config.getName());
            }

            return list;
        }
    }
}
