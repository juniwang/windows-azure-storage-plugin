package com.microsoft.test.RegressionTestFrame;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoftopentechnologies.windowsazurestorage.helper.AzureCredentials;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.File;
import java.util.UUID;

/**
 * Created by t-yuhang on 9/1/2017.
 */
public class CreateRandomFilesOnAzureBuilder extends Builder implements SimpleBuildStep {

    private final String storageCredentialId;
    private final String containerName;
    private final int fileNum;
    private String filePattern = "";
    private int fileLen;

    @DataBoundConstructor
    public CreateRandomFilesOnAzureBuilder(final String storageCredentialId,
                                           final String containerName, final int fileNum) {
        this.storageCredentialId = storageCredentialId;
        this.containerName = containerName;
        this.fileNum = fileNum;
    }

    @DataBoundSetter
    public void setFilePattern(final String filepattern) {
        this.filePattern = filepattern;
    }

    @DataBoundSetter
    public void setFileLen(final int fileLen) {
        this.fileLen = fileLen;
    }

    public String getStorageCredentialId() {
        return this.storageCredentialId;
    }

    public String getContainerName() {
        return this.containerName;
    }

    public int getFileNum() {
        return this.fileNum;
    }

    public String getFilePattern() {
        return this.filePattern;
    }

    public int getFileLen() {
        return this.fileLen;
    }

    private String generateRandomString(final int length) {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("[^a-z0-9]", "a").substring(0, length);
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

            CloudStorageAccount account = new CloudStorageAccount(
                    new StorageCredentialsAccountAndKey(azureStorageAccountName, azureStorageAccountKey));
            CloudBlobClient blobClient = account.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference(expContainerName);
            container.createIfNotExists();

            for (int i = 0; i < fileNum; i++) {
                File file = new File(UUID.randomUUID().toString() + filePattern);
                String content = generateRandomString(fileLen);
                FileUtils.writeStringToFile(file, content);
                CloudBlockBlob blob = container.getBlockBlobReference(file.getName());
                blob.uploadFromFile(file.getAbsolutePath());
                file.delete();
            }
        } catch (Exception ex) {

        }
    }

    @Override
    public CreateRandomFilesOnAzureDescriptor getDescriptor() {
        return (CreateRandomFilesOnAzureDescriptor) super.getDescriptor();
    }

    @Extension
    public static final class CreateRandomFilesOnAzureDescriptor extends BuildStepDescriptor<Builder> {

        public CreateRandomFilesOnAzureDescriptor() {
            load();
        }

        public boolean isApplicable(final Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Create Random Files On Azure";
        }
    }
}
