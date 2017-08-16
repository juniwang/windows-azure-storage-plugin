package PremiumJenkinsTests;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.microsoftopentechnologies.windowsazurestorage.helper.AzureCredentials;
import hudson.security.ACL;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * Created by t-yuhang on 8/4/2017.
 */
public class GlobalConfigurationIT extends IntegrationTest {

    private static final Logger LOGGER = Logger.getLogger(GlobalConfigurationIT.class.getName());

    @Before
    public void setUp() throws Exception {
        testEnvironment = new TestEnvironment();
    }

    @Test
    public void CredentialTest() throws Exception {
        AzureCredentials azureCredentials = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        AzureCredentials.class,
                        Jenkins.getInstance(),
                        ACL.SYSTEM,
                        Collections.<DomainRequirement>emptyList()),
                CredentialsMatchers.withId(testEnvironment.storageCredentialId));
        assertEquals(azureCredentials.getId(), testEnvironment.storageCredentialId);
        assertEquals(azureCredentials.getStorageAccountName(), testEnvironment.azureStorageAccountName);
        assertEquals(azureCredentials.getStorageKey(), Secret.fromString(testEnvironment.azureStorageAccountKey1).getEncryptedValue());
        assertEquals(azureCredentials.getBlobEndpointURL(), testEnvironment.blobURL);
    }
}
