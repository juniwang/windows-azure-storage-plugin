package RegressionTests;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoftopentechnologies.windowsazurestorage.helper.AzureCredentials;
import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Node;
import hudson.model.Result;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.DescribableList;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.transform.Source;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by t-yuhang on 8/21/2017.
 */
@RunWith(AzureJenkinsSuite.class)
public class RegressionTest extends AzureJenkinsFrame {

    public RegressionTest(Source source) {
        super(source);
    }

    String storageCredentialId;
    String containername;

    @Before
    public void setUp() throws Exception {
        storageCredentialId = "storageCredentialId";
        containername = GenerateRandomString(12);

        setEnvVar("containername", containername);

        setAzureCredentials(storageCredentialId);
        generateRandomFilesOnAzure(storageCredentialId, containername, 20, ".txt");
    }

    public void verify() {
        assertEquals(Result.SUCCESS, getBuildStatus());
    }

    @After
    public void tearDown() throws Exception {
        deleteContainerOnAzure(storageCredentialId, containername);
    }
}
