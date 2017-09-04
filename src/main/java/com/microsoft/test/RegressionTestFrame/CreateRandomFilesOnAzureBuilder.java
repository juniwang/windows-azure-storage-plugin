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

import java.io.File;
import java.util.UUID;

/**
 * Created by t-yuhang on 9/1/2017.
 */
public class CreateRandomFilesOnAzureBuilder extends Builder implements SimpleBuildStep {

    private final String storageCredentialId;
    private final String containerName;
    private int filenum;
    private String filepattern = ".txt";
    private int filelen;

    @DataBoundConstructor
    public CreateRandomFilesOnAzureBuilder(final String storageCredentialId, final String containerName,
                                           final int filenum, final String filepattern, final int filelen) {
        this.storageCredentialId = storageCredentialId;
        this.containerName = containerName;
        this.filenum = filenum;
        this.filepattern = filepattern;
        this.filelen = filelen;
    }

    public String getStorageCredentialId() {
        return this.storageCredentialId;
    }

    public String getContainerName() {
        return this.containerName;
    }

    public int getFilenum() {
        return this.filenum;
    }

    public String getFilepattern() {
        return this.filepattern;
    }

    public int getFilelen() {
        return this.filelen;
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

            for (int i = 0; i < filenum; i++) {
                File file = new File(UUID.randomUUID().toString() + filepattern);
                String content = generateRandomString(filelen);
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
