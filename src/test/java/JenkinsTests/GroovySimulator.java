package JenkinsTests;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import hudson.FilePath;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.tasks.BatchFile;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static com.microsoftopentechnologies.windowsazurestorage.AzureStorageBuilder.DOWNLOAD_TYPE_CONTAINER;

/**
 * Created by t-yuhang on 8/1/2017.
 */
public class GroovySimulator {
    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    FreeStyleProject project;

    @Before
    public void setUp() throws Exception{
        project = j.createFreeStyleProject();
        project.getBuildersList().add(new BatchFile("echo Hello > hello.txt"));
        project.scheduleBuild2(0).get();
    }


    @Test
    public void addCredentials() {

    }

    @Test
    public void clearBuildQueue() {

    }

    @Test
    public void reloadJobConfig() {
        List<Item> itemList = j.getInstance().getAllItems();
        for (Item item: itemList) {
            String name = item.getFullName();
        }
    }

    @Test
    public void updateEmailAddress() {

    }

    @Test
    public void workspaceCleaner() throws Exception {

    }

}
