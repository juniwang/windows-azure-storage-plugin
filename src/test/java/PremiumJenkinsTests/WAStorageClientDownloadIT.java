package PremiumJenkinsTests;

import com.google.common.collect.Iterables;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.blob.*;
import com.microsoftopentechnologies.windowsazurestorage.AzureStorageBuilder;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.Builder;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static com.microsoftopentechnologies.windowsazurestorage.AzureStorageBuilder.DOWNLOAD_TYPE_CONTAINER;
import static org.junit.Assert.assertEquals;

/**
 * Created by t-yuhang on 7/25/2017.
 */
public class WAStorageClientDownloadIT extends IntegrationTest {

    private static final Logger LOGGER = Logger.getLogger(WAStorageClientDownloadIT.class.getName());

    @Before
    public void setUp() throws Exception{
        testEnvironment = new TestEnvironment();
    }

    @Test
    public void Test() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();

        project.updateByXml((Source)new StreamSource(new FileInputStream(new File("src\\test\\resources\\WAStorageClientUploadITTest.xml"))));

        FreeStyleBuild build = project.scheduleBuild2(0).get();

        assertEquals(build.getResult(), Result.SUCCESS);
        List<Builder> builderList = project.getBuilders();
        String containername = ((AzureStorageBuilder)builderList.get(0)).getContainerName();
        CloudStorageAccount account = new CloudStorageAccount(new StorageCredentialsAccountAndKey(
                testEnvironment.azureStorageAccountName, testEnvironment.azureStorageAccountKey1));
        CloudBlobClient blobClient = account.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference(containername);
        FilePath filePath = build.getWorkspace();
        File file = new File(filePath.getRemote());
        File[] files = file.listFiles();
        assertEquals(files.length, Iterables.size(container.listBlobs()));
    }
}
