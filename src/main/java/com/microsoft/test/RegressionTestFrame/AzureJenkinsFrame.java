package com.microsoft.test.RegressionTestFrame;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoftopentechnologies.windowsazurestorage.helper.AzureCredentials;
import hudson.EnvVars;
import hudson.Util;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.security.ACL;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import javax.xml.transform.Source;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by t-yuhang on 8/21/2017.
 */
public class AzureJenkinsFrame {

    private Source source;

    public AzureJenkinsFrame(final Source source) {
        this.source = source;
    }

    @Rule
    public JenkinsRule j = new JenkinsRule();

    protected AzureCredentials getAzureCredentials(final String storageCredentialId) throws Exception {
        AzureCredentials azureCredentials = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        AzureCredentials.class,
                        Jenkins.getInstance(),
                        ACL.SYSTEM,
                        Collections.<DomainRequirement>emptyList()),
                CredentialsMatchers.withId(storageCredentialId));
        return azureCredentials;
    }

    protected String getEnvVar(final String key) {
        return getEnvVar(key, "");
    }

    protected String getEnvVar(final String key, final String value) {
        DescribableList<NodeProperty<?>, NodePropertyDescriptor> descriptorDescribableList =
                j.jenkins.getGlobalNodeProperties();
        List<EnvironmentVariablesNodeProperty> environmentVariablesNodePropertyList =
                descriptorDescribableList.getAll(EnvironmentVariablesNodeProperty.class);
        if (environmentVariablesNodePropertyList.size() == 1) {
            return environmentVariablesNodePropertyList.get(0).getEnvVars().get(key, value);
        } else {
            return value;
        }
    }

    protected EnvVars getEnvVars() {
        DescribableList<NodeProperty<?>, NodePropertyDescriptor> descriptorDescribableList =
                j.jenkins.getGlobalNodeProperties();
        List<EnvironmentVariablesNodeProperty> environmentVariablesNodePropertyList =
                descriptorDescribableList.getAll(EnvironmentVariablesNodeProperty.class);
        if (environmentVariablesNodePropertyList.size() == 1) {
            return environmentVariablesNodePropertyList.get(0).getEnvVars();
        } else {
            return new EnvVars();
        }
    }

    protected void deleteContainerOnAzure(final String storageCredentialId,
                                          final String containerName) throws Exception {
            AzureCredentials.StorageAccountCredential storageAccountCredential =
                    AzureCredentials.getStorageAccountCredential(storageCredentialId);
            String azureStorageAccountName = storageAccountCredential.getStorageAccountName();
            String azureStorageAccountKey = storageAccountCredential.getStorageAccountKey();

            final EnvVars envVars = getEnvVars();
            String expContainerName = Util.replaceMacro(containerName, envVars);

            CloudStorageAccount account = new CloudStorageAccount(new StorageCredentialsAccountAndKey(
                    azureStorageAccountName, azureStorageAccountKey));
            CloudBlobClient blobClient = account.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference(expContainerName);
            container.deleteIfExists();
    }

    protected void deleteDirAtLocal(final String filepath) throws Exception {
        File dir = new File(filepath);
        dir.deleteOnExit();
    }

    protected void getJenkinsVersion() {
        j.jenkins.
    }

    @Test
    public void test() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.updateByXml(this.source);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        List<String> loggList = build.getLog(16);
        assertEquals(Result.SUCCESS, build.getResult());
    }
}