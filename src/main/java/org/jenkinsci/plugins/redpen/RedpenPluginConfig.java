package org.jenkinsci.plugins.redpen;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@EqualsAndHashCode(callSuper = true)
@Extension
@Data
public class RedpenPluginConfig extends GlobalConfiguration {
    private static final Logger LOGGER = Logger.getLogger(RedpenPluginConfig.class.getName());

    public static RedpenPluginConfig get() {
        return GlobalConfiguration.all().get(RedpenPluginConfig.class);
    }

    private List<RedpenGlobalConfig> configs = new ArrayList<>();

    public List<RedpenGlobalConfig> getConfigs() {
        return configs;
    }

    @DataBoundConstructor
    public RedpenPluginConfig(List<RedpenGlobalConfig> configs) {
        this.configs = configs;
    }

    @DataBoundSetter
    public void setConfigs(List<RedpenGlobalConfig> configs) {
        this.configs = configs;
        save();
    }

    public RedpenPluginConfig() {
        load();
    }
}
