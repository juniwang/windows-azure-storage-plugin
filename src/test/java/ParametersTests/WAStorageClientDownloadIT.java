package ParametersTests;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.util.*;
import java.util.logging.Logger;
import java.io.File;

import static com.microsoftopentechnologies.windowsazurestorage.AzureStorageBuilder.DOWNLOAD_TYPE_CONTAINER;
import static org.junit.Assert.assertEquals;

/**
 * Created by t-yuhang on 8/16/2017.
 */
@RunWith(Parameterized.class)
public class WAStorageClientDownloadIT extends IntegrationTest{

    private static final Logger LOGGER = Logger.getLogger(WAStorageClientDownloadIT.class.getName());

    private File file;

    public WAStorageClientDownloadIT(File file) {
        this.file = file;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> fileList() {
        Collection<Object[]> fileList = new ArrayList<Object[]>();
        fileList.add(new Object[]{new File("src\\test\\resources\\WAStorageClientUploadITAllFilesTest.xml")});
        fileList.add(new Object[]{new File("src\\test\\resources\\WAStorageClientUploadITTxtFilesTest.xml")});
        fileList.add(new Object[]{new File("src\\test\\resources\\WAStorageClientUploadITPngFilesTest.xml")});
        return fileList;
    }

    private String downloadType;
    private String containername;

    private String includeFilesPattern;
    private String excludeFilesPattern;
    private String downloadDirLoc;

    private CloudStorageAccount account;
    private CloudBlobClient blobClient;
    private CloudBlobContainer container;

    private HashMap<String, String> fileHashMap = new HashMap<>();

    @Before
    public void setUp() throws Exception{
        testEnvironment = new TestEnvironment();
        downloadType = DOWNLOAD_TYPE_CONTAINER;
        containername = testEnvironment.GenerateRandomString(6);

        account = new CloudStorageAccount(new StorageCredentialsAccountAndKey(
                testEnvironment.azureStorageAccountName, testEnvironment.azureStorageAccountKey1));
        blobClient = account.createCloudBlobClient();
        container = blobClient.getContainerReference(containername);
        container.createIfNotExists();

        File dir = new File("resources");
        if(!dir.exists()) {
            dir.mkdir();
        }
        for(int i = 0; i < 20; i ++) {
            String content = testEnvironment.GenerateRandomString(32);
            File file = new File(dir, UUID.randomUUID().toString() + ".txt");
            FileUtils.writeStringToFile(file, content);
            fileHashMap.put(content, file.getName());
            CloudBlockBlob blob = container.getBlockBlobReference(file.getName());
            blob.uploadFromFile(file.getAbsolutePath());
            file.delete();
        }
        for(int i = 0; i < 30; i ++) {
            String content = testEnvironment.GenerateRandomString(32);
            File file = new File(dir, UUID.randomUUID().toString() + ".png");
            FileUtils.writeStringToFile(file, content);
            fileHashMap.put(content, file.getName());
            CloudBlockBlob blob = container.getBlockBlobReference(file.getName());
            blob.uploadFromFile(file.getAbsolutePath());
            file.delete();
        }
        dir.delete();
    }

    @Test
    public void AllTest() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();

        project.updateByXml((Source)new StreamSource(new FileInputStream(file)));
        List<Builder> builderList =  project.getBuilders();
        for(Builder builder: builderList) {
            if(builder instanceof AzureStorageBuilder) {
                ((AzureStorageBuilder) builder).setContainerName(containername);
            }
        }

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(build.getResult(), Result.SUCCESS);
        FilePath filePath = build.getWorkspace();
        File file = new File(filePath.getRemote());
        File[] files = file.listFiles();
        for (File f: files) {
            String content = FileUtils.readFileToString(f);
            assertEquals(f.getName().trim(), fileHashMap.get(content).trim());
        }
    }

    @After
    public void tearDown() throws Exception {
        container.deleteIfExists();
    }
}
