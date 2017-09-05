package com.microsoft.test.RegressionTestFrame;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.microsoftopentechnologies.windowsazurestorage.helper.AzureCredentials;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by t-yuhang on 9/4/2017.
 */
public class SetAzureCredentialsBuilder extends Builder implements SimpleBuildStep {
    private final String storageCredentialId;

    @DataBoundConstructor
    public SetAzureCredentialsBuilder(final String storageCredentialId) {
        this.storageCredentialId = storageCredentialId;
    }

    public String getStorageCredentialId() {
        return this.storageCredentialId;
    }

    private String loadFromEnv(final String name) {
        return loadFromEnv(name, "");
    }

    private String loadFromEnv(final String name, final String defaultValue) {
        final String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        } else {
            return value;
        }
    }

    @Override
    public void perform(final Run<?, ?> build, final FilePath workspace,
                        final Launcher launcher, final TaskListener listener) {
        try {
            String azureStorageAccountName = loadFromEnv("AZURE_STORAGE_TEST_STORAGE_ACCOUNT_NAME");
            String azureStorageAccountKey1 = loadFromEnv("AZURE_STORAGE_TEST_STORAGE_ACCOUNT_KEY1");
            String azureStorageAccountKey2 = loadFromEnv("AZURE_STORAGE_TEST_STORAGE_ACCOUNT_KEY2");
            String blobURL = loadFromEnv("BlobURL");

            AzureCredentials azureCredentials = new AzureCredentials(CredentialsScope.GLOBAL, storageCredentialId,
                    null, azureStorageAccountName, azureStorageAccountKey1, blobURL);

            CredentialsStore credentialsStore = CredentialsProvider.lookupStores(
                    Jenkins.getInstance()).iterator().next();
            credentialsStore.addCredentials(Domain.global(), azureCredentials);
        } catch (Exception ex) {

        }
    }

    @Override
    public SetAzureCredentialsDescriptor getDescriptor() {
        return (SetAzureCredentialsDescriptor) super.getDescriptor();
    }

    @Extension
    public static final class SetAzureCredentialsDescriptor extends BuildStepDescriptor<Builder> {

        public SetAzureCredentialsDescriptor() {
            load();
        }

        public boolean isApplicable(final Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Set Azure Credentials";
        }
    }
}
