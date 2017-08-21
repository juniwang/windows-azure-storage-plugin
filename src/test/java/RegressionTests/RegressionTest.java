package RegressionTests;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;

import static org.junit.Assert.assertEquals;

/**
 * Created by t-yuhang on 8/21/2017.
 */
@RunWith(AzureJenkinsSuite.class)
public class RegressionTest extends AzureJenkinsTest {

    public RegressionTest(Source source) {
        super(source);
    }

    public void verify() {
        System.out.println("Hello Jenkins!!!");
    }

    @Test
    public void Test() throws Exception {
        addAzureStorageAccountCredential("a8afbc7ca5a3a1a1b61f4ece9725785c");

        FreeStyleProject project = this.j.createFreeStyleProject();
        project.updateByXml(this.source);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(build.getResult(), Result.SUCCESS);

        verify();
    }
}
