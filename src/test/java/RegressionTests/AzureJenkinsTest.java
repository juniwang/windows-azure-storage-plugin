package RegressionTests;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.microsoftopentechnologies.windowsazurestorage.helper.AzureCredentials;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import jenkins.model.Jenkins;
import org.junit.ClassRule;
import org.jvnet.hudson.test.JenkinsRule;

import javax.xml.transform.Source;

/**
 * Created by t-yuhang on 8/21/2017.
 */
public class AzureJenkinsTest {

    protected Source source;

    public AzureJenkinsTest(Source source) {
        this.source = source;
    }

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static String loadFromEnv(final String name) {
        return loadFromEnv(name, "");
    }

    private static String loadFromEnv(final String name, final String defaultValue) {
        final String value = System.getenv(name);
        if(value == null || value.isEmpty()) {
            return defaultValue;
        }
        else {
            return value;
        }
    }

    protected void addAzureStorageAccountCredential(String storageCredentialId) throws Exception {
        String azureStorageAccountName = loadFromEnv("AZURE_STORAGE_TEST_STORAGE_ACCOUNT_NAME");
        String azureStorageAccountKey1 = loadFromEnv("AZURE_STORAGE_TEST_STORAGE_ACCOUNT_KEY1");
        String azureStorageAccountKey2 = loadFromEnv("AZURE_STORAGE_TEST_STORAGE_ACCOUNT_KEY2");
        String blobURL = loadFromEnv("BlobURL");

        AzureCredentials azureCredentials = new AzureCredentials(CredentialsScope.GLOBAL, storageCredentialId,
                null, azureStorageAccountName, azureStorageAccountKey1, blobURL);

        CredentialsStore credentialsStore = CredentialsProvider.lookupStores(Jenkins.getInstance()).iterator().next();
        credentialsStore.addCredentials(Domain.global(), azureCredentials);
    }

    protected void runTest() {

    }

    protected void verify() {

    }
}
