package MonkeysTests;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import jenkins.model.Jenkins;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.assertEquals;

/**
 * Created by t-yuhang on 8/9/2017.
 */
public class MonkeysTest {

    @ClassRule
    public static MonkeysRule monkeysRule = new MonkeysRule();

    @Test
    public void monkeysTest() throws Exception {
//        Jenkins jenkins = monkeysRule.getInstance();
//        FreeStyleProject project = (FreeStyleProject)jenkins.createProjectFromXML("AzureStorage",
//                new FileInputStream(new File("D:\\jenkinsci\\windows-azure-storage-plugin\\config\\config_3.0.xml")));
//        FreeStyleBuild build = project.scheduleBuild2(0).get();
//        assertEquals(build.getResult(), Result.SUCCESS);

        String configDir = "D:\\jenkinsci\\windows-azure-storage-plugin\\config";
        monkeysRule.setDirectory(new File(configDir));
    }
}
