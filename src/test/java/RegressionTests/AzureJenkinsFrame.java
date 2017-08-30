package RegressionTests;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoftopentechnologies.windowsazurestorage.helper.AzureCredentials;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.security.ACL;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import javax.xml.transform.Source;

import java.util.*;
import java.io.File;

/**
 * Created by t-yuhang on 8/21/2017.
 */
public class AzureJenkinsFrame {

    protected Source source;

    public AzureJenkinsFrame(Source source) {
        this.source = source;
    }

    private FreeStyleProject project;

    private FreeStyleBuild build;

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private String loadFromEnv(final String name) {
        return loadFromEnv(name, "");
    }

    private String loadFromEnv(final String name, final String defaultValue) {
        final String value = System.getenv(name);
        if(value == null || value.isEmpty()) {
            return defaultValue;
        }
        else {
            return value;
        }
    }

    protected void setAzureCredentials(String storageCredentialId) throws Exception {
        String azureStorageAccountName = loadFromEnv("AZURE_STORAGE_TEST_STORAGE_ACCOUNT_NAME");
        String azureStorageAccountKey1 = loadFromEnv("AZURE_STORAGE_TEST_STORAGE_ACCOUNT_KEY1");
        String azureStorageAccountKey2 = loadFromEnv("AZURE_STORAGE_TEST_STORAGE_ACCOUNT_KEY2");
        String blobURL = loadFromEnv("BlobURL");

        AzureCredentials azureCredentials = new AzureCredentials(CredentialsScope.GLOBAL, storageCredentialId,
                null, azureStorageAccountName, azureStorageAccountKey1, blobURL);

        CredentialsStore credentialsStore = CredentialsProvider.lookupStores(Jenkins.getInstance()).iterator().next();
        credentialsStore.addCredentials(Domain.global(), azureCredentials);
    }

    protected AzureCredentials getAzureCredentials(String storageCredentialId) throws Exception {
        AzureCredentials azureCredentials = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        AzureCredentials.class,
                        Jenkins.getInstance(),
                        ACL.SYSTEM,
                        Collections.<DomainRequirement>emptyList()),
                CredentialsMatchers.withId(storageCredentialId));
        return azureCredentials;
    }

    protected String GenerateRandomString(int length) {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("[^a-z0-9]", "a").substring(0, length);
    }

    protected void setEnvVar(final String key) throws Exception {
        setEnvVar(key, "");
    }

    protected void setEnvVar(final String key, final String value) throws Exception {
        DescribableList<NodeProperty<?>, NodePropertyDescriptor> descriptorDescribableList = j.jenkins.getGlobalNodeProperties();
        List<EnvironmentVariablesNodeProperty> environmentVariablesNodePropertyList = descriptorDescribableList.getAll(EnvironmentVariablesNodeProperty.class);
        if(environmentVariablesNodePropertyList.size() == 0) {
            descriptorDescribableList.add(new EnvironmentVariablesNodeProperty(new EnvironmentVariablesNodeProperty.Entry(key, value)));
        }
        else if(environmentVariablesNodePropertyList.size() == 1) {
            environmentVariablesNodePropertyList.get(0).getEnvVars().put(key, value);
        }
        else {

        }
    }

    protected String getEnvVar(final String key) {
        return getEnvVar(key, "");
    }

    protected String getEnvVar(final String key, final String value) {
        DescribableList<NodeProperty<?>, NodePropertyDescriptor> descriptorDescribableList = j.jenkins.getGlobalNodeProperties();
        List<EnvironmentVariablesNodeProperty> environmentVariablesNodePropertyList = descriptorDescribableList.getAll(EnvironmentVariablesNodeProperty.class);
        if(environmentVariablesNodePropertyList.size() == 1) {
            return environmentVariablesNodePropertyList.get(0).getEnvVars().get(key, value);
        }
        else {
            return value;
        }
    }

    protected void generateRandomFilesOnAzure(String storageCredentialId, String containername, int num, String filepattern) throws Exception {
        AzureCredentials azureCredentials = getAzureCredentials(storageCredentialId);
        generateRandomFilesOnAzure(azureCredentials.getStorageCred().getStorageAccountName(), azureCredentials.getStorageCred().getStorageAccountKey(), containername, num, filepattern);
    }

    protected void generateRandomFilesOnAzure(String azureStorageAccountName, String azureStorageAccountKey, String containername, int num, String filepattern) throws Exception {
        CloudStorageAccount account = new CloudStorageAccount(new StorageCredentialsAccountAndKey(
                azureStorageAccountName, azureStorageAccountKey));
        CloudBlobClient blobClient = account.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference(containername);
        container.createIfNotExists();

        for(int i = 0; i < num; i ++) {
            File file = new File(UUID.randomUUID().toString() + filepattern);
            String content = GenerateRandomString(32);
            FileUtils.writeStringToFile(file, content);
            CloudBlockBlob blob = container.getBlockBlobReference(file.getName());
            blob.uploadFromFile(file.getAbsolutePath());
            file.delete();
        }
    }

    protected void generateRandomFileAtLocal(String filepath, int num, String filepattern) throws Exception {
        for(int i = 0; i < num; i ++) {
            File file = new File(filepath, UUID.randomUUID().toString() + filepattern);
            String content = GenerateRandomString(32);
            FileUtils.writeStringToFile(file, content);
        }
    }

    protected void deleteContainerOnAzure(String storageCredentialId, String containername) throws Exception {
        AzureCredentials azureCredentials = getAzureCredentials(storageCredentialId);
        deleteContainerOnAzure(azureCredentials.getStorageCred().getStorageAccountName(), azureCredentials.getStorageCred().getStorageAccountKey(), containername);
    }

    protected void deleteContainerOnAzure(String azureStorageAccountName, String azureStorageAccountKey, String containername) throws Exception {
        CloudStorageAccount account = new CloudStorageAccount(new StorageCredentialsAccountAndKey(
                azureStorageAccountName, azureStorageAccountKey));
        CloudBlobClient blobClient = account.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference(containername);
        container.deleteIfExists();
    }

    protected void deleteDirAtLocal(String filepath) throws Exception {
        File dir = new File(filepath);
        dir.deleteOnExit();
    }

    protected FreeStyleProject getProject() {
        return project;
    }

    protected FreeStyleBuild getBuild() {
        return build;
    }

    protected FilePath getWorkSpace() {
        return build.getWorkspace();
    }

    protected Result getBuildStatus() {
        return build.getResult();
    }

    @Test
    public void Test() throws Exception {
        project = j.createFreeStyleProject();
        project.updateByXml(this.source);
        build = project.scheduleBuild2(0).get();

        verify();
    }

    protected void verify() {

    }
}