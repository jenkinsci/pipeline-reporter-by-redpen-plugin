package org.jenkinsci.plugins.redpen.secrets;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SecretRetriever {
    /**
     * Get Jenkins secret by secretId
     * @param secretId SecretId specified in Jenkins credential Manager
     * @return an Optional String if secret exist
     */
    public Optional<String> getSecretFor(final String secretId) {

        final List<StringCredentials> credentials = getCredential();
        final CredentialsMatcher matcher = CredentialsMatchers.withId(secretId);

        return Optional.ofNullable(CredentialsMatchers.firstOrNull(credentials, matcher))
                .flatMap(credential -> Optional.of(credential.getSecret()))
                .flatMap(secret -> Optional.of(secret.getPlainText()));
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
