package com.microsoft.test.RegressionTestFrame;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoftopentechnologies.windowsazurestorage.helper.AzureCredentials;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

public class DeleteContainerOnAzureBuilder extends Builder implements SimpleBuildStep {

    private final String storageCredentialId;
    private final String containerName;

    @DataBoundConstructor
    public DeleteContainerOnAzureBuilder(final String storageCredentialId, final String containerName) {
        this.storageCredentialId = storageCredentialId;
        this.containerName = containerName;
    }

    public String getStorageCredentialId() {
        return this.storageCredentialId;
    }

    public String getContainerName() {
        return this.containerName;
    }

    @Override
    public void perform(final Run<?, ?> build, final FilePath workspace,
                        final Launcher launcher, final TaskListener listener) {
        try {
            AzureCredentials.StorageAccountCredential storageAccountCredential =
                    AzureCredentials.getStorageAccountCredential(storageCredentialId);
            String azureStorageAccountName = storageAccountCredential.getStorageAccountName();
            String azureStorageAccountKey = storageAccountCredential.getStorageAccountKey();

            final EnvVars envVars = build.getEnvironment(listener);
            String expContainerName = Util.replaceMacro(containerName, envVars);

            CloudStorageAccount account = new CloudStorageAccount(new StorageCredentialsAccountAndKey(
                    azureStorageAccountName, azureStorageAccountKey));
            CloudBlobClient blobClient = account.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference(expContainerName);
            container.deleteIfExists();
        } catch (Exception ex) {

        }
    }

    @Override
    public DeleteContainerOnAzureDescriptor getDescriptor() {
        return (DeleteContainerOnAzureDescriptor) super.getDescriptor();
    }

    @Extension
    public static final class DeleteContainerOnAzureDescriptor extends BuildStepDescriptor<Builder> {

        public DeleteContainerOnAzureDescriptor() {
            load();
        }

        public boolean isApplicable(final Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Delete Container On Azure";
        }
    }
}
