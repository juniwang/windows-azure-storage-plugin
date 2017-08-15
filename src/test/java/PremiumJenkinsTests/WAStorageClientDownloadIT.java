package PremiumJenkinsTests;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoftopentechnologies.windowsazurestorage.AzureStorageBuilder;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
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

    private String downloadType;
    private String containername;

    private String includeFilesPattern;
    private String excludeFilesPattern;
    private String downloadDirLoc;

    private boolean flattenDirectories;
    private boolean includeArchiveZips;
    private boolean deleteFromAzureAfterDownload;

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
    public void AllFilesTest() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.updateByXml((Source)new StreamSource(new FileInputStream(new File("D:\\jenkinsci\\windows-azure-storage-plugin\\src\\test\\resources\\WAStorageClientUploadITAllFilesTest.xml"))));

        List<Builder> builderList =  project.getBuilders();
        for(Builder builder: builderList) {
            if(builder instanceof AzureStorageBuilder) {
                ((AzureStorageBuilder) builder).setContainerName(containername);
            }
        }

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        FilePath filePath = build.getWorkspace();
        File file = new File(filePath.getRemote());
        File[] files = file.listFiles();
        assertEquals(files.length, 50);
        for (File f: files) {
            String content = FileUtils.readFileToString(f);
            assertEquals(f.getName().trim(), fileHashMap.get(content).trim());
        }
    }

    @Test
    public void TxtFilesTest() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.updateByXml((Source)new StreamSource(new FileInputStream(new File("D:\\jenkinsci\\windows-azure-storage-plugin\\src\\test\\resources\\WAStorageClientUploadITTxtFilesTest.xml"))));

        List<Builder> builderList =  project.getBuilders();
        for(Builder builder: builderList) {
            if(builder instanceof AzureStorageBuilder) {
                ((AzureStorageBuilder) builder).setContainerName(containername);
            }
        }

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        FilePath filePath = build.getWorkspace();
        File file = new File(filePath.getRemote());
        File[] files = file.listFiles();
        assertEquals(files.length, 20);
        for (File f: files) {
            String content = FileUtils.readFileToString(f);
            assertEquals(f.getName().trim(), fileHashMap.get(content).trim());
        }
    }

    @Test
    public void PngFilesTest() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.updateByXml((Source)new StreamSource(new FileInputStream(new File("D:\\jenkinsci\\windows-azure-storage-plugin\\src\\test\\resources\\WAStorageClientUploadITPngFilesTest.xml"))));

        List<Builder> builderList =  project.getBuilders();
        for(Builder builder: builderList) {
            if(builder instanceof AzureStorageBuilder) {
                ((AzureStorageBuilder) builder).setContainerName(containername);
            }
        }

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        FilePath filePath = build.getWorkspace();
        File file = new File(filePath.getRemote());
        File[] files = file.listFiles();
        assertEquals(files.length, 30);
        for (File f: files) {
            String content = FileUtils.readFileToString(f);
            assertEquals(f.getName().trim(), fileHashMap.get(content).trim());
        }
    }

    @Test
    public void SetDownLocTest() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.updateByXml((Source)new StreamSource(new FileInputStream(new File("D:\\jenkinsci\\windows-azure-storage-plugin\\src\\test\\resources\\WAStorageClientUploadITSetDownLocTest.xml"))));

        downloadDirLoc = project.getRootDir().getAbsolutePath() + "\\DownloadLoc";
        List<Builder> builderList =  project.getBuilders();
        for(Builder builder: builderList) {
            if(builder instanceof AzureStorageBuilder) {
                ((AzureStorageBuilder) builder).setContainerName(containername);
                ((AzureStorageBuilder) builder).setDownloadDirLoc(downloadDirLoc);
            }
        }

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        File file = new File(downloadDirLoc);
        File[] files = file.listFiles();
        assertEquals(files.length, 20);
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
