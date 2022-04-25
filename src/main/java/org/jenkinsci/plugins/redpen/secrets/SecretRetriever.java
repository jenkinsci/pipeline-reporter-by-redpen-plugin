package org.jenkinsci.plugins.redpen.secrets;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.model.Run;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import java.util.Collections;
import java.util.List;

public class SecretRetriever {
    /**
     * Get Jenkins secret by secretId
     * @param secretId SecretId specified in Jenkins credential Manager
     * @return an Optional String if secret exist
     */
    public String getSecretById(final String secretId, Run<?, ?> run) {
        StringCredentials credentialById = CredentialsProvider.findCredentialById(secretId, StringCredentials.class, run);
        return credentialById == null ? null : credentialById.getSecret().getPlainText();
    }

    /**
     * Get All Secrets
     * @return List of jenkins secrets
     */
    public List<StringCredentials> getCredential() {
        return CredentialsProvider.lookupCredentials(
                StringCredentials.class,
                Jenkins.get(),
                ACL.SYSTEM,
                Collections.emptyList());
    }
}
