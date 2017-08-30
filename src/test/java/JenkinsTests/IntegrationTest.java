package JenkinsTests;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.microsoftopentechnologies.windowsazurestorage.helper.AzureCredentials;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by t-yuhang on 7/25/2017.
 */
public class IntegrationTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private static final Logger LOGGER = Logger.getLogger(IntegrationTest.class.getName());

    protected static class TestEnvironment {
        public final String storageCredentialId;
        public final String azureStorageAccountName;
        public final String azureStorageAccountKey1;
        public final String azureStorageAccountKey2;
        public final String blobURL;

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

        public String GenerateRandomString(int length) {
            String uuid = UUID.randomUUID().toString();
            return uuid.replaceAll("[^a-z0-9]", "a").substring(0, length);
        }

        TestEnvironment() throws Exception {
            azureStorageAccountName = loadFromEnv("AZURE_STORAGE_TEST_STORAGE_ACCOUNT_NAME");
            azureStorageAccountKey1 = loadFromEnv("AZURE_STORAGE_TEST_STORAGE_ACCOUNT_KEY1");
            azureStorageAccountKey2 = loadFromEnv("AZURE_STORAGE_TEST_STORAGE_ACCOUNT_KEY2");
            blobURL = loadFromEnv("BlobURL");

            AzureCredentials.StorageAccountCredential storageCreds = new AzureCredentials.StorageAccountCredential(
                    azureStorageAccountName, azureStorageAccountKey1, blobURL);
            storageCredentialId = storageCreds.getId();
            AzureCredentials azureCredentials = new AzureCredentials(CredentialsScope.GLOBAL, storageCredentialId,
                    null, azureStorageAccountName, azureStorageAccountKey1, blobURL);

            CredentialsStore credentialsStore = CredentialsProvider.lookupStores(Jenkins.getInstance()).iterator().next();
            credentialsStore.addCredentials(Domain.global(), azureCredentials);
        }
    }

    protected TestEnvironment testEnvironment = null;
}
